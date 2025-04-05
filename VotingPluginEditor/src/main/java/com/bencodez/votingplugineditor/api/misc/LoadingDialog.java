package com.bencodez.votingplugineditor.api.misc;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoadingDialog extends JDialog {
	public LoadingDialog(JFrame parent) {
		super(parent, "Loading", true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Loading, please wait..."), BorderLayout.CENTER);
		getContentPane().add(panel);
		setUndecorated(true);
		setSize(200, 100);
		setLocationRelativeTo(parent);
	}
}
