
package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

import lombok.Getter;

public class StringListSettingButton implements SettingButton {
	@Getter
	private String key;

	private String initialValue;

	private String labelText;

	private JTextArea textArea;

	private JLabel label;

	public StringListSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText) {
		this(panel, key, data, labelText, null);
	}

	public StringListSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String hoverText) {
		this.key = key;
		initialValue = PanelUtils.getStringList(data, key);
		this.labelText = labelText;
		getComponent(panel);
		label.setToolTipText(hoverText);
	}

	public JTextArea createLabelAndTextArea(JPanel panel) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		label = new JLabel(labelText);
		textArea = new JTextArea(initialValue);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(150, 70));
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

		subPanel.add(label);
		subPanel.add(scrollPane);
		panel.add(subPanel);

		return textArea;
	}

	@Override
	public boolean hasChanged() {
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
	
	private boolean isWidthSet = false;
	
	public JTextArea getComponent() {
        return textArea;
	}
	
	public void setVisible(boolean visible) {
        label.setVisible(visible);
        textArea.setVisible(visible);
	}

	@Override
	public void setMaxWidth(int width) {
		if (isWidthSet) {
			return;
		}
		label.setMaximumSize(new Dimension(width, label.getPreferredSize().height));
		label.setPreferredSize(new Dimension(width, label.getPreferredSize().height));
		isWidthSet = true;
	}

	@Override
	public int getWidth() {
		return label.getFontMetrics(label.getFont()).stringWidth(label.getText());
	}
}
