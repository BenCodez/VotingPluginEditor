package com.bencodez.votingplugineditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public abstract class YmlConfigHandler {
	protected String filePath;
	protected Map<String, Object> configData;
	protected Map<String, String> commentMap;

	public YmlConfigHandler(String filePath) {
		this.filePath = filePath;
		this.configData = new LinkedHashMap<>();
		this.commentMap = new LinkedHashMap<>();
		load();
	}

	public void load() {
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
			Yaml yaml = new Yaml();
			configData = yaml.load(yamlContent.toString());
			if (configData == null) {
				configData = new LinkedHashMap<>();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setPrettyFlow(true);
			options.setIndent(2);
			Yaml yaml = new Yaml(options);

			for (Map.Entry<String, Object> entry : configData.entrySet()) {
				String key = entry.getKey();
				if (commentMap.containsKey(key)) {
					writer.write(commentMap.get(key));
				}
				writeYaml(writer, yaml, key, entry.getValue(), 0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeYaml(BufferedWriter writer, Yaml yaml, String key, Object value, int indentLevel)
			throws IOException {
		String indent = "  ".repeat(indentLevel);
		if (value instanceof Map) {
			writer.write(indent + key + ":\n");
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				writeYaml(writer, yaml, entry.getKey(), entry.getValue(), indentLevel + 1);
			}
		} else if (value instanceof List) {
			writer.write(indent + key + ":\n");
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;
			for (Object item : list) {
				String quotedItem = quoteIfNeeded(item.toString());
				writer.write(indent + "  - " + quotedItem + "\n");
			}
		} else if (value instanceof String[]) {
			writer.write(indent + key + ":\n");
			for (String item : (String[]) value) {
				String quotedItem = quoteIfNeeded(item);
				writer.write(indent + "  - " + quotedItem + "\n");
			}
		} else if (value instanceof String) {
			writer.write(indent + key + ": " + quoteIfNeeded((String) value) + "\n");
		} else {
			writer.write(indent + key + ": " + value.toString() + "\n");
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

	public void set(String path, Object value) {
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