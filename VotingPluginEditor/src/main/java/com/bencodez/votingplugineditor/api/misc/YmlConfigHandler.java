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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

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

    private String parseComments(String rawText) {
        LoaderOptions options = new LoaderOptions();
        options.setProcessComments(true);
        Node root = new Yaml(options).compose(new StringReader(rawText));
        commentMap.clear();
        collectComments(root, "", 0);
        return rawText;
    }

    private void collectComments(Node node, String parentPath, int depth) {
        if (!(node instanceof MappingNode) || node.isTwoStepsConstruction()) {
            return;
        }
        for (NodeTuple entry : ((MappingNode) node).getValue()) {
            Node key = entry.getKeyNode();
            if (!(key instanceof ScalarNode)) {
                continue;
            }
            String name = ((ScalarNode) key).getValue();
            String path = parentPath.isEmpty() ? name : parentPath + "." + name;
            List<CommentLine> comments = key.getBlockComments();
            if (comments != null) {
                StringBuilder block = new StringBuilder();
                for (CommentLine comment : comments) {
                    if (comment.getCommentType() == CommentType.BLOCK) {
                        block.append("  ".repeat(depth)).append('#').append(comment.getValue()).append('\n');
                    }
                }
                if (block.length() > 0) {
                    commentMap.put(path, block.toString());
                }
            }
            collectComments(entry.getValueNode(), path, depth + 1);
        }
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
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setIndent(2);
        Yaml yaml = new Yaml(opts);

        StringBuilder out = new StringBuilder();
        try {
            for (Map.Entry<?, ?> e : configData.entrySet()) {
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
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(out.toString());
                }
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
                           Object key,
                           Object value,
                           int indentLevel,
                           String parentPath) throws IOException {
        String indent = new String(new char[indentLevel]).replace("\0", "  ");
        String thisPath = parentPath.isEmpty() ? String.valueOf(key) : parentPath + "." + key;

        if (commentMap.containsKey(thisPath)) {
            out.append(commentMap.get(thisPath));
        }

        if (value instanceof Map<?, ?> && !((Map<?, ?>) value).isEmpty()) {
            // Let the emitter quote the key while keeping nested entries available for comments.
            String header = yaml.dump(Collections.singletonMap(key, Collections.emptyMap()));
            out.append(indent).append(header, 0, header.length() - " {}\n".length()).append("\n");
            for (Map.Entry<?, ?> sub : ((Map<?, ?>) value).entrySet()) {
                writeYaml(out, yaml, sub.getKey(), sub.getValue(), indentLevel + 1, thisPath);
            }
        } else {
            Object yamlValue = value instanceof String[] ? Arrays.asList((String[]) value) : value;
            out.append(yaml.dump(Collections.singletonMap(key, yamlValue)).indent(indentLevel * 2));
        }
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
