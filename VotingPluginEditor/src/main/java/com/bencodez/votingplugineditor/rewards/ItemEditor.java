package com.bencodez.votingplugineditor.rewards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.yaml.snakeyaml.Yaml;

public abstract class ItemEditor {
	
	private static Map<String, Object> initialState;
	private JFrame frame;
	private JTextField nameField;
	private JTextField materialField;
	private JTextField amountField;
	private JTextField minAmountField;
	private JTextField maxAmountField;
	private JTextField chanceField;
	private JTextArea loreArea;
	private JTextArea enchantsArea;
	private JCheckBox glowCheckbox;
	private JCheckBox checkLoreLengthCheckbox;
	private JTextField customModelDataField;

	private Map<String, Object> configData; // Map to hold the config values

	public ItemEditor(Map<String, Object> data) {
		configData = data;
		initialState = new HashMap<>(data);

		// Create GUI
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		frame = new JFrame("Item Editor");
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

		// Name
		nameField = createLabelAndTextField(panel, "Item Name:", getStringValue("Name", "New Item"));
		// Material
		materialField = createLabelAndTextField(panel, "Material:", getStringValue("Material", "DIAMOND"));
		// Amount
		amountField = createLabelAndTextField(panel, "Amount:", String.valueOf(getIntValue("Amount", 1)));
		// Min Amount
		minAmountField = createLabelAndTextField(panel, "Min Amount:", String.valueOf(getIntValue("MinAmount", 0)));
		// Max Amount
		maxAmountField = createLabelAndTextField(panel, "Max Amount:", String.valueOf(getIntValue("MaxAmount", 0)));
		// Chance
		chanceField = createLabelAndTextField(panel, "Chance (%):", String.valueOf(getIntValue("Chance", 0)));
		// Lore
		loreArea = createLabelAndTextArea(panel, "Lore (one per line):", getStringValue("Lore", ""));
		// Enchants
		enchantsArea = createLabelAndTextArea(panel, "Enchants (one per line):", getStringValue("Enchants", ""));
		// Glow
		glowCheckbox = new JCheckBox("Glow", getBooleanValue("Glow"));
		panel.add(glowCheckbox);
		// Check Lore Length
		checkLoreLengthCheckbox = new JCheckBox("Check Lore Length", getBooleanValue("CheckLoreLength"));
		panel.add(checkLoreLengthCheckbox);
		// Custom Model Data
		customModelDataField = createLabelAndTextField(panel, "Custom Model Data:",
				String.valueOf(getIntValue("CustomModelData", 0)));

		// Save Button
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveChanges());
		panel.add(saveButton);

		return panel;
	}

	private JTextField createLabelAndTextField(JPanel panel, String labelText, String initialValue) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));

		JTextField textField = new JTextField(initialValue);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

		subPanel.add(label);
		subPanel.add(textField);
		panel.add(subPanel);

		return textField;
	}

	private JTextArea createLabelAndTextArea(JPanel panel, String labelText, String initialValue) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		JTextArea textArea = new JTextArea(initialValue);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(20, 70));

		subPanel.add(label);
		subPanel.add(scrollPane);
		panel.add(subPanel);

		return textArea;
	}

	private boolean getBooleanValue(String key) {
		return Boolean.TRUE.equals(configData.get(key));
	}

	private String getStringValue(String key, String defaultValue) {
		return (String) configData.getOrDefault(key, defaultValue);
	}

	private int getIntValue(String key, int defaultValue) {
		Object value = configData.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return defaultValue;
	}

	private void saveChanges() {
	    Map<String, Object> currentState = new HashMap<>();
	    currentState.put("Name", nameField.getText());
	    currentState.put("Material", materialField.getText());
	    currentState.put("Amount", Integer.parseInt(amountField.getText()));
	    currentState.put("MinAmount", Integer.parseInt(minAmountField.getText()));
	    currentState.put("MaxAmount", Integer.parseInt(maxAmountField.getText()));
	    currentState.put("Chance", Integer.parseInt(chanceField.getText()));
	    currentState.put("Lore", loreArea.getText().split("\n")); // Assuming multiple lines represent different lore items
	    currentState.put("Enchants", enchantsArea.getText().split("\n")); // Enchant line items
	    currentState.put("Glow", glowCheckbox.isSelected());
	    currentState.put("CheckLoreLength", checkLoreLengthCheckbox.isSelected());
	    currentState.put("CustomModelData", Integer.parseInt(customModelDataField.getText()));

	    // Check for changes
	    Map<String, Object> changes = new HashMap<>();
	    for (String key : currentState.keySet()) {
	        // Check if initial state differs from current state for non-null values
	        if (!Objects.equals(initialState.get(key), currentState.get(key))) {
	            changes.put(key, currentState.get(key));
	        }
	    }

	    // Save changes if there are any
	    if (!changes.isEmpty()) {
	        try (FileWriter writer = new FileWriter("path/to/item.yml")) { // Specify the path to your item file
	            Yaml yaml = new Yaml();
	            yaml.dump(changes, writer);
	            JOptionPane.showMessageDialog(frame, "Changes have been saved.");
	            // Update the initial state after saving
	            initialState.putAll(changes);
	        } catch (IOException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(frame, "Failed to save changes.");
	        }
	    } else {
	        JOptionPane.showMessageDialog(frame, "No changes detected.");
	    }
	}
	
	public abstract void saveChanges(Map<String,Object> changes);
}
