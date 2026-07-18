package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

/**
 * Editor UI handler for VotingPlugin's {@code Config.yml}.
 * <p>
 * This class builds the Swing panels used by VotingPluginEditor to edit core plugin settings.
 * Updated for VotingPlugin 7.0+ layout (Database section, VoteReminders, VoteBroadcast).
 * </p>
 */
public class ConfigConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	public ConfigConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("Config.yml Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel mainPanel = createMainEditorPanel();
		tabbedPane.addTab("Main Settings", mainPanel);

		JPanel voteRemindingPanel = createVoteRemindersPanel();
		tabbedPane.addTab("Vote Reminders", voteRemindingPanel);

		JPanel formattingPanel = createFormattingPanel();
		tabbedPane.addTab("Formatting Settings", formattingPanel);

		JPanel topVoterSettingsPanel = createTopVoterSettingsPanel();
		tabbedPane.addTab("Top Voter Settings", topVoterSettingsPanel);

		frame.add(tabbedPane, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createMainEditorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new StringSettingButton(panel, "DebugLevel", getConfigData(), "Debug Level", "NONE",
				new String[] { "NONE", "INFO", "EXTRA" }));

		settingButtons.add(new StringSettingButton(panel, "DataStorage", getConfigData(), "Data Storage", "SQLITE",
				new String[] { "SQLITE", "MYSQL" }));

		panel.add(createDatabaseSettingsPanel());

		settingButtons.add(new BooleanSettingButton(panel, "AutoCreateVoteSites", getConfigData(), "Auto Create VoteSites"));
		settingButtons.add(new StringSettingButton(panel, "BedrockPlayerPrefix", getConfigData(), "Bedrock Player Prefix", "."));
		settingButtons.add(new BooleanSettingButton(panel, "OnlineMode", getConfigData(), "Online Mode"));
		settingButtons.add(new BooleanSettingButton(panel, "UseVoteGUIMainCommand", getConfigData(), "Use VoteGUI Main Command"));
		settingButtons.add(new BooleanSettingButton(panel, "CreateBackups", getConfigData(), "Create Backups"));
		settingButtons.add(new BooleanSettingButton(panel, "LoadCommandAliases", getConfigData(), "Load Command Aliases"));
		settingButtons.add(new IntSettingButton(panel, "PointsOnVote", getConfigData(), "Points On Vote", 0));
		settingButtons.add(new BooleanSettingButton(panel, "CaseInsensitiveYMLFiles", getConfigData(), "Case Insensitive YML Files"));
		settingButtons.add(new BooleanSettingButton(panel, "LimitMonthlyVotes", getConfigData(), "Limit Monthly Votes"));
		settingButtons.add(new BooleanSettingButton(panel, "TreatVanishAsOffline", getConfigData(), "Treat Vanish As Offline"));
		settingButtons.add(new StringSettingButton(panel, "DelayLoginEvent", getConfigData(), "Delay Login Event (e.g. 0s)", "0s"));
		settingButtons.add(new BooleanSettingButton(panel, "WaitUntilLoggedIn", getConfigData(), "Wait Until Logged In"));
		settingButtons.add(new BooleanSettingButton(panel, "PerSiteCoolDownEvents", getConfigData(), "Per-Site Cooldown Events"));
		settingButtons.add(new BooleanSettingButton(panel, "QueueVotesDuringTimeChange", getConfigData(), "Queue Votes During Time Change"));

		settingButtons.add(new BooleanSettingButton(panel, "AdvancedServiceSiteHandling", getConfigData(),
				"Advanced Service Site Handling"));
		settingButtons.add(new BooleanSettingButton(panel, "StoreMonthTotalsWithDate", getConfigData(),
				"Store Month Totals With Date"));
		settingButtons.add(new BooleanSettingButton(panel, "UseMonthDateTotalsAsPrimaryTotal", getConfigData(),
				"Use Month Date Totals As Primary Total"));
		settingButtons.add(new BooleanSettingButton(panel, "AllowUnjoined", getConfigData(), "AllowUnjoined"));
		settingButtons.add(
				new BooleanSettingButton(panel, "GiveDefaultPermission", getConfigData(), "Give Default Permission"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private Object getConfigData(String path) {
		return get(path);
	}

	private JPanel createFormattingPanel() {
		JPanel formattingPanel = new JPanel();
		formattingPanel.setLayout(new BoxLayout(formattingPanel, BoxLayout.Y_AXIS));
		formattingPanel.setBorder(BorderFactory.createTitledBorder("Formatting Settings"));

		ArrayList<SettingButton> localButtons = new ArrayList<SettingButton>();

		localButtons.add(new StringSettingButton(formattingPanel, "Format.HelpLine", getConfigData(), "Help Line", ""));
		localButtons.add(new StringSettingButton(formattingPanel, "Format.TimeFormat", getConfigData(), "Time Format", ""));
		localButtons.add(new StringSettingButton(formattingPanel, "Format.RewardTimeFormat", getConfigData(),
				"Reward Time Format", ""));

		formattingPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel broadcastPanel = new JPanel();
		broadcastPanel.setLayout(new BoxLayout(broadcastPanel, BoxLayout.Y_AXIS));
		broadcastPanel.setBorder(BorderFactory.createTitledBorder("Vote Broadcast"));

		localButtons.add(new StringSettingButton(broadcastPanel, "VoteBroadcast.Type", getConfigData(),
				"Type (EVERY_VOTE / LIST / etc.)", "EVERY_VOTE"));
		localButtons.add(new StringSettingButton(broadcastPanel, "VoteBroadcast.Duration", getConfigData(),
				"Duration (e.g. 2m)", "2m"));
		localButtons.add(new IntSettingButton(broadcastPanel, "VoteBroadcast.MaxSitesListed", getConfigData(),
				"Max Sites Listed (0 = all)", 0));

		localButtons.add(new StringSettingButton(broadcastPanel, "VoteBroadcast.Format.BroadcastMsg", getConfigData(),
				"Broadcast Message", ""));
		localButtons.add(new StringSettingButton(broadcastPanel, "VoteBroadcast.Format.Header", getConfigData(),
				"Header", ""));
		localButtons.add(new StringSettingButton(broadcastPanel, "VoteBroadcast.Format.ListLine", getConfigData(),
				"List Line", ""));

		formattingPanel.add(broadcastPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(localButtons);
		this.settingButtons.addAll(localButtons);

		return formattingPanel;
	}

	private JPanel createTopVoterSettingsPanel() {
		JPanel topVoterSettingsPanel = new JPanel();
		topVoterSettingsPanel.setLayout(new BoxLayout(topVoterSettingsPanel, BoxLayout.Y_AXIS));
		topVoterSettingsPanel.setBorder(BorderFactory.createTitledBorder("Top Voter Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "TopVoterIgnorePermission", getConfigData(),
				"Top Voter Ignore Permission"));
		settingButtons.add(new StringSettingButton(topVoterSettingsPanel, "VoteTopDefault", getConfigData(),
				"Vote Top Default", "Monthly", new String[] { "AllTime", "Monthly", "Weekly", "Daily" }));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "TopVoterAwardsTies", getConfigData(),
				"Top Voter Awards Ties"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "LoadTopVoter.AllTime", getConfigData(),
				"Load Top Voter AllTime"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "LoadTopVoter.Monthly", getConfigData(),
				"Load Top Voter Monthly"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "LoadTopVoter.Weekly", getConfigData(),
				"Load Top Voter Weekly"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "LoadTopVoter.Daily", getConfigData(),
				"Load Top Voter Daily"));
		settingButtons.add(new IntSettingButton(topVoterSettingsPanel, "MaxiumNumberOfTopVotersToLoad", getConfigData(),
				"Maximum Number Of Top Voters To Load", 1000));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "StoreTopVoters.Weekly", getConfigData(),
				"Store Top Voters Weekly"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "StoreTopVoters.Daily", getConfigData(),
				"Store Top Voters Daily"));
		settingButtons.add(new BooleanSettingButton(topVoterSettingsPanel, "LimitMonthlyVotes", getConfigData(),
				"Limit Monthly Votes"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		return topVoterSettingsPanel;
	}

	private JPanel createVoteRemindersPanel() {
		JPanel voteRemindingPanel = new JPanel();
		ArrayList<SettingButton> localButtons = new ArrayList<SettingButton>();

		voteRemindingPanel.setLayout(new BoxLayout(voteRemindingPanel, BoxLayout.Y_AXIS));
		voteRemindingPanel.setBorder(BorderFactory.createTitledBorder("Vote Reminder Settings"));

		localButtons.add(new BooleanSettingButton(voteRemindingPanel, "VoteReminderOptions.Enabled", getConfigData(),
				"Vote Reminders Enabled"));
		localButtons.add(new BooleanSettingButton(voteRemindingPanel, "VoteReminderOptions.StopAfterMatch", getConfigData(),
				"Stop After First Match"));
		localButtons.add(new StringSettingButton(voteRemindingPanel, "VoteReminderOptions.GlobalCooldown", getConfigData(),
				"Global Cooldown (e.g. 10m)", "10m"));
		localButtons.add(new IntSettingButton(voteRemindingPanel, "VoteReminderOptions.DefaultPriority", getConfigData(),
				"Default Priority", 0));

		voteRemindingPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel remindersPanel = new JPanel();
		remindersPanel.setLayout(new BoxLayout(remindersPanel, BoxLayout.Y_AXIS));
		remindersPanel.setBorder(BorderFactory.createTitledBorder("VoteReminders"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(remindersPanel.getWidth()) {
			@Override
			public void onItemRemove(String name) {
				remove("VoteReminders." + name);
				save();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("VoteReminders." + name + ".Type", "LOGIN");
				set("VoteReminders." + name + ".Priority", 50);
				set("VoteReminders." + name + ".Delay", "3s");
				set("VoteReminders." + name + ".Cooldown", "3m");
				set("VoteReminders." + name + ".Rewards.Messages.Player",
						"&aYou can vote! Sites left: &e%sitesavailable%&a.");
				save();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				JFrame reminderFrame = new JFrame("Editing VoteReminder - " + name);
				reminderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				reminderFrame.setSize(650, 650);
				reminderFrame.setLayout(new BorderLayout());

				JPanel settingsPanel = new JPanel();
				settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
				settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

				ArrayList<SettingButton> buttons = new ArrayList<SettingButton>();

				buttons.add(new StringSettingButton(settingsPanel, "VoteReminders." + name + ".Type", getConfigData(),
						"Type (LOGIN / FIRSTJOIN / INTERVAL / ANYSITE_COOLDOWN / ALLSITES_COOLDOWN)", "LOGIN"));
				buttons.add(new IntSettingButton(settingsPanel, "VoteReminders." + name + ".Priority", getConfigData(),
						"Priority", 0));
				buttons.add(new StringSettingButton(settingsPanel, "VoteReminders." + name + ".Delay", getConfigData(),
						"Delay (optional, e.g. 3s)", ""));
				buttons.add(new StringSettingButton(settingsPanel, "VoteReminders." + name + ".Interval", getConfigData(),
						"Interval (for INTERVAL, e.g. 30m)", ""));
				buttons.add(new StringSettingButton(settingsPanel, "VoteReminders." + name + ".Cooldown", getConfigData(),
						"Cooldown (e.g. 2h)", "0s"));

				settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				settingsPanel.add(addRewardsButton("VoteReminders." + name + ".Rewards", "Edit Rewards"));

				PanelUtils.adjustSettingButtonsMaxWidth(buttons);
				ConfigConfig.this.settingButtons.addAll(buttons);

				JButton saveButton = new JButton("Save and Apply Changes");
				saveButton.addActionListener(e -> saveChanges());

				reminderFrame.add(settingsPanel, BorderLayout.CENTER);
				reminderFrame.add(saveButton, BorderLayout.SOUTH);
				reminderFrame.setLocationRelativeTo(null);
				reminderFrame.setVisible(true);
			}
		};

		String[] list = PanelUtils
				.convertSetToArray(((Map<String, Object>) get("VoteReminders", new HashMap<String, Object>())).keySet());

		remindersPanel.add(addRemoveEditor.getAddButton("Add VoteReminder", "Add VoteReminder"));
		remindersPanel.add(addRemoveEditor.getRemoveButton("Remove VoteReminder", "Remove VoteReminder", list));
		remindersPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		addRemoveEditor.getOptionsButtons(remindersPanel, list);

		voteRemindingPanel.add(remindersPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(localButtons);
		this.settingButtons.addAll(localButtons);

		return voteRemindingPanel;
	}

	private JPanel createDatabaseSettingsPanel() {
		JPanel databasePanel = new JPanel();
		databasePanel.setLayout(new BoxLayout(databasePanel, BoxLayout.Y_AXIS));
		databasePanel.setBorder(BorderFactory.createTitledBorder("Database Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		settingButtons.add(new StringSettingButton(databasePanel, "Database.Host", getConfigData(), "Database Host", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "Database.Port", getConfigData(), "Database Port", 3306));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Database", getConfigData(),
				"Database Name", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Username", getConfigData(),
				"Database Username", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Password", getConfigData(),
				"Database Password", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "Database.MaxConnections", getConfigData(),
				"Maximum Connections", 1));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Prefix", getConfigData(),
				"Table Prefix", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Name", getConfigData(),
				"Table Name Override", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.DbType", getConfigData(),
				"Database Type", "MYSQL", new String[] { "MYSQL", "MARIADB", "POSTGRESQL" }));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		databasePanel.setVisible(false);

		JButton toggleButton = new JButton("Show/Hide Database Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.addActionListener(event -> databasePanel.setVisible(!databasePanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(databasePanel);

		return containerPanel;
	}

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> {
			new RewardEditor(getConfigData(path), path) {
				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							set(path + "." + change.getKey(), change.getValue());
						}
						save();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void removePath(String subPath) {
					remove(path + "." + subPath);
					save();
				}

				@Override
				public Map<String, Object> updateData() {
					return (Map<String, Object>) getConfigData(path);
				}

				@Override
				public String getVotingPluginDirectory() {
					return getPluginDirectory();
				}

				@Override
				public SFTPSettings getSFTPSetting() {
					return getSFTPSettings();
				}
			};
		});
		return rewardsEdit;
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : settingButtons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}
		}

		if (!changes.isEmpty()) {
			try {
				for (Entry<String, Object> change : changes.entrySet()) {
					set(change.getKey(), change.getValue());
				}
				save();
				JOptionPane.showMessageDialog(null, "Changes have been saved.");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to save changes.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "No changes detected.");
		}
	}
}
