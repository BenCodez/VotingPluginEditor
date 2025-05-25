package com.bencodez.votingplugineditor.api.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.bencodez.votingplugineditor.api.sftp.SFTPConnection;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;

public abstract class YmlConfigHandler {
    protected String filePath;
    @Getter
    private String pluginDirectory;

    @Getter
    protected Map<String, Object> configData;
    // full YAML path (e.g. "a.b.c") → comment block
    protected Map<String, String> commentMap;

    @Getter
    @Setter
    private SFTPSettings sFTPSettings;

    public YmlConfigHandler(String filePath, String pluginDirectory, SFTPSettings sftpSettings) {
        this.filePath = filePath;
        this.pluginDirectory = pluginDirectory;
        this.configData = new LinkedHashMap<>();
        this.commentMap = new LinkedHashMap<>();
        this.sFTPSettings = sftpSettings;
        load();
    }

    public boolean useSFTP() {
        return sFTPSettings != null && sFTPSettings.isEnabled();
    }

    /** Downloads via SFTP and returns YAML body (comments parsed out). */
    public String loadConfigFromSFTP(String host,
                                     int port,
                                     String user,
                                     String password,
                                     String remotePath) {
        StringBuilder raw = new StringBuilder();
        try (SSHClient sshClient = SFTPConnection.createSession(host, port, user, password);
             SFTPClient sftpClient = sshClient.newSFTPClient();
             RemoteFile remoteFile = sftpClient.open(remotePath, EnumSet.of(OpenMode.READ))) {

            byte[] buffer = new byte[8192];
            long offset = 0;
            int bytesRead;
            while ((bytesRead = remoteFile.read(offset, buffer, 0, buffer.length)) > 0) {
                raw.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                offset += bytesRead;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config from SFTP: " + remotePath, ex);
        }
        return parseComments(raw.toString());
    }

    /** Sends YAML text via SFTP to remotePath. */
    public void saveConfigToSFTP(String host,
                                 int port,
                                 String user,
                                 String password,
                                 String remotePath,
                                 String content) {
        try (SSHClient sshClient = SFTPConnection.createSession(host, port, user, password);
             SFTPClient sftpClient = sshClient.newSFTPClient();
             RemoteFile remoteFile = sftpClient.open(remotePath,
                     EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC))) {

            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            remoteFile.write(0, data, 0, data.length);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save config to SFTP: " + remotePath, ex);
        }
    }

