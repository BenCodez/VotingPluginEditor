
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

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

import lombok.Getter;

public class StringSettingButton implements SettingButton {
	@Getter
	private String key;

	private String initialValue;

	private String labelText;

	private JTextField textField;

	private String[] options;

	private JComboBox optionsBox;

	private JLabel label;

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue) {
		this(panel, key, data, labelText, defaultValue, null, null);
	}

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue, String[] options) {
		this(panel, key, data, labelText, defaultValue, options, null);
	}

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue, String hoverText) {
		this(panel, key, data, labelText, defaultValue, null, hoverText);
	}

	public StringSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			String defaultValue, String[] options, String hoverText) {
		this.key = key;
		initialValue = PanelUtils.getStringValue(data, key, defaultValue);
		this.labelText = labelText;
		this.options = options;
		getComponent(panel);
		label.setToolTipText(hoverText);
	}

	public JTextField createLabelAndTextField(JPanel panel) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 5));

		label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));
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

	private boolean isWidthSet = false;

	public JTextField getComponent() {
		return textField;
	}

	public void setVisible(boolean visible) {
		label.setVisible(visible);
		if (textField != null) {
			textField.setVisible(visible);
		} else {
			optionsBox.setVisible(visible);
		}
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
