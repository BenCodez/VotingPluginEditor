package com.bencodez.votingplugineditor.rewards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bencodez.votingplugineditor.api.SettingButton;

public abstract class AddEditor {

	private JFrame frame;
	// private Map<String, Object> configData;
	private List<SettingButton> buttons;

	public AddEditor(String name) {
		buttons = new ArrayList<>();

		// Create GUI
		createAndShowGUI(name);
	}

	private void createAndShowGUI(String name) {
		frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());

		JPanel panel = createMainPanel();
		frame.add(panel, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JTextField textField;

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("Name:");
		addPanel.add(label);

		textField = new JTextField("");
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

		addPanel.add(textField);

		panel.add(addPanel);

		JButton addButton = new JButton("Click to Add");
		addButton.addActionListener(event -> {
			onAdd(textField.getText());
			frame.dispose();
		});
		panel.add(addButton);

		return panel;
	}

	public abstract void onAdd(String name);

	// public abstract void saveChanges(Map<String, Object> changes);
}
