package com.bencodez.votingplugineditor.api.edit.add;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class RemoveEditor {

	private JFrame frame;

	private String[] options;

	public RemoveEditor(String name, String[] options) {
		this.options = options;
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

	private JComboBox optionsBox;

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("Selected:");
		addPanel.add(label);

		optionsBox = new JComboBox<>(options);
		optionsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, optionsBox.getPreferredSize().height));

		addPanel.add(optionsBox);

		panel.add(addPanel);

		JButton addButton = new JButton("Click to remove");
		addButton.addActionListener(event -> {
			onRemove((String) optionsBox.getSelectedItem());
			frame.dispose();
		});
		panel.add(addButton);

		return panel;
	}

	public abstract void onRemove(String name);

	// public abstract void saveChanges(Map<String, Object> changes);
}
