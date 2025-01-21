package com.bencodez.votingplugineditor.api.edit.rewards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.ItemEditor;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;

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
		frame.setSize(800, 900);
		frame.setLayout(new BorderLayout());

		JPanel panel = createMainPanel();
		frame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange());

		frame.add(saveButton, BorderLayout.SOUTH);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void openItemsGUIItem(String path, String name) {
		configData = updateData();
		new ItemEditor(
				(Map<String, Object>) PanelUtils.get(configData, path + "." + name, new HashMap<String, Object>())) {

			@Override
			public void saveChanges(Map<String, Object> changes) {
				for (Entry<String, Object> change : changes.entrySet()) {
					getChanges().put(path + "." + name + "." + change.getKey(), change.getValue());
				}
				if (!changes.isEmpty()) {
					saveChange();
				}
			}

			@Override
			public void removeItemPath(String subPath) {
				removePath(path + "." + name + "." + subPath);
				saveChange();
				configData = updateData();
			}
		};
	}

	private void openItemsGUI(String title, String path) {
		JFrame itemsFrame = new JFrame(title);
		itemsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		itemsFrame.setSize(600, 600);
		itemsFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(frame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				removePath(path + "." + name);
				frame.dispose();
				openItemsGUI(title, path);
			}

			@Override
			public void onItemAdd(String name) {
				changes.put(path + "." + name + ".Material", "STONE");
				changes.put(path + "." + name + ".Amount", 1);
				saveChange();
				itemsFrame.dispose();
				openItemsGUIItem(path, name);
			}

			@Override
			public void onItemSelect(String name) {
				openItemsGUIItem(path, name);
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, path, new HashMap<String, Object>());

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
			openItemsGUI("Edit Items", "Items");
		});

		panel.add(itemsButton);

		panel.add(PanelUtils.createSectionLabel("Requirements"));
		buttons.add(new IntSettingButton(panel, "Chance", configData, "Chance to give this entire reward", 0));
		buttons.add(new BooleanSettingButton(panel, "RequirePermission", configData, "Require Permission (Set below)"));
		buttons.add(new StringSettingButton(panel, "Permission", configData, "Permission (Enable above)", ""));

		panel.add(PanelUtils.createSectionLabel("Rewards"));

		// Add Money, EXP, and EXPLevels buttons side by side
		JPanel rewardsPanel = new JPanel();
		rewardsPanel.setLayout(new BoxLayout(rewardsPanel, BoxLayout.X_AXIS));
		rewardsPanel.add(createCollapsiblePanel("Money", "Money:", 0));
		rewardsPanel.add(createCollapsiblePanel("EXP", "EXP:", 0));
		rewardsPanel.add(createCollapsiblePanel("EXPLevels", "Exp Levels:", 0));

		panel.add(rewardsPanel);

		buttons.add(new StringListSettingButton(panel, "Commands", configData, "Commands (one per line, no /):"));
		buttons.add(new StringListSettingButton(panel, "RandomCommand", configData,
				"RandomCommand (Picks one command at random):"));

		// Add the new button for editing the title
		JButton editTitleButton = new JButton("Edit Title");
		editTitleButton.addActionListener(event -> {
			openTitleEditor();
		});

		panel.add(editTitleButton);

		// Add the new button for editing the action bar
		JButton editActionBarButton = new JButton("Edit Action Bar");
		editActionBarButton.addActionListener(event -> {
			openActionBarEditor();
		});

		panel.add(editActionBarButton);

		// RandomItem
		JButton itemsButton2 = new JButton("Edit Random Item (Only give one item)");
		itemsButton2.addActionListener(event -> {
			openItemsGUI("Edit RandomItem", "RandomItem");
		});

		panel.add(itemsButton2);

		buttons.add(new StringListSettingButton(panel, "Messages.Player", configData,
				"Messages to player (use %player%):"));

		return panel;
	}

	private void openActionBarEditor() {
		JFrame actionBarFrame = new JFrame("Edit Action Bar");
		actionBarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		actionBarFrame.setSize(600, 200);
		actionBarFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		buttons.add(new StringSettingButton(panel, "ActionBar.Message", configData, "Message", "&cRemember to vote"));
		buttons.add(new IntSettingButton(panel, "ActionBar.Delay", configData, "Delay", 30));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		actionBarFrame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange());

		actionBarFrame.add(saveButton, BorderLayout.SOUTH);

		actionBarFrame.setLocationRelativeTo(null);
		actionBarFrame.setVisible(true);
	}

	private void openTitleEditor() {
		JFrame titleFrame = new JFrame("Edit Title");
		titleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		titleFrame.setSize(600, 300);
		titleFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		buttons.add(new BooleanSettingButton(panel, "Title.Enabled", configData, "Enabled"));
		buttons.add(new StringSettingButton(panel, "Title.Title", configData, "Title", "&cRemember to vote!"));
		buttons.add(new StringSettingButton(panel, "Title.SubTitle", configData, "SubTitle", "&aType /vote"));
		buttons.add(new IntSettingButton(panel, "Title.FadeIn", configData, "FadeIn", 10));
		buttons.add(new IntSettingButton(panel, "Title.ShowTime", configData, "ShowTime", 50));
		buttons.add(new IntSettingButton(panel, "Title.FadeOut", configData, "FadeOut", 10));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		titleFrame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange());

		titleFrame.add(saveButton, BorderLayout.SOUTH);

		titleFrame.setLocationRelativeTo(null);
		titleFrame.setVisible(true);
	}

	private JPanel createCollapsiblePanel(String key, String label, int defaultValue) {
		JPanel collapsiblePanel = new JPanel();
		collapsiblePanel.setLayout(new BoxLayout(collapsiblePanel, BoxLayout.Y_AXIS));
		collapsiblePanel.setBorder(BorderFactory.createTitledBorder(label));

		IntSettingButton settingButton = new IntSettingButton(collapsiblePanel, key, configData, label, defaultValue);
		buttons.add(settingButton);

		collapsiblePanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton(label);
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> collapsiblePanel.setVisible(!collapsiblePanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(collapsiblePanel);

		return containerPanel;
	}

	public void saveChange() {
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
