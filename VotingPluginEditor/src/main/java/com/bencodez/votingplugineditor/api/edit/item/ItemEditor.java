package com.bencodez.votingplugineditor.api.edit.item;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.VotingPluginEditor;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;

public abstract class ItemEditor {

	private JFrame frame;
	private Map<String, Object> configData;
	private List<SettingButton> buttons;
	private Map<String, Object> changes;

	public ItemEditor(Map<String, Object> data) {
		configData = data;
		buttons = new ArrayList<>();
		changes = new HashMap<String, Object>();

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

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChanges());

		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		buttons.add(new StringSettingButton(panel, "Material", configData, "Item material", "STONE",
				PanelUtils.convertListToArray(VotingPluginEditor.getMaterials())));
		// Setup SettingButtons
		buttons.add(new IntSettingButton(panel, "Amount", configData, "Amount:", 1));

		buttons.add(new StringListSettingButton(panel, "Lore", configData, "Lore (one per line):"));

		// buttons.add(new StringListSettingButton(panel, "Enchants", configData,
		// "Enchants (one per line):"));

		buttons.add(new IntSettingButton(panel, "CustomModelData", configData, "Custom Model Data:", 0));

		JPanel enchantsPanel = new JPanel();
		enchantsPanel.setLayout(new BoxLayout(enchantsPanel, BoxLayout.X_AXIS));

		AddRemoveEditor enchantsEditor = new AddRemoveEditor() {

			@Override
			public void onItemSelect(String name) {

			}

			@Override
			public void onItemRemove(String name) {
				removeItemPath("Enchants." + name);
				frame.dispose();
			}

			@Override
			public void onItemAdd(String name) {
				changes.put("Enchants." + name, 1);
				saveChanges();
				frame.dispose();
			}
		};

		Map<String, Object> enchantData = (Map<String, Object>) PanelUtils.get(configData, "Enchants",
				new HashMap<String, Object>());

		enchantsPanel.add(enchantsEditor.getAddButton("Add Enchant", "Add Enchant"));
		enchantsPanel.add(enchantsEditor.getRemoveButton("Remove Enchant", "Remove Enchant", enchantData.keySet()));

		panel.add(enchantsPanel);
		panel.add(PanelUtils.createSectionLabel("Edit Enchant (If any):"));

		enchantsEditor.getOptionsButtons(panel, PanelUtils.convertSetToArray(enchantData.keySet()), false);

		buttons.add(new IntSettingButton(panel, "MinAmount", configData, "Min Amount:", 0));
		buttons.add(new IntSettingButton(panel, "MaxAmount", configData, "Max Amount:", 0));
		buttons.add(new IntSettingButton(panel, "Chance", configData, "Chance (% Rewards Only):", 0));
		buttons.add(new IntSettingButton(panel, "Slot", configData, "Slot (Only works in GUI's):", 0));

		return panel;
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();

		// Check for changes
		for (SettingButton button : buttons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}
		}

		changes.putAll(this.changes);
		this.changes.clear();

		// Save changes if there are any
		if (!changes.isEmpty()) {
			saveChanges(changes);
		}
	}

	public abstract void removeItemPath(String path);

	public abstract void saveChanges(Map<String, Object> changes);
}
