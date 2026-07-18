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

import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

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
		tabbedPane.addTab("Main Settings", createMainEditorPanel());
		tabbedPane.addTab("Vote Reminders", createVoteReminderOptionsPanel());
		tabbedPane.addTab("Formatting Settings", createFormattingPanel());
		tabbedPane.addTab("Top Voter Settings", createTopVoterSettingsPanel());

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

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();
		settingButtons.add(new StringSettingButton(formattingPanel, "Format.HelpLine", getConfigData(), "Help Line",
				"&6%Command% - &6%HelpMessage%"));
		settingButtons.add(new StringSettingButton(formattingPanel, "Format.BroadcastMsg", getConfigData(),
				"Broadcast Message", "&6[&4Broadcast&6] &2Thanks &c%player% &2for voting on %SiteName%"));

		BooleanSettingButton onlyOneOfflineBroadcastButton = new BooleanSettingButton(formattingPanel,
				"Format.OnlyOneOfflineBroadcast", getConfigData(), "Only One Offline Broadcast");
		settingButtons.add(onlyOneOfflineBroadcastButton);

		StringSettingButton offlineBroadcastButton = new StringSettingButton(formattingPanel, "Format.OfflineBroadcast",
				getConfigData(), "Offline Broadcast",
				"&6[&4Broadcast&6] &2Thanks &c%player% &2for voting on %numberofvotes% times!");
		settingButtons.add(offlineBroadcastButton);
		settingButtons.add(new BooleanSettingButton(formattingPanel, "Format.BroadcastWhenOnline", getConfigData(),
				"Broadcast When Online"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		offlineBroadcastButton.setVisible(Boolean.TRUE.equals(getConfigData("Format.OnlyOneOfflineBroadcast")));
		onlyOneOfflineBroadcastButton
				.addActionListener(event -> offlineBroadcastButton.setVisible(onlyOneOfflineBroadcastButton.isSelected()));

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

	private JPanel createVoteReminderOptionsPanel() {
		JPanel reminderPanel = new JPanel();
		reminderPanel.setLayout(new BoxLayout(reminderPanel, BoxLayout.Y_AXIS));
		reminderPanel.setBorder(BorderFactory.createTitledBorder("Vote Reminder Options"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();
		settingButtons.add(new BooleanSettingButton(reminderPanel, "VoteReminderOptions.Enabled", getConfigData(),
				"Vote Reminders Enabled"));
		settingButtons.add(new BooleanSettingButton(reminderPanel, "VoteReminderOptions.StopAfterMatch", getConfigData(),
				"Stop After First Matching Reminder"));
		settingButtons.add(new StringSettingButton(reminderPanel, "VoteReminderOptions.GlobalCooldown", getConfigData(),
				"Global Reminder Cooldown", "10m"));
		settingButtons.add(new IntSettingButton(reminderPanel, "VoteReminderOptions.DefaultPriority", getConfigData(),
				"Default Reminder Priority", 0));
		settingButtons.add(new StringSettingButton(reminderPanel, "VoteReminderOptions.Defaults.Cooldown", getConfigData(),
				"Default Reminder Cooldown", "0"));
		settingButtons.add(new StringSettingButton(reminderPanel, "VoteReminderOptions.Defaults.Delay", getConfigData(),
				"Default Reminder Delay", "0"));

		reminderPanel.add(Box.createVerticalStrut(10));
		reminderPanel.add(addRewardsButton("VoteReminderOptions.Defaults.Rewards", "Edit Default Reminder Rewards"));
		reminderPanel.add(Box.createVerticalStrut(10));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);
		return reminderPanel;
	}

	private JPanel createDatabaseSettingsPanel() {
		JPanel databasePanel = new JPanel();
		databasePanel.setLayout(new BoxLayout(databasePanel, BoxLayout.Y_AXIS));
		databasePanel.setBorder(BorderFactory.createTitledBorder("Database Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Host", getConfigData(), "Database Host", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "Database.Port", getConfigData(), "Database Port", 3306));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Database", getConfigData(), "Database Name", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Username", getConfigData(),
				"Database Username", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Password", getConfigData(),
				"Database Password", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "Database.MaxConnections", getConfigData(),
				"Maximum Connections", 1));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Prefix", getConfigData(), "Table Prefix", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.Name", getConfigData(),
				"Table Name Override", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "Database.DbType", getConfigData(), "Database Type",
				"MYSQL", new String[] { "MYSQL", "MARIADB", "POSTGRESQL" }));

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
