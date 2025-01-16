package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import com.bencodez.votingplugineditor.YmlConfigHandler;

public class BungeeSettingsConfig extends YmlConfigHandler {
	public BungeeSettingsConfig(String filePath) {
		super(filePath);
	}

	@Override
	public void openEditorGUI() {
		JFrame editorFrame = new JFrame("Editing VoteSites - " + new File(filePath).getName());
		editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editorFrame.setSize(400, 300);
		editorFrame.setLayout(new BorderLayout());

		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}
}