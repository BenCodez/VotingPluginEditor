package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.PanelUtils;

import lombok.Getter;

public class BooleanSettingButton implements SettingButton {
	@Getter
	private String key;

	private boolean initialValue;

	private String labelText;

	private JCheckBox checkBox;

	public BooleanSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText) {
		this(panel, key, data, labelText, false, null);
	}

	public BooleanSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			boolean defaultValue) {
		this(panel, key, data, labelText, defaultValue, null);
	}

	public BooleanSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			boolean defaultValue, String hoverText) {
		this.key = key;
		initialValue = PanelUtils.getBooleanValue(data, key, defaultValue);
		this.labelText = labelText;
		getComponent(panel);
		checkBox.setToolTipText(hoverText);
	}

	public JCheckBox createLabelAndTextField(JPanel panel) {
		checkBox = new JCheckBox(labelText, initialValue);
		checkBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(checkBox);
		return checkBox;
	}

	@Override
	public boolean hasChanged() {
		return !checkBox.isSelected() == initialValue;
	}

	@Override
	public Object getValue() {
		return checkBox.isSelected();
	}

	@Override
	public Component getComponent(JPanel panel) {
		return createLabelAndTextField(panel);
	}

	@Override
	public void updateValue() {
		initialValue = (boolean) getValue();
	}

	@Override
	public void setMaxWidth(int width) {
		checkBox.setMaximumSize(new Dimension(width, checkBox.getPreferredSize().height));
		checkBox.setPreferredSize(new Dimension(width, checkBox.getPreferredSize().height));
	}

	@Override
	public int getWidth() {
		return checkBox.getPreferredSize().width;
	}

}