    /** Reads entire local file, then parse out comments. */
    public String loadFromLocal() {
        StringBuilder raw = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                raw.append(line).append(System.lineSeparator());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load local config: " + filePath, e);
        }
        return parseComments(raw.toString());
    }

    /**
     * Parses out comment blocks and stores them under full YAML paths.
     * Returns the YAML text without standalone comment lines.
     */
    private String parseComments(String rawText) {
        StringBuilder yamlBody = new StringBuilder();
        StringBuilder lastComment = new StringBuilder();
        List<String> pathStack = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(rawText))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("#")) {
                    lastComment.append(line).append(System.lineSeparator());
                } else if (trimmed.contains(":")) {
                    int indentSpaces = line.indexOf(trimmed);
                    int level = indentSpaces / 2;
                    String key = trimmed.split(":", 2)[0];

                    while (pathStack.size() > level) {
                        pathStack.remove(pathStack.size() - 1);
                    }
                    if (pathStack.size() == level) {
                        pathStack.add(key);
                    } else {
                        pathStack.set(level, key);
                    }
                    String fullPath = String.join(".", pathStack);

                    if (lastComment.length() > 0) {
                        commentMap.put(fullPath, lastComment.toString());
                        lastComment.setLength(0);
                    }
                    yamlBody.append(line).append(System.lineSeparator());
                } else {
                    yamlBody.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            // not expected with StringReader
        }
        return yamlBody.toString();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        String yamlContent = useSFTP()
            ? loadConfigFromSFTP(
                  sFTPSettings.getHost(),
                  sFTPSettings.getPort(),
                  sFTPSettings.getUser(),
                  sFTPSettings.getPassword(),
                  computeRemotePath())
            : loadFromLocal();

        if (yamlContent == null) {
            configData = new LinkedHashMap<>();
        } else {
            Object loaded = new Yaml().load(yamlContent);
            configData = (loaded instanceof Map)
                ? (Map<String,Object>)loaded
                : new LinkedHashMap<>();
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setPrettyFlow(true);
            opts.setIndent(2);
            Yaml yaml = new Yaml(opts);

            StringBuilder out = new StringBuilder();
            for (Map.Entry<String,Object> e : configData.entrySet()) {
                writeYaml(out, yaml, e.getKey(), e.getValue(), 0, "");
            }

            if (useSFTP()) {
                saveConfigToSFTP(
                    sFTPSettings.getHost(),
                    sFTPSettings.getPort(),
                    sFTPSettings.getUser(),
                    sFTPSettings.getPassword(),
                    computeRemotePath(),
                    out.toString());
            } else {
                writer.write(out.toString());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save config: " + filePath, ex);
        }
    }

    private String computeRemotePath() {
        String filename = new File(filePath).getName();
        int idx = filename.indexOf(".yml");
        if (idx != -1) filename = filename.substring(0, idx + 4);
        return pluginDirectory + "/" + filename;
    }

    @SuppressWarnings("unchecked")
    private void writeYaml(Appendable out,
                           Yaml yaml,
                           String key,
                           Object value,
                           int indentLevel,
                           String parentPath) throws IOException {
        String indent = new String(new char[indentLevel]).replace("\0", "  ");
        String thisPath = parentPath.isEmpty() ? key : parentPath + "." + key;

        if (commentMap.containsKey(thisPath)) {
            out.append(commentMap.get(thisPath));
        }

        String formattedKey = quoteIfNeeded(key);

        if (value instanceof Map) {
            out.append(indent).append(formattedKey).append(":\n");
            for (Map.Entry<String,Object> sub : ((Map<String,Object>)value).entrySet()) {
                writeYaml(out, yaml, sub.getKey(), sub.getValue(), indentLevel+1, thisPath);
            }
        } else if (value instanceof List) {
            out.append(indent).append(formattedKey).append(":\n");
            for (Object item : (List<Object>)value) {
                out.append(indent)
                   .append("  - ")
                   .append(convertValueToString(item))
                   .append("\n");
            }
        } else if (value instanceof String[]) {
            out.append(indent).append(formattedKey).append(":\n");
            for (String item : (String[])value) {
                out.append(indent)
                   .append("  - ")
                   .append(quoteIfNeeded(item))
                   .append("\n");
            }
        } else {
            out.append(indent)
               .append(formattedKey)
               .append(": ")
               .append(convertValueToString(value))
               .append("\n");
        }
    }

    private String convertValueToString(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return quoteIfNeeded((String)value);
        return value.toString();
    }

    private String quoteIfNeeded(String v) {
        if (v.matches("^[a-zA-Z0-9_-]+$")) return v;
        return "'" + v.replace("'", "''") + "'";
    }

    public Object get(String path) {
        return get(path, null);
    }

    @SuppressWarnings("unchecked")
    public Object get(String path, Object defaultValue) {
        String[] keys = path.split("\\.");
        Map<String,Object> current = configData;
        for (int i = 0; i < keys.length-1; i++) {
            Object nested = current.get(keys[i]);
            if (!(nested instanceof Map)) return defaultValue;
            current = (Map<String,Object>)nested;
        }
        return current.getOrDefault(keys[keys.length-1], defaultValue);
    }

    @SuppressWarnings("unchecked")
    public void remove(String path) {
        String[] keys = path.split("\\.");
        Map<String,Object> current = configData;
        for (int i = 0; i < keys.length-1; i++) {
            Object nested = current.get(keys[i]);
            if (!(nested instanceof Map)) return;
            current = (Map<String,Object>)nested;
        }
        current.remove(keys[keys.length-1]);
    }
    
    public Object get(Map<String, Object> configData, String path, Object defaultValue) {
		String[] keys = path.split("\\.");
		Map<String, Object> current = configData;
		for (int i = 0; i < keys.length - 1; i++) {
			Object nested = current.get(keys[i]);
			if (nested instanceof Map) {
				current = (Map<String, Object>) nested;
			} else {
				return defaultValue;
			}
		}
		return current.getOrDefault(keys[keys.length - 1], defaultValue);
	}

    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String,Object> current = configData;
        for (int i = 0; i < keys.length-1; i++) {
            Object nested = current.get(keys[i]);
            if (!(nested instanceof Map)) {
                nested = new LinkedHashMap<>();
                current.put(keys[i], nested);
            }
            current = (Map<String,Object>)nested;
        }
        current.put(keys[keys.length-1], value);
    }

    public abstract void openEditorGUI();
}
