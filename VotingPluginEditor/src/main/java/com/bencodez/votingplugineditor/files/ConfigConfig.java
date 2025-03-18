
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

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
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

		JPanel mainPanel = createMainEditorPanel();
		tabbedPane.addTab("Main Settings", mainPanel);

		JPanel voteRemindingPanel = createVoteRemindingPanel();
		tabbedPane.addTab("Vote Reminding", voteRemindingPanel);

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

		panel.add(createMySQLSettingsPanel());

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

		// Add your formatting settings here
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

		// Add more settings as needed

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		// Initially set the visibility of OfflineBroadcast based on
		// OnlyOneOfflineBroadcast
		offlineBroadcastButton.setVisible((Boolean) getConfigData("Format.OnlyOneOfflineBroadcast"));

		// Add action listener to toggle visibility of OfflineBroadcast
		onlyOneOfflineBroadcastButton.addActionListener(event -> {
			offlineBroadcastButton.setVisible(onlyOneOfflineBroadcastButton.isSelected());
		});

		return formattingPanel;
	}

	private JPanel createTopVoterSettingsPanel() {
		JPanel topVoterSettingsPanel = new JPanel();
		topVoterSettingsPanel.setLayout(new BoxLayout(topVoterSettingsPanel, BoxLayout.Y_AXIS));
		topVoterSettingsPanel.setBorder(BorderFactory.createTitledBorder("Top Voter Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		// Add your top voter settings here
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

	private JPanel createVoteRemindingPanel() {
		JPanel voteRemindingPanel = new JPanel();

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		voteRemindingPanel.setLayout(new BoxLayout(voteRemindingPanel, BoxLayout.Y_AXIS));
		voteRemindingPanel.setBorder(BorderFactory.createTitledBorder("Vote Reminding Settings"));

		settingButtons.add(new BooleanSettingButton(voteRemindingPanel, "VoteReminding.Enabled", getConfigData(),
				"Vote Reminding Enabled"));
		settingButtons.add(new BooleanSettingButton(voteRemindingPanel, "VoteReminding.RemindOnLogin", getConfigData(),
				"Vote Reminding On Login"));
		settingButtons.add(new BooleanSettingButton(voteRemindingPanel, "VoteReminding.RemindOnlyOnce", getConfigData(),
				"Vote Reminding Only Once"));
		settingButtons.add(new IntSettingButton(voteRemindingPanel, "VoteReminding.RemindDelay", getConfigData(),
				"Vote Reminding Delay", 30));

		voteRemindingPanel.add(Box.createVerticalStrut(10)); // Spacer
		voteRemindingPanel.add(addRewardsButton("VoteReminding.Rewards", "Edit Vote Reminding Rewards"));
		voteRemindingPanel.add(Box.createVerticalStrut(10)); // Spacer

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		return voteRemindingPanel;
	}

	private JPanel createMySQLSettingsPanel() {
		JPanel mysqlPanel = new JPanel();
		mysqlPanel.setLayout(new BoxLayout(mysqlPanel, BoxLayout.Y_AXIS));
		mysqlPanel.setBorder(BorderFactory.createTitledBorder("MySQL Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		settingButtons
				.add(new StringSettingButton(mysqlPanel, "MySQL.Host", getConfigData(), "MySQL Host", "192.168.0.156"));
		settingButtons.add(new IntSettingButton(mysqlPanel, "MySQL.Port", getConfigData(), "MySQL Port", 3306));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "MySQL.Database", getConfigData(), "MySQL Database", "db"));
		settingButtons.add(
				new StringSettingButton(mysqlPanel, "MySQL.Password", getConfigData(), "MySQL Password", "mD9!Zui9GH"));
		settingButtons.add(
				new IntSettingButton(mysqlPanel, "MySQL.MaxConnections", getConfigData(), "MySQL Max Connections", 1));
		settingButtons.add(new StringSettingButton(mysqlPanel, "MySQL.Prefix", getConfigData(), "MySQL Prefix", ""));
		settingButtons.add(
				new StringSettingButton(mysqlPanel, "MySQL.Name", getConfigData(), "MySQL Name", "VotingPlugin_Users"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);

		this.settingButtons.addAll(settingButtons);

		mysqlPanel.setVisible(false); // Initially hide the panel

		JButton toggleButton = new JButton("Show/Hide MySQL Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.addActionListener(event -> mysqlPanel.setVisible(!mysqlPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(mysqlPanel);

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

		// Notify & save changes
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
