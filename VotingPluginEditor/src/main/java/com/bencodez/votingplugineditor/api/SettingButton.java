package com.bencodez.votingplugineditor.api;

import java.awt.Component;

import javax.swing.JPanel;

public interface SettingButton {
	public String getKey();
	
	public boolean hasChanged();
	
	public Object getValue();
	
	public Component getComponent(JPanel panel);
}
