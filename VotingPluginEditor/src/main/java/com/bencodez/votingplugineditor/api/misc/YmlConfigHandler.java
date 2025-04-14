package com.bencodez.votingplugineditor.api.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
	protected Map<String, String> commentMap;

	@Getter
	@Setter
	private boolean useSFTP = false;

	@Getter
	@Setter
	private SFTPSettings sFTPSettings;

	public YmlConfigHandler(String filePath, String votingPluginDirectory, SFTPSettings sftpSettings) {
		this.filePath = filePath;
		this.configData = new LinkedHashMap<>();
		this.commentMap = new LinkedHashMap<>();
		this.pluginDirectory = votingPluginDirectory;
		this.sFTPSettings = sftpSettings;
		load();
	}

	public String loadConfigFromSFTP(String host, int port, String user, String password, String remotePath) {
		StringBuilder content = new StringBuilder();
		try (SSHClient sshClient = SFTPConnection.createSession(host, port, user, password)) {
			try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
				try (RemoteFile remoteFile = sftpClient.open(remotePath)) {
					byte[] buffer = new byte[8192];
					long offset = 0;
					int bytesRead;
					while ((bytesRead = remoteFile.read(offset, buffer, 0, buffer.length)) > 0) {
						content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
						offset += bytesRead;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// Handle exceptions appropriately
		}
		return content.toString();
	}

	public void saveConfigToSFTP(String host, int port, String user, String password, String remotePath,
			String content) {
		try (SSHClient sshClient = SFTPConnection.createSession(host, port, user, password)) {
			try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
				try (RemoteFile remoteFile = sftpClient.open(remotePath,
						EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC))) {
					byte[] data = content.getBytes(StandardCharsets.UTF_8);
					remoteFile.write(0, data, 0, data.length);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// Handle exceptions appropriately
		}
	}

	public String loadFromLocal() {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			StringBuilder yamlContent = new StringBuilder();
			String lastComment = "";
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("#")) {
					lastComment += line + System.lineSeparator();
				} else if (line.contains(":")) {
					String key = line.split(":", 2)[0].trim();
					yamlContent.append(line).append(System.lineSeparator());
					if (!lastComment.isEmpty()) {
						commentMap.put(key, lastComment);
						lastComment = "";
					}
				} else {
					yamlContent.append(line).append(System.lineSeparator());
				}
			}

			return yamlContent.toString();

		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void load() {
		String yamlContent = useSFTP
				? loadConfigFromSFTP(getSFTPSettings().getHost(), getSFTPSettings().getPort(),
						getSFTPSettings().getUser(), getSFTPSettings().getPassword(), pluginDirectory)
				: loadFromLocal();
		if (yamlContent == null) {
			return;
		}

		Yaml yaml = new Yaml();
		configData = yaml.load(yamlContent);
	}

	public void save() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setPrettyFlow(true);
			options.setIndent(2);
			Yaml yaml = new Yaml(options);

			StringBuilder yamlContent = new StringBuilder();
			for (Map.Entry<String, Object> entry : configData.entrySet()) {
				String key = entry.getKey();
				if (commentMap.containsKey(key)) {
					yamlContent.append(commentMap.get(key));
				}
				writeYaml(yamlContent, yaml, key, entry.getValue(), 0);
			}

			if (useSFTP) {
				saveConfigToSFTP("host", 22, "user", "password", filePath, yamlContent.toString());
			} else {
				writer.write(yamlContent.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeYaml(Appendable appendable, Yaml yaml, String key, Object value, int indentLevel)
			throws IOException {
		String indent = new String(new char[indentLevel]).replace("\0", "  ");
		String formattedKey = convertKeyToString(key);

		if (value instanceof Map) {
			appendable.append(indent).append(formattedKey).append(":\n");
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				writeYaml(appendable, yaml, convertKeyToString(entry.getKey()), entry.getValue(), indentLevel + 1);
			}
		} else if (value instanceof List) {
			appendable.append(indent).append(formattedKey).append(":\n");
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;
			for (Object item : list) {
				appendable.append(indent).append("  - ").append(convertValueToString(item)).append("\n");
			}
		} else if (value instanceof String[]) {
			appendable.append(indent).append(formattedKey).append(":\n");
			for (String item : (String[]) value) {
				appendable.append(indent).append("  - ").append(quoteIfNeeded(item)).append("\n");
			}
		} else {
			appendable.append(indent).append(formattedKey).append(": ").append(convertValueToString(value))
					.append("\n");
		}
	}

	private String convertKeyToString(Object key) {
		if (key instanceof String) {
			return quoteIfNeeded((String) key);
		} else {
			// Convert non-string keys to a string to avoid potential issues when writing to
			// YAML
			return quoteIfNeeded(String.valueOf(key));
		}
	}

	private String convertValueToString(Object value) {
		if (value == null) {
			return "null"; // YAML representation for null values
		} else if (value instanceof String) {
			return quoteIfNeeded((String) value);
		} else if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		} else {
			return quoteIfNeeded(value.toString()); // Encode other types as strings
		}
	}

	private String quoteIfNeeded(String value) {
		if (value.matches("^[a-zA-Z0-9_-]+$")) {
			return value;
		}
		return "'" + value.replace("'", "''") + "'";
	}

	public Object get(String path) {
		return get(path, null);
	}

	public Object get(String path, Object defaultValue) {
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

	public void remove(String path) {
		System.out.println("Removing: " + path);
		String[] keys = path.split("\\.");
		Map<String, Object> current = configData;
		for (int i = 0; i < keys.length - 1; i++) {
			Object nested = current.get(keys[i]);
			if (nested instanceof Map) {
				current = (Map<String, Object>) nested;
			} else {
				return; // Path not found or doesn't exist as a map
			}
		}
		current.remove(keys[keys.length - 1]);
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

	public void set(String path, Object value) {
		System.out.println("Setting: " + path + " to " + value.toString());
		String[] keys = path.split("\\.");
		Map<String, Object> current = configData;
		for (int i = 0; i < keys.length - 1; i++) {
			Object nested = current.get(keys[i]);
			if (!(nested instanceof Map)) {
				nested = new LinkedHashMap<>();
				current.put(keys[i], nested);
			}
			current = (Map<String, Object>) nested;
		}
		current.put(keys[keys.length - 1], value);
	}

	public abstract void openEditorGUI();
}