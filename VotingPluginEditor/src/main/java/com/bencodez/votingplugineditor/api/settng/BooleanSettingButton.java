package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
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
		this.key = key;
		initialValue = PanelUtils.getBooleanValue(data, key, false);
		this.labelText = labelText;
		getComponent(panel);
	}

	public BooleanSettingButton(JPanel panel, String key, Map<String, Object> data, String labelText,
			boolean defaultValue) {
		this.key = key;
		initialValue = PanelUtils.getBooleanValue(data, key, defaultValue);
		this.labelText = labelText;
		getComponent(panel);
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

}
