package com.bencodez.votingplugineditor;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class PanelUtils {
	public static JPanel createLabelAndTextField(String labelText, String initialValue) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // adds padding

		JLabel label = new JLabel(labelText);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
		label.setAlignmentY(Component.CENTER_ALIGNMENT);

		JTextField textField = new JTextField(initialValue);
		textField.setHorizontalAlignment(SwingConstants.CENTER); // Center text in the text field
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
		textField.setAlignmentY(Component.CENTER_ALIGNMENT);

		panel.add(label);
		panel.add(Box.createHorizontalStrut(10)); // adds space between label and text field
		panel.add(textField);

		return panel;
	}

	public static Object getState(String path, Map<String, Object> data) {
		String[] p = path.split(Pattern.quote("."));
		System.out.println(path);
		if (data.containsKey(p[0])) {
			Object ob = data.get(p[0]);
			if (ob instanceof Map) {
				return getState(path.replaceFirst(p[0] + ".", ""), (Map<String, Object>) data.get(p[0]));
			}
			return ob;
		}
		return null;
	}

	public static boolean getBooleanValue(Map<String, Object> data, String key) {
		if (key.contains(".")) {
			String[] p = key.split(Pattern.quote("."));
			if (data.containsKey(p[0])) {
				Object ob = data.get(p[0]);
				if (ob instanceof Map) {
					return getBooleanValue((Map<String, Object>) data.get(p[0]), key.replaceFirst(p[0] + ".", ""));
				}
				return Boolean.TRUE.equals(ob);
			}
		} else {
			return Boolean.TRUE.equals(data.get(key));
		}
		return false;
	}

	public static String getStringValue(Map<String, Object> data, String key, String defaultValue) {
		if (key.contains(".")) {
			String[] p = key.split(Pattern.quote("."));
			if (data.containsKey(p[0])) {
				Object ob = data.getOrDefault(p[0], defaultValue);
				if (ob instanceof Map) {
					return getStringValue((Map<String, Object>) data.get(p[0]), key.replaceFirst(p[0] + ".", ""),
							defaultValue);
				}
				return (String) ob;
			}
		} else {
			return (String) data.getOrDefault(key, defaultValue);
		}
		return defaultValue;
	}

	public static String getStringList(Map<String, Object> data, String key) {
		System.out.println(key);
		Object list = data.get(key);
		if (key.contains(".")) {
			String[] p = key.split(Pattern.quote("."));
			if (data.containsKey(p[0])) {
				Object ob = data.get(p[0]);
				if (ob instanceof Map) {
					return getStringList((Map<String, Object>) data.get(p[0]), key.replaceFirst(p[0] + ".", ""));
				}
			}
		} else if (list instanceof String[]) {
			return String.join("\n", (String[]) list);
		} else if (list instanceof Iterable) {
			// Handle lists in the form of Iterable
			StringBuilder commandList = new StringBuilder();
			for (Object command : (Iterable<?>) list) {
				commandList.append(command).append("\n");
			}
			return commandList.toString().trim(); // Removing trailing newline
		} else if (list instanceof String) {
			return list.toString();
		}
		return ""; // Return an empty string if no commands were found
	}

	public static int getIntValue(Map<String, Object> data, String key, int defaultValue) {
		if (key.contains(".")) {
			String[] p = key.split(Pattern.quote("."));
			if (data.containsKey(p[0])) {
				Object ob = data.getOrDefault(p[0], defaultValue);
				if (ob instanceof Map) {
					return getIntValue((Map<String, Object>) data.get(p[0]), key.replaceFirst(p[0] + ".", ""),
							defaultValue);
				}
				if (ob instanceof Number) {
					return ((Number) ob).intValue();
				}
			}
		} else {
			Object value = data.get(key);
			if (value instanceof Number) {
				return ((Number) value).intValue();
			}
		}
		return defaultValue;

	}

	public static String[] convertListToArray(List<String> list) {
		return list != null ? list.toArray(new String[0]) : new String[0];
	}

	public static String[] convertSetToArray(Set<String> set) {
		return set != null ? set.toArray(new String[0]) : new String[0];
	}

	public static JPanel createLabelAndCheckbox(String labelText, boolean isSelected) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // adds padding

		JLabel label = new JLabel(labelText);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
		label.setAlignmentY(Component.CENTER_ALIGNMENT);

		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(isSelected);
		checkBox.setAlignmentY(Component.CENTER_ALIGNMENT);

		panel.add(label);
		panel.add(Box.createHorizontalStrut(10)); // Adds space between the label and checkbox
		panel.add(checkBox);

		return panel;
	}
}
