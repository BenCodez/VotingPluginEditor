package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

import lombok.Getter;

public class BooleanSettingButton implements SettingButton {
	@Getter
	private String key;

	private boolean initialValue;

	private String labelText;

	private JCheckBox checkBox;

	public void addActionListener(ActionListener r) {
		checkBox.addActionListener(r);
	}

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

	public JCheckBox getComponent() {
		return checkBox;
	}

	public void setVisible(boolean visible) {
		checkBox.setVisible(visible);
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

	private boolean isWidthSet = false;

	@Override
	public void setMaxWidth(int width) {
		if (isWidthSet) {
			return;
		}
		checkBox.setMaximumSize(new Dimension(width + 50, checkBox.getPreferredSize().height));
		checkBox.setPreferredSize(new Dimension(width, checkBox.getPreferredSize().height));
		isWidthSet = true;
	}

	@Override
	public int getWidth() {
		return checkBox.getFontMetrics(checkBox.getFont()).stringWidth(checkBox.getText());
	}

	public boolean isSelected() {
		return checkBox.isSelected();
	}

}