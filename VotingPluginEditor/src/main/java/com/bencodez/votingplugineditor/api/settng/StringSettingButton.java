package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bencodez.votingplugineditor.PanelUtils;

import lombok.Getter;

public class StringSettingButton implements SettingButton {
	@Getter
	private String key;

	private String initialValue;

	private String labelText;

	private JTextField textField;

	private String[] options;

	private JComboBox optionsBox;

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue) {
		this.key = key;
		initialValue = PanelUtils.getStringValue(data, key, defaultValue);
		this.labelText = labelText;
		getComponent(panel);
	}

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue, String[] options) {
		this.key = key;
		initialValue = PanelUtils.getStringValue(data, key, defaultValue);
		this.labelText = labelText;
		this.options = options;
		getComponent(panel);
	}

	public JTextField createLabelAndTextField(JPanel panel) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 5));

		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(300, label.getPreferredSize().height));
		subPanel.add(label);

		if (options != null && options.length > 0) {
			optionsBox = new JComboBox<>(options);
			optionsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, optionsBox.getPreferredSize().height));
			optionsBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
			optionsBox.setSelectedItem(initialValue);
			subPanel.add(optionsBox);
		} else {
			textField = new JTextField(initialValue);
			textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
			textField.setAlignmentX(Component.RIGHT_ALIGNMENT);
			subPanel.add(textField);
		}

		panel.add(subPanel);

		return textField;
	}

	@Override
	public boolean hasChanged() {
		if (textField != null) {
			return !textField.getText().equals(initialValue);
		} else {
			return !optionsBox.getSelectedItem().toString().equals(initialValue);
		}
	}

	@Override
	public Object getValue() {
		if (textField != null) {
			return textField.getText();
		} else {
			return (String) optionsBox.getSelectedItem();
		}
	}

	@Override
	public Component getComponent(JPanel panel) {
		return createLabelAndTextField(panel);
	}

	@Override
	public void updateValue() {
		initialValue = (String) getValue();
	}

}
