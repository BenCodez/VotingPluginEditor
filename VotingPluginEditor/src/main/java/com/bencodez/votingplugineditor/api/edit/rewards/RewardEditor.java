package com.bencodez.votingplugineditor.api.edit.rewards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.VotingPluginEditor;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.ItemEditor;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.DoubleSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.files.RewardFilesConfig;

import lombok.Getter;

public abstract class RewardEditor {
	private JFrame frame;

	private Map<String, Object> configData; // Holds the initial config values
	@Getter
	private Map<String, Object> changes;

	private List<SettingButton> buttons;

	private String path;

	@SuppressWarnings("unchecked")
	public RewardEditor(Object data, String path) {
		buttons = new ArrayList<SettingButton>();
		changes = new HashMap<String, Object>();
		if (data instanceof Map) {
			configData = (Map<String, Object>) data;
		} else if (data instanceof List) {
			configData = new HashMap<String, Object>();
			System.out.println("Data is a list");

			int confirmation = JOptionPane.showConfirmDialog(null,
					"In order to edit this reward, it must be converted to not use reward files.",
					"Would you like to convert this reward?", JOptionPane.YES_NO_OPTION);

			if (confirmation == JOptionPane.YES_OPTION) {
				ArrayList<String> rewards = (ArrayList<String>) data;
				if (rewards.size() == 0) {
					changes.put("", new HashMap<String, Object>());
					saveChange();
				} else if (rewards.size() == 1) {
					RewardFilesConfig rewardFiles = new RewardFilesConfig(VotingPluginEditor.directoryPath
							+ File.separator + "Rewards" + File.separator + rewards.get(0) + ".yml",
							rewards.get(0) + ".yml", false);

					for (Entry<String, Object> entry : rewardFiles.getConfigData().entrySet()) {
						changes.put(entry.getKey(), entry.getValue());
					}

					saveChange();
				} else {
					for (String reward : rewards) {
						RewardFilesConfig rewardFiles = new RewardFilesConfig(VotingPluginEditor.directoryPath
								+ File.separator + "Rewards" + File.separator + reward + ".yml", reward + ".yml",
								false);
						for (Entry<String, Object> entry : rewardFiles.getConfigData().entrySet()) {
							changes.put("AdvancedRewards." + reward + "." + entry.getKey(), entry.getValue());
						}

					}
					saveChange();
				}
			} else {
				System.out.println("User declined to convert reward");
				return;
			}

		}
		if (configData == null) {
			configData = new HashMap<String, Object>();
		}

		this.path = path;
		createAndShowGUI(path);
	}

	private void createAndShowGUI(String path) {
		frame = new JFrame("Reward Editor: " + path);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 900);
		frame.setLayout(new BorderLayout());

