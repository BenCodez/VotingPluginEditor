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
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.SFTPSettings;
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

	public abstract String getVotingPluginDirectory();

	public abstract SFTPSettings getSFTPSetting();

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
					RewardFilesConfig rewardFiles = new RewardFilesConfig(
							getVotingPluginDirectory() + File.separator + "Rewards" + File.separator + rewards.get(0)
									+ ".yml",
							rewards.get(0) + ".yml", false, getVotingPluginDirectory(), getSFTPSetting());

					for (Entry<String, Object> entry : rewardFiles.getConfigData().entrySet()) {
						changes.put(entry.getKey(), entry.getValue());
					}

					saveChange();
				} else {
					for (String reward : rewards) {
						RewardFilesConfig rewardFiles = new RewardFilesConfig(getVotingPluginDirectory()
								+ File.separator + "Rewards" + File.separator + reward + ".yml", reward + ".yml", false,
								getVotingPluginDirectory(), getSFTPSetting());
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

				@Override
				public String getVotingPluginDirectory1() {
					return getVotingPluginDirectory();
				}

				@Override
				protected SFTPSettings getSFTPSettings1() {
					return getSFTPSetting();
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

	private void openBossBarEditor() {
		JFrame bossBarFrame = new JFrame("Edit BossBar");
		bossBarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		bossBarFrame.setSize(600, 400);
		bossBarFrame.setLayout(new BorderLayout());

		JPanel bossBarPanel = new JPanel();
		bossBarPanel.setLayout(new BoxLayout(bossBarPanel, BoxLayout.Y_AXIS));
		bossBarPanel.setBorder(BorderFactory.createTitledBorder("BossBar"));

		buttons.add(new BooleanSettingButton(bossBarPanel, "BossBar.Enabled", configData, "Enabled"));
		buttons.add(new StringSettingButton(bossBarPanel, "BossBar.Message", configData, "Message", ""));
		buttons.add(new StringSettingButton(bossBarPanel, "BossBar.Color", configData, "Color", "BLUE"));
		buttons.add(new StringSettingButton(bossBarPanel, "BossBar.Style", configData, "Style", "SOLID"));
		buttons.add(new DoubleSettingButton(bossBarPanel, "BossBar.Progress", configData, "Progress", 0.50));
		buttons.add(new IntSettingButton(bossBarPanel, "BossBar.Delay", configData, "Delay", 30));

		bossBarFrame.add(bossBarPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(bossBarFrame));

		bossBarFrame.add(saveButton, BorderLayout.SOUTH);

		bossBarFrame.setLocationRelativeTo(null);
		bossBarFrame.setVisible(true);
	}

	private void openSoundEditor() {
		JFrame soundFrame = new JFrame("Edit Sound");
		soundFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		soundFrame.setSize(600, 400);
		soundFrame.setLayout(new BorderLayout());

		JPanel soundPanel = new JPanel();
		soundPanel.setLayout(new BoxLayout(soundPanel, BoxLayout.Y_AXIS));
		soundPanel.setBorder(BorderFactory.createTitledBorder("Sound"));

		buttons.add(new BooleanSettingButton(soundPanel, "Sound.Enabled", configData, "Enabled"));
		buttons.add(new StringSettingButton(soundPanel, "Sound.Sound", configData, "Sound", "BLOCK_ANVIL_USE"));
		buttons.add(new DoubleSettingButton(soundPanel, "Sound.Volume", configData, "Volume", 1.0));
		buttons.add(new DoubleSettingButton(soundPanel, "Sound.Pitch", configData, "Pitch", 1.0));

		soundFrame.add(soundPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(soundFrame));

		soundFrame.add(saveButton, BorderLayout.SOUTH);

		soundFrame.setLocationRelativeTo(null);
		soundFrame.setVisible(true);
	}

	private void openLocationDistanceEditor() {
		JFrame locationDistanceFrame = new JFrame("Edit LocationDistance");
		locationDistanceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		locationDistanceFrame.setSize(600, 400);
		locationDistanceFrame.setLayout(new BorderLayout());

		JPanel locationDistancePanel = new JPanel();
		locationDistancePanel.setLayout(new BoxLayout(locationDistancePanel, BoxLayout.Y_AXIS));
		locationDistancePanel.setBorder(BorderFactory.createTitledBorder("LocationDistance"));

		buttons.add(
				new StringSettingButton(locationDistancePanel, "LocationDistance.World", configData, "World", "world"));
		buttons.add(new IntSettingButton(locationDistancePanel, "LocationDistance.X", configData, "X", 0));
		buttons.add(new IntSettingButton(locationDistancePanel, "LocationDistance.Y", configData, "Y", 0));
		buttons.add(new IntSettingButton(locationDistancePanel, "LocationDistance.Z", configData, "Z", 0));
		buttons.add(
				new IntSettingButton(locationDistancePanel, "LocationDistance.Distance", configData, "Distance", 10));

		locationDistanceFrame.add(locationDistancePanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(locationDistanceFrame));

		locationDistanceFrame.add(saveButton, BorderLayout.SOUTH);

		locationDistanceFrame.setLocationRelativeTo(null);
		locationDistanceFrame.setVisible(true);
	}

	private void createAndShowGUI(String path) {
		frame = new JFrame("Reward Editor: " + path);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 900);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel requirementsPanel = createRequirementsPanel();
		JPanel rewardsPanel = createRewardsPanel();

		tabbedPane.addTab("Requirements", requirementsPanel);
		tabbedPane.addTab("Rewards", rewardsPanel);

		frame.add(tabbedPane, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(frame));

		frame.add(saveButton, BorderLayout.SOUTH);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createRequirementsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		panel.add(PanelUtils.createSectionLabel("Requirements"));
		buttons.add(new DoubleSettingButton(panel, "Chance", configData, "Chance to give this entire reward", 0));
		buttons.add(new StringSettingButton(panel, "JavascriptExpression", configData, "Javascript Expression", ""));

		BooleanSettingButton requirePermissionButton = new BooleanSettingButton(panel, "RequirePermission", configData,
				"Require Permission (Set after enabling below)");
		buttons.add(requirePermissionButton);

		StringSettingButton permissionButton = new StringSettingButton(panel, "Permission", configData, "Permission",
				"");
		buttons.add(permissionButton);

		permissionButton.setVisible(requirePermissionButton.getComponent().isSelected());

		requirePermissionButton.getComponent().addItemListener(e -> {
			boolean selected = e.getStateChange() == ItemEvent.SELECTED;
			permissionButton.setVisible(selected);
			panel.revalidate();
			panel.repaint();
		});

		BooleanSettingButton forceOfflineButton = new BooleanSettingButton(panel, "ForceOffline", configData,
				"Force Offline");
		buttons.add(forceOfflineButton);

		StringSettingButton rewardTypeButton = new StringSettingButton(panel, "RewardType", configData, "Reward Type",
				"BOTH", new String[] { "BOTH", "OFFLINE", "ONLINE" });
		buttons.add(rewardTypeButton);

		buttons.add(new IntSettingButton(panel, "RewardExpiration", configData, "Reward Expiration (minutes)", -1));

		panel.add(createWorldsPanel());

		panel.add(createServerPanel());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		JButton editLocationDistanceButton = new JButton("Edit LocationDistance");
		editLocationDistanceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editLocationDistanceButton.addActionListener(event -> openLocationDistanceEditor());
		buttonPanel.add(editLocationDistanceButton);

		JButton editDayOfMonthButton = new JButton("Edit DayOfMonth");
		editDayOfMonthButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editDayOfMonthButton.addActionListener(event -> openDayOfMonthEditor());
		buttonPanel.add(editDayOfMonthButton);

		JButton editVoteTotalButton = new JButton("Edit VoteTotal");
		editVoteTotalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editVoteTotalButton.addActionListener(event -> openVoteTotalEditor());
		buttonPanel.add(editVoteTotalButton);

		panel.add(buttonPanel);

		return panel;
	}

	private void openVoteTotalEditor() {
		JFrame voteTotalFrame = new JFrame("Edit VoteTotal");
		voteTotalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		voteTotalFrame.setSize(600, 400);
		voteTotalFrame.setLayout(new BorderLayout());

		JPanel voteTotalPanel = new JPanel();
		voteTotalPanel.setLayout(new BoxLayout(voteTotalPanel, BoxLayout.Y_AXIS));
		voteTotalPanel.setBorder(BorderFactory.createTitledBorder("VoteTotal"));

		buttons.add(new BooleanSettingButton(voteTotalPanel, "VoteTotal.AtleastMode", configData, "AtleastMode"));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.Daily", configData, "Daily", -1));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.Weekly", configData, "Weekly", -1));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.Monthly", configData, "Monthly", -1));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.AllTime", configData, "AllTime", -1));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.Points", configData, "Points", -1));
		buttons.add(new IntSettingButton(voteTotalPanel, "VoteTotal.MilestoneCount", configData, "MilestoneCount", -1));

		voteTotalFrame.add(voteTotalPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(voteTotalFrame));

		voteTotalFrame.add(saveButton, BorderLayout.SOUTH);

		voteTotalFrame.setLocationRelativeTo(null);
		voteTotalFrame.setVisible(true);
	}

	private JPanel createWorldsPanel() {
		JPanel worldsPanel = new JPanel();
		worldsPanel.setLayout(new BoxLayout(worldsPanel, BoxLayout.Y_AXIS));
		worldsPanel.setBorder(BorderFactory.createTitledBorder("Worlds Settings"));

		buttons.add(new StringListSettingButton(worldsPanel, "Worlds", configData, "Worlds"));
		buttons.add(new StringListSettingButton(worldsPanel, "BlackListedWorlds", configData, "BlackListed Worlds"));

		worldsPanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton("Worlds Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> worldsPanel.setVisible(!worldsPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(worldsPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		return containerPanel;
	}

	private JPanel createServerPanel() {
		JPanel serverPanel = new JPanel();
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
		serverPanel.setBorder(BorderFactory.createTitledBorder("Server Settings"));

		buttons.add(new StringSettingButton(serverPanel, "Server", configData, "Server", ""));
		buttons.add(new StringListSettingButton(serverPanel, "BlockedServers", configData, "Blocked Servers"));

		serverPanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton("Server Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.addActionListener(event -> serverPanel.setVisible(!serverPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(serverPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		return containerPanel;
	}

	private void openDayOfMonthEditor() {
		JFrame dayOfMonthFrame = new JFrame("Edit DayOfMonth");
		dayOfMonthFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dayOfMonthFrame.setSize(600, 400);
		dayOfMonthFrame.setLayout(new BorderLayout());

		JPanel dayOfMonthPanel = new JPanel();
		dayOfMonthPanel.setLayout(new BoxLayout(dayOfMonthPanel, BoxLayout.Y_AXIS));
		dayOfMonthPanel.setBorder(BorderFactory.createTitledBorder("DayOfMonth"));

		buttons.add(new BooleanSettingButton(dayOfMonthPanel, "DayOfMonth.Enabled", configData, "Enabled"));
		buttons.add(new StringListSettingButton(dayOfMonthPanel, "DayOfMonth.Days", configData, "Days"));

		dayOfMonthFrame.add(dayOfMonthPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(dayOfMonthFrame));

		dayOfMonthFrame.add(saveButton, BorderLayout.SOUTH);

		dayOfMonthFrame.setLocationRelativeTo(null);
		dayOfMonthFrame.setVisible(true);
	}

	private JPanel createRewardsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		panel.add(PanelUtils.createSectionLabel("Rewards"));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(createCollapsiblePanel(panel, "Money", "Money Settings",
				new String[] { "Money", "Money.Min", "Money.Max", "Money.Round" }));
		buttonPanel.add(
				createCollapsiblePanel(panel, "EXP", "EXP Settings", new String[] { "EXP", "EXP.Min", "EXP.Max" }));
		buttonPanel.add(createCollapsiblePanel(panel, "EXPLevels", "EXP Levels Settings",
				new String[] { "EXPLevels", "EXPLevels.Min", "EXPLevels.Max" }));

		panel.add(buttonPanel);

		JButton editCommandsButton = new JButton("Edit Commands");
		editCommandsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editCommandsButton.addActionListener(event -> openCommandsEditor());

		JPanel itemsPanel = new JPanel();
		itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.X_AXIS));

		JButton itemsButton = new JButton("Edit Items (Give all items)");
		itemsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		itemsButton.addActionListener(event -> openItemsGUI("Edit Items", "Items"));

		JButton itemsButton2 = new JButton("Edit Random Item (Only give one item)");
		itemsButton2.setAlignmentX(Component.CENTER_ALIGNMENT);
		itemsButton2.addActionListener(event -> openItemsGUI("Edit RandomItem", "RandomItem"));

		itemsPanel.add(editCommandsButton);
		itemsPanel.add(itemsButton);
		itemsPanel.add(itemsButton2);

		panel.add(itemsPanel);

		panel.add(Box.createRigidArea(new Dimension(0, 15)));

		JPanel editPanel = new JPanel();
		editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));

		JButton editTitleButton = new JButton("Edit Title");
		editTitleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editTitleButton.addActionListener(event -> openTitleEditor());
		editPanel.add(editTitleButton);

		JButton editActionBarButton = new JButton("Edit Action Bar");
		editActionBarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editActionBarButton.addActionListener(event -> openActionBarEditor());
		editPanel.add(editActionBarButton);

		JButton editMessagesButton = new JButton("Edit Messages");
		editMessagesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editMessagesButton.addActionListener(event -> openMessagesEditor());
		editPanel.add(editMessagesButton);

		panel.add(editPanel);

		JButton editAdvancedPriorityButton = new JButton(
				"Edit Advanced Priority (Give first possible reward from list)");
		editAdvancedPriorityButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editAdvancedPriorityButton.addActionListener(event -> openAdvancedEditor("AdvancedPriority"));

		JButton editAdvancedWorldButton = new JButton("Edit Advanced World (Give rewards based on world name)");
		editAdvancedWorldButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editAdvancedWorldButton.addActionListener(event -> openAdvancedEditor("AdvancedWorld"));

		JButton editAdvancedRewardsButton = new JButton("Edit Advanced Rewards (Multiple Sub Rewards)");
		editAdvancedRewardsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editAdvancedRewardsButton.addActionListener(event -> openAdvancedEditor("AdvancedRewards"));

		JButton editAdvancedRandomRewardButton = new JButton("Edit Advanced Random Reward (Give a random reward)");
		editAdvancedRandomRewardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editAdvancedRandomRewardButton.addActionListener(event -> openAdvancedEditor("AdvancedRandomReward"));

		JButton subRewardsButton = new JButton("Edit Rewards (Sub)");
		subRewardsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		subRewardsButton.addActionListener(event -> openSubEditor("Rewards"));

		JPanel centeredPanel = new JPanel();
		centeredPanel.setLayout(new BoxLayout(centeredPanel, BoxLayout.Y_AXIS));
		centeredPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		centeredPanel.add(editAdvancedPriorityButton);
		centeredPanel.add(editAdvancedWorldButton);
		centeredPanel.add(editAdvancedRewardsButton);
		centeredPanel.add(editAdvancedRandomRewardButton);
		centeredPanel.add(subRewardsButton);

		panel.add(centeredPanel);

		JPanel horizontalPanel = new JPanel();
		horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));

		JButton editPriorityButton = new JButton("Edit Priority");
		editPriorityButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editPriorityButton.addActionListener(event -> openRewardEditor("Priority"));
		horizontalPanel.add(editPriorityButton);

		JButton editJavascriptsButton = new JButton("Edit Javascripts");
		editJavascriptsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editJavascriptsButton.addActionListener(event -> openRewardEditor("Javascripts"));
		horizontalPanel.add(editJavascriptsButton);

		JButton editRandomRewardButton = new JButton("Edit RandomReward");
		editRandomRewardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editRandomRewardButton.addActionListener(event -> openRewardEditor("RandomReward"));
		horizontalPanel.add(editRandomRewardButton);

		panel.add(horizontalPanel);

		JPanel horizontalPanel2 = new JPanel();
		horizontalPanel2.setLayout(new BoxLayout(horizontalPanel2, BoxLayout.X_AXIS));

		JButton editBossBarButton = new JButton("Edit BossBar");
		editBossBarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editBossBarButton.addActionListener(event -> openBossBarEditor());
		horizontalPanel2.add(editBossBarButton);

		JButton editSoundButton = new JButton("Edit Sound");
		editSoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editSoundButton.addActionListener(event -> openSoundEditor());
		horizontalPanel2.add(editSoundButton);

		JButton editFireworkButton = new JButton("Edit Firework");
		editFireworkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editFireworkButton.addActionListener(event -> openFireworkEditor());
		horizontalPanel2.add(editFireworkButton);

		JButton editPotionsButton = new JButton("Edit Potions");
		editPotionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editPotionsButton.addActionListener(event -> openPotionsEditor());
		horizontalPanel2.add(editPotionsButton);

		panel.add(horizontalPanel2);

		JPanel horizontalPanel3 = new JPanel();
		horizontalPanel3.setLayout(new BoxLayout(horizontalPanel3, BoxLayout.X_AXIS));

		JButton editRandomCommandButton = new JButton("Edit RandomCommand");
		editRandomCommandButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editRandomCommandButton.addActionListener(event -> openRewardEditor("RandomCommand"));
		horizontalPanel3.add(editRandomCommandButton);

		JButton editSpecialChanceButton = new JButton("Edit Special Chance");
		editSpecialChanceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editSpecialChanceButton.addActionListener(event -> openSpecialChanceEditor());
		horizontalPanel3.add(editSpecialChanceButton);

		JButton editLuckyButton = new JButton("Edit Lucky");
		editLuckyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editLuckyButton.addActionListener(event -> openLuckyEditor());
		horizontalPanel3.add(editLuckyButton);

		JButton editJavaScriptButton = new JButton("Edit JavaScript Rewards");
		editJavaScriptButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editJavaScriptButton.addActionListener(event -> openJavaScriptEditor());
		horizontalPanel3.add(editJavaScriptButton);

		panel.add(horizontalPanel3);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);

		return panel;
	}

	private void openRewardEditor(String type) {
		JFrame rewardFrame = new JFrame("Edit " + type);
		rewardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		rewardFrame.setSize(600, 400);
		rewardFrame.setLayout(new BorderLayout());

		JPanel rewardPanel = new JPanel();
		rewardPanel.setLayout(new BoxLayout(rewardPanel, BoxLayout.Y_AXIS));
		rewardPanel.setBorder(BorderFactory.createTitledBorder(type));

		// Add settings buttons based on the type
		switch (type) {
		case "Priority":
			buttons.add(new StringListSettingButton(rewardPanel, "Priority", configData, "Priority"));
			break;
		case "Javascripts":
			buttons.add(new StringListSettingButton(rewardPanel, "Javascripts", configData, "Javascripts"));
			break;
		case "RandomReward":
			buttons.add(new StringListSettingButton(rewardPanel, "RandomReward", configData, "RandomReward"));
			break;
		case "RandomCommand":
			buttons.add(new StringListSettingButton(rewardPanel, "RandomCommand", configData, "RandomCommand"));
			break;
		}

		rewardFrame.add(rewardPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(rewardFrame));

		rewardFrame.add(saveButton, BorderLayout.SOUTH);

		rewardFrame.setLocationRelativeTo(null);
		rewardFrame.setVisible(true);
	}

	private void openPotionsEditor() {
		JFrame potionsFrame = new JFrame("Edit Potions");
		potionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		potionsFrame.setSize(600, 400);
		potionsFrame.setLayout(new BorderLayout());

		JPanel potionsPanel = new JPanel();
		potionsPanel.setLayout(new BoxLayout(potionsPanel, BoxLayout.Y_AXIS));
		potionsPanel.setBorder(BorderFactory.createTitledBorder("Potions"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(potionsFrame.getWidth()) {
			@Override
			public void onItemRemove(String name) {
				removePath("Potions." + name);
				potionsFrame.dispose();
				openPotionsEditor();
			}

			@Override
			public void onItemAdd(String name) {
				changes.put("Potions." + name + ".Duration", 100);
				changes.put("Potions." + name + ".Amplifier", 1);
				saveChange();
				potionsFrame.dispose();
				openPotionsEditor();
			}

			@Override
			public void onItemSelect(String name) {
				// Handle item selection if needed
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, "Potions",
				new HashMap<String, Object>());

		potionsPanel.add(
				addRemoveEditor.getAddButton("Add Potion", "Add new potion", VotingPluginEditor.getPotionEffects()));
		potionsPanel.add(addRemoveEditor.getRemoveButton("Remove Potion", "Remove potion",
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(potionsPanel, PanelUtils.convertSetToArray(map.keySet()));

		potionsFrame.add(potionsPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(potionsFrame));

		potionsFrame.add(saveButton, BorderLayout.SOUTH);

		potionsFrame.setLocationRelativeTo(null);
		potionsFrame.setVisible(true);
	}

	private void openLuckyEditor() {
		JFrame luckyFrame = new JFrame("Edit Lucky");
		luckyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		luckyFrame.setSize(600, 400);
		luckyFrame.setLayout(new BorderLayout());

		JPanel luckyPanel = new JPanel();
		luckyPanel.setLayout(new BoxLayout(luckyPanel, BoxLayout.Y_AXIS));
		luckyPanel.setBorder(BorderFactory.createTitledBorder("Lucky"));

		buttons.add(new BooleanSettingButton(luckyPanel, "OnlyOneLucky", configData, "OnlyOneLucky"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(luckyFrame.getWidth()) {
			@Override
			public void onItemRemove(String name) {
				removePath("Lucky." + name);
				luckyFrame.dispose();
				openLuckyEditor();
			}

			@Override
			public void onItemAdd(String name) {
				changes.put("Lucky." + name + ".Messages.Player", "You were lucky and received an extra reward!");
				changes.put("Lucky." + name + ".Money", 100);
				saveChange();
				luckyFrame.dispose();
				openLuckyEditor();
			}

			@Override
			public void onItemSelect(String name) {
				openSubEditor("Lucky." + name);
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, "Lucky",
				new HashMap<String, Object>());

		luckyPanel.add(addRemoveEditor.getAddButton("Add Lucky", "Add new lucky reward"));
		luckyPanel.add(addRemoveEditor.getRemoveButton("Remove Lucky", "Remove lucky reward",
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(luckyPanel, PanelUtils.convertSetToArray(map.keySet()));

		luckyFrame.add(luckyPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(luckyFrame));

		luckyFrame.add(saveButton, BorderLayout.SOUTH);

		luckyFrame.setLocationRelativeTo(null);
		luckyFrame.setVisible(true);
	}

	private void openFireworkEditor() {
		JFrame fireworkFrame = new JFrame("Edit Firework");
		fireworkFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		fireworkFrame.setSize(600, 400);
		fireworkFrame.setLayout(new BorderLayout());

		JPanel fireworkPanel = new JPanel();
		fireworkPanel.setLayout(new BoxLayout(fireworkPanel, BoxLayout.Y_AXIS));
		fireworkPanel.setBorder(BorderFactory.createTitledBorder("Firework"));

		buttons.add(new BooleanSettingButton(fireworkPanel, "Firework.Enabled", configData, "Enabled"));
		buttons.add(new IntSettingButton(fireworkPanel, "Firework.Power", configData, "Power", 2));
		buttons.add(new StringListSettingButton(fireworkPanel, "Firework.Colors", configData, "Colors"));
		buttons.add(new StringListSettingButton(fireworkPanel, "Firework.FadeOutColor", configData, "FadeOutColor"));
		buttons.add(new BooleanSettingButton(fireworkPanel, "Firework.Trail", configData, "Trail"));
		buttons.add(new BooleanSettingButton(fireworkPanel, "Firework.Flicker", configData, "Flicker"));
		buttons.add(new StringListSettingButton(fireworkPanel, "Firework.Types", configData, "Types"));
		buttons.add(new BooleanSettingButton(fireworkPanel, "Firework.Detonate", configData, "Detonate"));

		fireworkFrame.add(fireworkPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(fireworkFrame));

		fireworkFrame.add(saveButton, BorderLayout.SOUTH);

		fireworkFrame.setLocationRelativeTo(null);
		fireworkFrame.setVisible(true);
	}

	private void openWorldsEditor(String type) {
		JFrame worldsFrame = new JFrame("Edit " + type);
		worldsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		worldsFrame.setSize(600, 800);
		worldsFrame.setLayout(new BorderLayout());

		JPanel worldsPanel = new JPanel();
		worldsPanel.setLayout(new BoxLayout(worldsPanel, BoxLayout.Y_AXIS));
		worldsPanel.setBorder(BorderFactory.createTitledBorder(type));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(worldsFrame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				removePath(type + "." + name);
				worldsFrame.dispose();
				openWorldsEditor(type);
			}

			@Override
			public void onItemAdd(String name) {
				changes.put(type + "." + name, new ArrayList<String>());
				saveChange();
				worldsFrame.dispose();
				openWorldsEditor(type);
			}

			@Override
			public void onItemSelect(String name) {
				openSubEditor(type + "." + name);
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, type, new HashMap<String, Object>());

		worldsPanel.add(addRemoveEditor.getAddButton("Add " + type, "Add new " + type.toLowerCase()));
		worldsPanel.add(addRemoveEditor.getRemoveButton("Remove " + type, "Remove " + type.toLowerCase(),
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(worldsPanel, PanelUtils.convertSetToArray(map.keySet()));

		worldsFrame.add(worldsPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(worldsFrame));

		worldsFrame.add(saveButton, BorderLayout.SOUTH);

		worldsFrame.setLocationRelativeTo(null);
		worldsFrame.setVisible(true);
	}

	private void openSpecialChanceEditor() {
		JFrame specialChanceFrame = new JFrame("Edit Special Chance");
		specialChanceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		specialChanceFrame.setSize(600, 800);
		specialChanceFrame.setLayout(new BorderLayout());

		JPanel specialChancePanel = new JPanel();
		specialChancePanel.setLayout(new BoxLayout(specialChancePanel, BoxLayout.Y_AXIS));
		specialChancePanel.setBorder(BorderFactory.createTitledBorder("Special Chance"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(specialChanceFrame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				removePath("SpecialChance." + name);
				specialChanceFrame.dispose();
				openSpecialChanceEditor();
			}

			@Override
			public void onItemAdd(String name) {
				changes.put("SpecialChance." + name + ".Commands", new ArrayList<String>());
				saveChange();
				specialChanceFrame.dispose();
				openSpecialChanceEditor();
			}

			@Override
			public void onItemSelect(String name) {
				openSubEditor("SpecialChance." + name);
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, "SpecialChance",
				new HashMap<String, Object>());

		specialChancePanel.add(addRemoveEditor.getAddButton("Add Special Chance", "Add new special chance"));
		specialChancePanel.add(addRemoveEditor.getRemoveButton("Remove Special Chance", "Remove special chance",
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(specialChancePanel, PanelUtils.convertSetToArray(map.keySet()));

		specialChanceFrame.add(specialChancePanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(specialChanceFrame));

		specialChanceFrame.add(saveButton, BorderLayout.SOUTH);

		specialChanceFrame.setLocationRelativeTo(null);
		specialChanceFrame.setVisible(true);
	}

	private void openSubEditor(String path) {
		new SubRewardEditor(PanelUtils.get(configData, path, new HashMap<String, Object>()), path) {

			@Override
			public void saveChanges1(Map<String, Object> changes) {
				try {
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

			@Override
			public String getVotingPluginDirectory1() {
				return getVotingPluginDirectory();
			}

			@Override
			protected SFTPSettings getSFTPSettings1() {
				return getSFTPSetting();
			}
		};
	}

	private void openAdvancedEditor(String type) {
		JFrame advancedFrame = new JFrame("Edit " + type);
		advancedFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		advancedFrame.setSize(600, 800);
		advancedFrame.setLayout(new BorderLayout());

		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
		advancedPanel.setBorder(BorderFactory.createTitledBorder(type));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(advancedFrame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				removePath(type + "." + name);
				advancedFrame.dispose();
				openAdvancedEditor(type);
			}

			@Override
			public void onItemAdd(String name) {
				changes.put(type + "." + name + ".Commands", new ArrayList<String>());
				saveChange();
				advancedFrame.dispose();
				openAdvancedEditor(type);
			}

			@Override
			public void onItemSelect(String name) {
				openSubEditor(type + "." + name);
			}
		};

		Map<String, Object> map = (Map<String, Object>) PanelUtils.get(configData, type, new HashMap<String, Object>());

		advancedPanel.add(addRemoveEditor.getAddButton("Add " + type, "Add new " + type.toLowerCase()));
		advancedPanel.add(addRemoveEditor.getRemoveButton("Remove " + type, "Remove " + type.toLowerCase(),
				PanelUtils.convertSetToArray(map.keySet())));
		addRemoveEditor.getOptionsButtons(advancedPanel, PanelUtils.convertSetToArray(map.keySet()));

		advancedFrame.add(advancedPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChange(advancedFrame));

		advancedFrame.add(saveButton, BorderLayout.SOUTH);

		advancedFrame.setLocationRelativeTo(null);
		advancedFrame.setVisible(true);
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
