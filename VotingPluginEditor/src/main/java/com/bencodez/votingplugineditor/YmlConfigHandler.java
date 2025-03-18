package com.bencodez.votingplugineditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.Getter;
import lombok.Setter;

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
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp channelSftp = null;
		try {
			session = jsch.getSession(user, host, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			InputStream input = channelSftp.get(remotePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			// Handle exceptions appropriately
		} finally {
			if (channelSftp != null && channelSftp.isConnected()) {
				channelSftp.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return content.toString();
	}

	public void saveConfigToSFTP(String host, int port, String user, String password, String remotePath,
			String content) {
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp channelSftp = null;
		try {
			session = jsch.getSession(user, host, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
			channelSftp.put(inputStream, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			// Handle exceptions appropriately
		} finally {
			if (channelSftp != null && channelSftp.isConnected()) {
				channelSftp.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
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
		String indent = "  ".repeat(indentLevel);
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
		if (value.matches("^\\w+$")) {
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