		JPanel panel = createMainPanel();
		frame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(frame));

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

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		String newPath = this.path;
		if (newPath.length() > 0) {
			newPath += ".";
		}
		newPath += path;
		final String path1 = newPath;
		rewardsEdit.addActionListener(event -> {
			new SubRewardEditor(PanelUtils.get(configData, path, new HashMap<String, Object>()), path1) {

				@Override
				public void saveChanges1(Map<String, Object> changes) {
					try {
						System.out.println("Saving " + path1);
						for (Entry<String, Object> change : changes.entrySet()) {
							getChanges().put(path + "." + change.getKey(), change.getValue());
						}
						saveChange();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void removePath1(String subPath) {
					removePath(path + "." + subPath);
				}

				@Override
				public Map<String, Object> updateData1() {
					return updateData();
				}
			};
		});
		return rewardsEdit;
	}

	private void openJavaScriptEditor() {
		JFrame javaScriptFrame = new JFrame("Edit JavaScript Rewards");
		javaScriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		javaScriptFrame.setSize(600, 400);
		javaScriptFrame.setLayout(new BorderLayout());

		JPanel javaScriptPanel = new JPanel();
		javaScriptPanel.setLayout(new BoxLayout(javaScriptPanel, BoxLayout.Y_AXIS));
		javaScriptPanel.setBorder(BorderFactory.createTitledBorder("JavaScript Rewards"));

		buttons.add(new BooleanSettingButton(javaScriptPanel, "Javascript.Enabled", configData, "Enabled"));
		buttons.add(new StringSettingButton(javaScriptPanel, "Javascript.Expression", configData, "Expression", ""));

		javaScriptPanel.add(addRewardsButton("Javascript.TrueRewards", "Edit True Rewards"));

		javaScriptPanel.add(addRewardsButton("Javascript.FalseRewards", "Edit False Rewards"));

		buttons.add(new StringListSettingButton(javaScriptPanel, "Javascripts", configData, "Javascripts"));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		javaScriptFrame.add(javaScriptPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(javaScriptFrame));

		javaScriptFrame.add(saveButton, BorderLayout.SOUTH);

		javaScriptFrame.setLocationRelativeTo(null);
		javaScriptFrame.setVisible(true);
	}

	private void openItemsGUI(String title, String path) {
		JFrame itemsFrame = new JFrame(title);
		itemsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		itemsFrame.setSize(600, 600);
		itemsFrame.setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		if (path.equalsIgnoreCase("Items")) {
			buttons.add(new BooleanSettingButton(top, "OnlyOneItemChance", configData, "OnlyOneItemChance"));
			top.add(Box.createRigidArea(new Dimension(0, 15)));
		}

		itemsFrame.add(top, BorderLayout.NORTH);

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

		JPanel timedPanel = new JPanel();
		timedPanel.setLayout(new BoxLayout(timedPanel, BoxLayout.X_AXIS));
		timedPanel.add(createDelayedPanel());
		timedPanel.add(createTimedPanel());
		panel.add(timedPanel);

		panel.add(PanelUtils.createSectionLabel("Requirements"));
		buttons.add(new DoubleSettingButton(panel, "Chance", configData, "Chance to give this entire reward", 0));

		buttons.add(new StringSettingButton(panel, "JavascriptExpression", configData, "Javascript Expression", ""));

		// Add the "RequirePermission" setting button
		BooleanSettingButton requirePermissionButton = new BooleanSettingButton(panel, "RequirePermission", configData,
				"Require Permission (Set after enabling below)");
		buttons.add(requirePermissionButton);

		// Add the "Permission" setting button
		StringSettingButton permissionButton = new StringSettingButton(panel, "Permission", configData, "Permission",
				"");
		buttons.add(permissionButton);

		// Initially hide the "Permission" line
		permissionButton.setVisible(requirePermissionButton.getComponent().isSelected());

		// Add an ItemListener to the "RequirePermission" checkbox
		requirePermissionButton.getComponent().addItemListener(e -> {
			boolean selected = e.getStateChange() == ItemEvent.SELECTED;
			permissionButton.setVisible(selected);
			panel.revalidate();
			panel.repaint();
		});

		panel.add(PanelUtils.createSectionLabel("Rewards"));

		JPanel buttonPanel = new JPanel();

		panel.add(buttonPanel);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		// Add Money, EXP, and EXPLevels settings with toggle buttons
		buttonPanel.add(createCollapsiblePanel(panel, "Money", "Money Settings",
				new String[] { "Money", "Money.Min", "Money.Max", "Money.Round" }));
		buttonPanel.add(
				createCollapsiblePanel(panel, "EXP", "EXP Settings", new String[] { "EXP", "EXP.Min", "EXP.Max" }));
		buttonPanel.add(createCollapsiblePanel(panel, "EXPLevels", "EXP Levels Settings",
				new String[] { "EXPLevels", "EXPLevels.Min", "EXPLevels.Max" }));

		panel.add(Box.createRigidArea(new Dimension(0, 15)));

		JButton editCommandsButton = new JButton("Edit Commands");
		editCommandsButton.addActionListener(event -> openCommandsEditor());

		JPanel itemsPanel = new JPanel();
		itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.X_AXIS));

		JButton itemsButton = new JButton("Edit Items (Give all items)");
		itemsButton.addActionListener(event -> {
			openItemsGUI("Edit Items", "Items");
		});

		// RandomItem
		JButton itemsButton2 = new JButton("Edit Random Item (Only give one item)");
		itemsButton2.addActionListener(event -> {
			openItemsGUI("Edit RandomItem", "RandomItem");
		});

		itemsPanel.add(editCommandsButton);
		itemsPanel.add(itemsButton);
		itemsPanel.add(itemsButton2);

		panel.add(itemsPanel, BorderLayout.CENTER);

		panel.add(Box.createRigidArea(new Dimension(0, 15)));

		JPanel editPanel = new JPanel();
		editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));

		// Add the new button for editing the title
		JButton editTitleButton = new JButton("Edit Title");
		editTitleButton.addActionListener(event -> {
			openTitleEditor();
		});
		editPanel.add(editTitleButton);

		// Add the new button for editing the action bar
		JButton editActionBarButton = new JButton("Edit Action Bar");
		editActionBarButton.addActionListener(event -> {
			openActionBarEditor();
		});
		editPanel.add(editActionBarButton);

		JButton editMessagesButton = new JButton("Edit Messages");
		editMessagesButton.addActionListener(event -> openMessagesEditor());
		editPanel.add(editMessagesButton);

		panel.add(editPanel);

		JButton editJavaScriptButton = new JButton("Edit JavaScript Rewards");
		editJavaScriptButton.addActionListener(event -> openJavaScriptEditor());
		panel.add(editJavaScriptButton);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		return panel;
	}

	private void openMessagesEditor() {
		JFrame messagesFrame = new JFrame("Edit Messages");
		messagesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		messagesFrame.setSize(600, 400);
		messagesFrame.setLayout(new BorderLayout());

		JPanel messagesPanel = new JPanel();
		messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
		messagesPanel.setBorder(BorderFactory.createTitledBorder("Messages"));

		buttons.add(new StringListSettingButton(messagesPanel, "Messages.Player", configData,
				"Messages to player (use %player%):"));

		buttons.add(new StringListSettingButton(messagesPanel, "Messages.Broadcast", configData,
				"Messages to broadcast (use %player%):"));

		buttons.add(new StringListSettingButton(messagesPanel, "Message", configData,
				"Messages to player (use %player%):"));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		messagesFrame.add(messagesPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(messagesFrame));

		messagesFrame.add(saveButton, BorderLayout.SOUTH);

		messagesFrame.setLocationRelativeTo(null);
		messagesFrame.setVisible(true);
	}

	private void openCommandsEditor() {
		JFrame commandsFrame = new JFrame("Edit Commands");
		commandsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		commandsFrame.setSize(600, 800);
		commandsFrame.setLayout(new BorderLayout());

		JPanel commandsPanel = new JPanel();
		commandsPanel.setLayout(new BoxLayout(commandsPanel, BoxLayout.Y_AXIS));
		commandsPanel.setBorder(BorderFactory.createTitledBorder("Commands"));

		commandsPanel.add(PanelUtils.createSectionLabel("One per line, no /"));
		buttons.add(new StringListSettingButton(commandsPanel, "Commands", configData,
				"Commands (Run as console, same as below):"));

		buttons.add(new StringListSettingButton(commandsPanel, "Commands.Console", configData,
				"Commands.Console (Use above when possible):"));

		buttons.add(new StringListSettingButton(commandsPanel, "Commands.Player", configData,
				"Commands.Player (Make player run command (one per line, no /):"));

		buttons.add(new BooleanSettingButton(commandsPanel, "Commands.Stagger", configData,
				"Commands.Stagger (Delays commands by a tick)", true));

		buttons.add(new StringListSettingButton(commandsPanel, "RandomCommand", configData,
				"RandomCommand (Picks one command at random):"));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		commandsFrame.add(commandsPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(commandsFrame));

		commandsFrame.add(saveButton, BorderLayout.SOUTH);

		commandsFrame.setLocationRelativeTo(null);
		commandsFrame.setVisible(true);
	}

	private JPanel createDelayedPanel() {
		JPanel delayedPanel = new JPanel();
		delayedPanel.setLayout(new BoxLayout(delayedPanel, BoxLayout.Y_AXIS));
		delayedPanel.setBorder(BorderFactory.createTitledBorder("Delayed Settings"));

		ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();

		buttons.add(new BooleanSettingButton(delayedPanel, "Delayed.Enabled", configData, "Enabled"));
		buttons.add(new IntSettingButton(delayedPanel, "Delayed.Hours", configData, "Hours", 0));
		buttons.add(new IntSettingButton(delayedPanel, "Delayed.Minutes", configData, "Minutes", 0));
		buttons.add(new IntSettingButton(delayedPanel, "Delayed.Seconds", configData, "Seconds", 0));
		buttons.add(new IntSettingButton(delayedPanel, "Delayed.MilliSeconds", configData, "MilliSeconds", 0));

		delayedPanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton("Delayed Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> delayedPanel.setVisible(!delayedPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(delayedPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);
		this.buttons.addAll(buttons);

		return containerPanel;
	}

	private JPanel createTimedPanel() {
		JPanel timedPanel = new JPanel();
		timedPanel.setLayout(new BoxLayout(timedPanel, BoxLayout.Y_AXIS));
		timedPanel.setBorder(BorderFactory.createTitledBorder("Timed Settings"));

		ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();

		buttons.add(new BooleanSettingButton(timedPanel, "Timed.Enabled", configData, "Enabled"));
		buttons.add(new IntSettingButton(timedPanel, "Timed.Hour", configData, "Hour", 0));
		buttons.add(new IntSettingButton(timedPanel, "Timed.Minute", configData, "Minute", 0));

		timedPanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton("Timed Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> timedPanel.setVisible(!timedPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(timedPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);
		this.buttons.addAll(buttons);

		return containerPanel;
	}

	private JButton createCollapsiblePanel(JPanel mainPanel, String key, String label, String[] subKeys) {
		JPanel collapsiblePanel = new JPanel();
		collapsiblePanel.setLayout(new BoxLayout(collapsiblePanel, BoxLayout.Y_AXIS));
		collapsiblePanel.setBorder(BorderFactory.createTitledBorder(label));

		ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();
		for (String subKey : subKeys) {
			String[] parts = subKey.split("\\.");
			String label1 = parts.length > 1 ? key + "." + parts[1] : subKey;
			if (subKey.endsWith(".Round")) {
				buttons.add(new BooleanSettingButton(collapsiblePanel, key + "." + subKey, configData, label1));
			} else {
				buttons.add(new IntSettingButton(collapsiblePanel, key + "." + subKey, configData, label1, 0));
			}
		}
		PanelUtils.adjustSettingButtonsMaxWidth(buttons);
		this.buttons.addAll(buttons);

		collapsiblePanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton(label);
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> collapsiblePanel.setVisible(!collapsiblePanel.isVisible()));

		mainPanel.add(collapsiblePanel);

		return toggleButton;
	}

	private void openActionBarEditor() {
		JFrame actionBarFrame = new JFrame("Edit Action Bar");
		actionBarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		actionBarFrame.setSize(600, 200);
		actionBarFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();

		buttons.add(new StringSettingButton(panel, "ActionBar.Message", configData, "Message", "&cRemember to vote"));
		buttons.add(new IntSettingButton(panel, "ActionBar.Delay", configData, "Delay", 30));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		this.buttons.addAll(buttons);

		actionBarFrame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(actionBarFrame));

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

		ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();

		buttons.add(new BooleanSettingButton(panel, "Title.Enabled", configData, "Enabled"));
		buttons.add(new StringSettingButton(panel, "Title.Title", configData, "Title", "&cRemember to vote!"));
		buttons.add(new StringSettingButton(panel, "Title.SubTitle", configData, "SubTitle", "&aType /vote"));
		buttons.add(new IntSettingButton(panel, "Title.FadeIn", configData, "FadeIn", 10));
		buttons.add(new IntSettingButton(panel, "Title.ShowTime", configData, "ShowTime", 50));
		buttons.add(new IntSettingButton(panel, "Title.FadeOut", configData, "FadeOut", 10));

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		this.buttons.addAll(buttons);

		titleFrame.add(panel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(titleFrame));

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
		saveChange(null);
	}

	public void saveChange(JFrame frame) {
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
			if (frame != null) {
				frame.dispose();
			}
		}
	}

	public abstract Map<String, Object> updateData();

	public abstract void removePath(String path);

	public void removePath1(String path) {
		removePath(path);
	}

	public abstract void saveChanges(Map<String, Object> changes);
}
