package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

import lombok.Getter;

public class DoubleSettingButton implements SettingButton {
	@Getter
	private String key;

	private double initialValue;

	private String labelText;

	private JTextField textField;

	private JLabel label;

	public DoubleSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			double defaultValue) {
		this(panel, key, data, labelText, defaultValue, null);
	}

	public DoubleSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			double defaultValue, String hoverText) {
		this.key = key;
		initialValue = PanelUtils.getDoubleValue(data, key, defaultValue);
		this.labelText = labelText;
		getComponent(panel);
		label.setToolTipText(hoverText);
	}

	public JTextField createLabelAndTextField(JPanel panel) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 5));

		label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));

		textField = new JTextField("" + initialValue);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
		textField.setAlignmentX(Component.RIGHT_ALIGNMENT);

		subPanel.add(label);
		subPanel.add(textField);
		panel.add(subPanel);

		return textField;
	}
	
	public JTextField getComponent() {
		return textField;
	}
	
	public void setVisible(boolean visible) {
		label.setVisible(visible);
		textField.setVisible(visible);
	}

	@Override
	public boolean hasChanged() {
		return !textField.getText().equals("" + initialValue);
	}

	@Override
	public Object getValue() {
		if (textField.getText().isBlank()) {
			return 0;
		}
		return Double.parseDouble(textField.getText());
	}

	@Override
	public Component getComponent(JPanel panel) {
		return createLabelAndTextField(panel);
	}

	@Override
	public void updateValue() {
		initialValue = (double) getValue();
	}
	
	private boolean isWidthSet = false;

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
