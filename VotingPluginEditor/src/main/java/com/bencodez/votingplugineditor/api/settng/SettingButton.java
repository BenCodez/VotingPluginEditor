package com.bencodez.votingplugineditor.api.settng;

import java.awt.Component;

import javax.swing.JPanel;

public interface SettingButton {
	public String getKey();

	public boolean hasChanged();

	public Object getValue();

	public Component getComponent(JPanel panel);
	
	public Component getComponent();
	
	public void setVisible(boolean visible);

	public void updateValue();

	public void setMaxWidth(int width);

	public int getWidth();
}
