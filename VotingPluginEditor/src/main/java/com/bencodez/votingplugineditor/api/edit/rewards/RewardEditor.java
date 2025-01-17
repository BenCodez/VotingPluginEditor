package com.bencodez.votingplugineditor.api.edit.rewards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.ItemEditor;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;

import lombok.Getter;

public abstract class RewardEditor {
	private JFrame frame;

	private Map<String, Object> configData; // Holds the initial config values
	@Getter
	private Map<String, Object> changes;

	private List<SettingButton> buttons;

	public RewardEditor(Map<String, Object> data) {
		configData = data;
		buttons = new ArrayList<SettingButton>();
		changes = new HashMap<String, Object>();
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		frame = new JFrame("Reward Editor");
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

	private void openItemsGUI(String name) {
		configData = updateData();
		new ItemEditor(
				(Map<String, Object>) PanelUtils.get(configData, "Items." + name, new HashMap<String, Object>())) {

			@Override
			public void saveChanges(Map<String, Object> changes) {
				for (Entry<String, Object> change : changes.entrySet()) {
					getChanges().put("Items." + name + "." + change.getKey(), change.getValue());
				}
				if (!changes.isEmpty()) {
					saveChanges();
				}
			}

			@Override
			public void removeItemPath(String path) {
				removePath("Items." + name + "." + path);
				saveChanges();
				configData = updateData();
			}
		};
	}

	private void openItemsGUI() {
		JFrame itemsFrame = new JFrame("Edit Items");
		itemsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		itemsFrame.setSize(600, 600);
		itemsFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor() {

			@Override
			public void onItemRemove(String name) {
				removePath("Items." + name);
				frame.dispose();
				openItemsGUI();
			}

			@Override
			public void onItemAdd(String name) {
				changes.put("Items." + name + ".Material", "STONE");
				changes.put("Items." + name + ".Amount", 1);
				saveChanges();
				itemsFrame.dispose();
				openItemsGUI(name);
			}

			@Override
			public void onItemSelect(String name) {
				openItemsGUI(name);
			}
		};

		Map<String, Object> map1 = null;
		if (configData.containsKey("Items")) {
			map1 = (Map<String, Object>) configData.get("Items");
		} else {
			map1 = new HashMap<String, Object>();
		}
		final Map<String, Object> map = map1;

		panel.add(addRemoveEditor.getAddButton("Add Item", "Add Item to reward"));
		panel.add(addRemoveEditor.getRemoveButton("Remove Item", "Remove Item",
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(panel, PanelUtils.convertSetToArray(map.keySet()));

		itemsFrame.add(panel);

		itemsFrame.setLocationRelativeTo(null);
		itemsFrame.setVisible(true);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JButton itemsButton = new JButton("Edit Items");
		itemsButton.addActionListener(event -> {
			openItemsGUI();
		});

		panel.add(itemsButton);

		buttons.add(new IntSettingButton(panel, "Money", configData, "Money:", 0));
		buttons.add(new IntSettingButton(panel, "EXP", configData, "EXP:", 0));
		buttons.add(new IntSettingButton(panel, "EXPLevels", configData, "Exp Levels:", 0));
		buttons.add(new IntSettingButton(panel, "Chance", configData, "Chance to give this entire reward", 0));

		buttons.add(new StringListSettingButton(panel, "Commands", configData, "Commands (one per line, no /):"));

		buttons.add(new StringListSettingButton(panel, "Messages.Player", configData, "Messages (use %player%):"));

		return panel;
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();
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
			
			configData = updateData();
		}
	}

	public abstract Map<String, Object> updateData();

	public abstract void removePath(String path);

	public abstract void saveChanges(Map<String, Object> changes);
}
