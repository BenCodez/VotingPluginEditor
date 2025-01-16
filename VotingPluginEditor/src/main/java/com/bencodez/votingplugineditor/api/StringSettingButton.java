package com.bencodez.votingplugineditor.api;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText, String defaultValue) {
		this.key = key;
		initialValue = PanelUtils.getStringValue(data, key, defaultValue);
		this.labelText = labelText;
		getComponent(panel);
	}

	public JTextField createLabelAndTextField(JPanel panel) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));

		textField = new JTextField(initialValue);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

		subPanel.add(label);
		subPanel.add(textField);
		panel.add(subPanel);

		return textField;
	}

	@Override
	public boolean hasChanged() {
		return !textField.getText().equals(initialValue);
	}

	@Override
	public Object getValue() {
		return textField.getText();
	}

	@Override
	public Component getComponent(JPanel panel) {
		return createLabelAndTextField(panel);
	}

}
