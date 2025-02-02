package com.bencodez.votingplugineditor.api.edit.add;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bencodez.votingplugineditor.api.settng.SettingButton;

public abstract class AddEditor {

	private JFrame frame;
	private JTextField textField;
	private JComboBox<String> comboBox;
	private List<String> options;

	public AddEditor(String name) {

		// Create GUI
		createAndShowGUI(name);
	}

	public AddEditor(String name, List<String> options) {
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

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("Name:");
		addPanel.add(label);

		if (options != null && !options.isEmpty()) {
			comboBox = new JComboBox<>();
			comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));
			for (String option : options) {
				comboBox.addItem(option);
			}
			addPanel.add(comboBox);
		} else {
			textField = new JTextField("");
			textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
			addPanel.add(textField);
		}

		panel.add(addPanel);

		JButton addButton = new JButton("Click to Add");
		addButton.addActionListener(event -> {
			if (comboBox != null) {
				onAdd((String) comboBox.getSelectedItem());
			} else {
				onAdd(textField.getText());
			}
			frame.dispose();
		});
		panel.add(addButton);

		return panel;
	}

	public abstract void onAdd(String name);

}
