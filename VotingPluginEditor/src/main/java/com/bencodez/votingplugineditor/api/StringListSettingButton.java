package com.bencodez.votingplugineditor.api;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.bencodez.votingplugineditor.PanelUtils;

import lombok.Getter;

public class StringListSettingButton implements SettingButton {
	@Getter
	private String key;

	private String initialValue;

	private String labelText;

	private JTextArea textArea;

	public StringListSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText) {
		this.key = key;
		initialValue = PanelUtils.getStringList(data, key);
		this.labelText = labelText;
		getComponent(panel);
	}

	public JTextArea createLabelAndTextArea(JPanel panel) {
		System.out.println(labelText + " " + initialValue);
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		textArea = new JTextArea(initialValue);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(150, 70));

		subPanel.add(label);
		subPanel.add(scrollPane);
		panel.add(subPanel);

		return textArea;
	}

	@Override
	public boolean hasChanged() {
		System.out.println(key +  textArea.getText() + " " + initialValue);
		return !textArea.getText().equals(initialValue);
	}

	@Override
	public Object getValue() {
		return textArea.getText().split("\n");
	}

	@Override
	public Component getComponent(JPanel panel) {
		return createLabelAndTextArea(panel);
	}

	@Override
	public void updateValue() {
		initialValue = textArea.getText();
	}
}
