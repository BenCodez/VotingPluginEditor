package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

public class SpecialRewardsConfig extends YmlConfigHandler {
	private final ArrayList<SettingButton> settingButtons;

	public SpecialRewardsConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		settingButtons = new ArrayList<SettingButton>();
	}

	JFrame editorFrame;

	@Override
	public void openEditorGUI() {
		editorFrame = new JFrame("Editing SpecialRewards.yml - " + new File(filePath).getName());
		editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editorFrame.setSize(800, 900);
		editorFrame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel rewardsPanel = new JPanel();
		rewardsPanel.setLayout(new BoxLayout(rewardsPanel, BoxLayout.Y_AXIS));
		rewardsPanel.add(addRewardsButton("FirstVote", "FirstVote Rewards"));
		rewardsPanel.add(addRewardsButton("FirstVoteToday", "FirstVoteToday Rewards"));
		rewardsPanel.add(addRewardsButton("AllSites", "AllSites Rewards"));
		rewardsPanel.add(addRewardsButton("AlmostAllSites", "AlmostAllSites Rewards"));
		rewardsPanel.add(addRewardsButton("AnySiteRewards", "Any Site Rewards"));
		rewardsPanel.add(addRewardsButton("LoginRewards", "Login Rewards"));
		rewardsPanel.add(addRewardsButton("LogoutRewards", "Logout Rewards"));
		rewardsPanel.add(addRewardsButton("VoteCoolDownEndedReward", "Vote Cool Down Ended Reward"));

		tabbedPane.addTab("Rewards", rewardsPanel);
		tabbedPane.addTab("VoteParty", createVotePartyPanel());
		tabbedPane.addTab("Cumulative Rewards", createCumulativeRewardsPanel());
		tabbedPane.addTab("Milestones", createMilestonesPanel());
		tabbedPane.addTab("Vote Streak", createVoteStreakPanel());
		tabbedPane.addTab("TopVoter Awards", createAwardsPanel());
		tabbedPane.addTab("Extra Options", createExtraOptionsPanel());

		editorFrame.add(tabbedPane, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		editorFrame.add(saveButton, BorderLayout.SOUTH);

		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}

	public JPanel createAwardsPanel() {
		JPanel awardsPanel = new JPanel();
		awardsPanel.setLayout(new BoxLayout(awardsPanel, BoxLayout.Y_AXIS));

		JButton monthlyAwardsButton = new JButton("Monthly Awards");
		monthlyAwardsButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, monthlyAwardsButton.getPreferredSize().height));
		monthlyAwardsButton.addActionListener(e -> {
			JFrame monthlyFrame = new JFrame("Monthly Awards");
			monthlyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			monthlyFrame.setSize(600, 700);
			monthlyFrame.add(createMonthlyAwardsPanel());
			monthlyFrame.setLocationRelativeTo(null);
			monthlyFrame.setVisible(true);
		});

		JButton weeklyAwardsButton = new JButton("Weekly Awards");
		weeklyAwardsButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, weeklyAwardsButton.getPreferredSize().height));
		weeklyAwardsButton.addActionListener(e -> {
			JFrame weeklyFrame = new JFrame("Weekly Awards");
			weeklyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			weeklyFrame.setSize(600, 700);
			weeklyFrame.add(createWeeklyAwardsPanel());
			weeklyFrame.setLocationRelativeTo(null);
			weeklyFrame.setVisible(true);
		});

		JButton dailyAwardsButton = new JButton("Daily Awards");
		dailyAwardsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, dailyAwardsButton.getPreferredSize().height));
		dailyAwardsButton.addActionListener(e -> {
			JFrame dailyFrame = new JFrame("Daily Awards");
			dailyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dailyFrame.setSize(600, 700);
			dailyFrame.add(createDailyAwardsPanel());
			dailyFrame.setLocationRelativeTo(null);
			dailyFrame.setVisible(true);
		});

		awardsPanel.add(monthlyAwardsButton);
		awardsPanel.add(weeklyAwardsButton);
		awardsPanel.add(dailyAwardsButton);

		return awardsPanel;
	}

	public JPanel createVoteStreakPanel() {
		JPanel voteStreakPanel = new JPanel();
		voteStreakPanel.setLayout(new BoxLayout(voteStreakPanel, BoxLayout.Y_AXIS));
		voteStreakPanel.setBorder(BorderFactory.createTitledBorder("Vote Streak"));

		JButton dayButton = new JButton("Vote Streak Day");
		dayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		dayButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, dayButton.getPreferredSize().height));
		dayButton.addActionListener(e -> {
			JFrame dayFrame = new JFrame("Vote Streak Day");
			dayFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dayFrame.setSize(600, 700);
			dayFrame.add(createVoteStreakDayPanel());
			dayFrame.setLocationRelativeTo(null);
			dayFrame.setVisible(true);
		});

		JButton weekButton = new JButton("Vote Streak Week");
		weekButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		weekButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, weekButton.getPreferredSize().height));
		weekButton.addActionListener(e -> {
			JFrame weekFrame = new JFrame("Vote Streak Week");
			weekFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			weekFrame.setSize(600, 700);
			weekFrame.add(createVoteStreakWeekPanel());
			weekFrame.setLocationRelativeTo(null);
			weekFrame.setVisible(true);
		});
		JButton monthButton = new JButton("Vote Streak Month");
		monthButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		monthButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, monthButton.getPreferredSize().height));

		monthButton.addActionListener(e -> {
			JFrame monthFrame = new JFrame("Vote Streak Month");
			monthFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			monthFrame.setSize(600, 700);
			monthFrame.add(createVoteStreakMonthPanel());
			monthFrame.setLocationRelativeTo(null);
			monthFrame.setVisible(true);
		});

		voteStreakPanel.add(dayButton);
		voteStreakPanel.add(weekButton);
		voteStreakPanel.add(monthButton);

		settingButtons.add(new BooleanSettingButton(voteStreakPanel, "Requirement.UsePercentage", getConfigData(),
				"Use Percentage", true));
		settingButtons.add(new IntSettingButton(voteStreakPanel, "Requirement.Day", getConfigData(), "Day", 25));
		settingButtons.add(new IntSettingButton(voteStreakPanel, "Requirement.Week", getConfigData(), "Week", 50));
		settingButtons.add(new IntSettingButton(voteStreakPanel, "Requirement.Month", getConfigData(), "Month", 50));

		return voteStreakPanel;
	}

	public JPanel createWeeklyAwardsPanel() {
		JPanel weeklyAwardsPanel = new JPanel();
		weeklyAwardsPanel.setLayout(new BoxLayout(weeklyAwardsPanel, BoxLayout.Y_AXIS));
		weeklyAwardsPanel.setBorder(BorderFactory.createTitledBorder("Weekly Awards"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(weeklyAwardsPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("WeeklyAwards." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("WeeklyAwards." + name + ".Rewards.Messages.Player",
						"&aYou came in " + name + " place in %TopVoter%!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("WeeklyAwards." + name + ".Rewards"), "WeeklyAwards." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("WeeklyAwards." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("WeeklyAwards." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("WeeklyAwards." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils
				.convertSetToArray(((Map<String, Object>) get("WeeklyAwards", new HashMap<String, Object>())).keySet());

		weeklyAwardsPanel.add(addRemoveEditor.getAddButton("Add Weekly Award", "Add Weekly Award"));
		weeklyAwardsPanel.add(addRemoveEditor.getRemoveButton("Remove Weekly Award", "Remove Weekly Award", list));
		weeklyAwardsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(weeklyAwardsPanel, list);

		return weeklyAwardsPanel;
	}

	public JPanel createVotePartyPanel() {
		JPanel votePartyPanel = new JPanel();
		votePartyPanel.setLayout(new BoxLayout(votePartyPanel, BoxLayout.Y_AXIS));
		votePartyPanel.setBorder(BorderFactory.createTitledBorder("VoteParty Settings"));

		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.Enabled", getConfigData(), "Enabled"));
		settingButtons.add(
				new IntSettingButton(votePartyPanel, "VoteParty.VotesRequired", getConfigData(), "Votes Required", 20));
		settingButtons.add(new IntSettingButton(votePartyPanel, "VoteParty.IncreaseVotesRequired", getConfigData(),
				"Increase Votes Required", 10));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.GiveAllPlayers", getConfigData(),
				"Give All Players"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.GiveOnlinePlayersOnly", getConfigData(),
				"Give Online Players Only"));
		settingButtons.add(
				new BooleanSettingButton(votePartyPanel, "VoteParty.ResetEachDay", getConfigData(), "Reset Each Day"));
		settingButtons.add(
				new BooleanSettingButton(votePartyPanel, "VoteParty.ResetWeekly", getConfigData(), "Reset Weekly"));
		settingButtons.add(
				new BooleanSettingButton(votePartyPanel, "VoteParty.ResetMonthly", getConfigData(), "Reset Monthly"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.OnlyOncePerDay", getConfigData(),
				"Only Once Per Day"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.OnlyOncePerWeek", getConfigData(),
				"Only Once Per Week"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.ResetExtraVotesWeekly", getConfigData(),
				"Reset Extra Votes Weekly"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.ResetExtraVotesMonthly", getConfigData(),
				"Reset Extra Votes Monthly"));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.CountFakeVotes", getConfigData(),
				"Count Fake Votes"));
		settingButtons.add(new IntSettingButton(votePartyPanel, "VoteParty.UserVotesRequired", getConfigData(),
				"User Votes Required", 0));
		settingButtons.add(new BooleanSettingButton(votePartyPanel, "VoteParty.CountOfflineVotes", getConfigData(),
				"Count Offline Votes"));
		settingButtons.add(new StringSettingButton(votePartyPanel, "VoteParty.Broadcast", getConfigData(), "Broadcast",
				"&cReached the vote party amount!"));
		settingButtons.add(new StringSettingButton(votePartyPanel, "VoteParty.VoteReminderBroadcast", getConfigData(),
				"Vote Reminder Broadcast", "%votesrequired% left to go, go vote!"));
		settingButtons.add(new StringListSettingButton(votePartyPanel, "VoteParty.VoteReminderAtVotes", getConfigData(),
				"Vote Reminder At Votes", ""));
		settingButtons.add(new StringListSettingButton(votePartyPanel, "VoteParty.GlobalCommands", getConfigData(),
				"Global Commands", ""));
		settingButtons.add(new StringListSettingButton(votePartyPanel, "VoteParty.GlobalRandomCommand", getConfigData(),
				"Global Random Command", ""));

		votePartyPanel.add(addRewardsButton("VoteParty.Rewards", "Edit VoteParty Rewards"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		return votePartyPanel;
	}

	public JPanel createCumulativeRewardsPanel() {
		JPanel cumulativeRewardsPanel = new JPanel();
		cumulativeRewardsPanel.setLayout(new BoxLayout(cumulativeRewardsPanel, BoxLayout.Y_AXIS));
		cumulativeRewardsPanel.setBorder(BorderFactory.createTitledBorder("Cumulative Rewards"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(cumulativeRewardsPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("Cumulative." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("Cumulative." + name + ".Enabled", false);
				set("Cumulative." + name + ".Rewards.Messages.Player", "&aYou got %cumulative% cumulative votes!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				JFrame cumulativeEditorFrame = new JFrame("Editing Cumulative Reward - " + name);
				cumulativeEditorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				cumulativeEditorFrame.setSize(600, 700);
				cumulativeEditorFrame
						.setLayout(new BoxLayout(cumulativeEditorFrame.getContentPane(), BoxLayout.Y_AXIS));

				JPanel settingsPanel = new JPanel();
				settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
				settingsPanel.setBorder(BorderFactory.createTitledBorder("Cumulative Reward Settings"));

				// Add settings components
				String[] totalToUseOptions = { "AllTime", "Monthly", "Weekly", "Daily" };
				settingButtons.add(new BooleanSettingButton(settingsPanel, "Cumulative." + name + ".Enabled",
						getConfigData(), "Enabled"));
				settingButtons.add(new StringSettingButton(settingsPanel, "Cumulative." + name + ".TotalToUse",
						getConfigData(), "Total To Use", "AllTime", totalToUseOptions));
				settingButtons.add(new StringListSettingButton(settingsPanel, "Cumulative." + name + ".BlackList",
						getConfigData(), "Blacklist", ""));
				settingButtons.add(new BooleanSettingButton(settingsPanel, "Cumulative." + name + ".Recurring",
						getConfigData(), "Recurring", true));

				settingsPanel.add(addRewardsButton("Cumulative." + name + ".Rewards", "Edit Rewards"));

				JButton saveButton = new JButton("Save and Apply Changes");
				saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, saveButton.getPreferredSize().height));
				saveButton.addActionListener(e -> saveChanges());
				saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

				cumulativeEditorFrame.add(settingsPanel);

				cumulativeEditorFrame.add(saveButton, BorderLayout.SOUTH);

				cumulativeEditorFrame.setLocationRelativeTo(null);

				cumulativeEditorFrame.setVisible(true);
			}
		};

		String[] list = PanelUtils
				.convertSetToArray(((Map<String, Object>) get("Cumulative", new HashMap<String, Object>())).keySet());

		cumulativeRewardsPanel.add(addRemoveEditor.getAddButton("Add Cumulative Reward", "Add Cumulative Reward"));
		cumulativeRewardsPanel
				.add(addRemoveEditor.getRemoveButton("Remove Cumulative Reward", "Remove Cumulative Reward", list));
		cumulativeRewardsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(cumulativeRewardsPanel, list);

		return cumulativeRewardsPanel;
	}

	public JPanel createVoteStreakDayPanel() {
		JPanel voteStreakDayPanel = new JPanel();
		voteStreakDayPanel.setLayout(new BoxLayout(voteStreakDayPanel, BoxLayout.Y_AXIS));
		voteStreakDayPanel.setBorder(BorderFactory.createTitledBorder("Vote Streak Day"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(voteStreakDayPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("VoteStreak.Day." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("VoteStreak.Day." + name + ".Enabled", false);
				set("VoteStreak.Day." + name + ".Rewards.Messages.Player",
						"&aYou voted for %Streak% %Type%'s in a row!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("VoteStreak.Day." + name + ".Rewards"), "VoteStreak.Day." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("VoteStreak.Day." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("VoteStreak.Day." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("VoteStreak.Day." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils.convertSetToArray(
				((Map<String, Object>) get("VoteStreak.Day", new HashMap<String, Object>())).keySet());

		voteStreakDayPanel.add(addRemoveEditor.getAddButton("Add Vote Streak Day", "Add Vote Streak Day"));
		voteStreakDayPanel
				.add(addRemoveEditor.getRemoveButton("Remove Vote Streak Day", "Remove Vote Streak Day", list));
		voteStreakDayPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(voteStreakDayPanel, list);

		return voteStreakDayPanel;
	}

	public JPanel createMilestonesPanel() {
		JPanel milestonesPanel = new JPanel();
		milestonesPanel.setLayout(new BoxLayout(milestonesPanel, BoxLayout.Y_AXIS));
		milestonesPanel.setBorder(BorderFactory.createTitledBorder("Milestones"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(milestonesPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("MileStones." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("MileStones." + name + ".Enabled", false);
				set("MileStones." + name + ".Rewards.Messages.Player", "&aYou got %milestone% milestone votes!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				JFrame milestoneEditorFrame = new JFrame("Editing Milestone - " + name);
				milestoneEditorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				milestoneEditorFrame.setSize(600, 700);
				milestoneEditorFrame.setLayout(new BoxLayout(milestoneEditorFrame.getContentPane(), BoxLayout.Y_AXIS));

				JPanel settingsPanel = new JPanel();
				settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
				settingsPanel.setBorder(BorderFactory.createTitledBorder("Milestone Settings"));

				settingButtons.add(new BooleanSettingButton(settingsPanel, "MileStones." + name + ".Enabled",
						getConfigData(), "Enabled"));
				settingsPanel.add(addRewardsButton("MileStones." + name + ".Rewards", "Edit Rewards"));

				JButton saveButton = new JButton("Save and Apply Changes");
				saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, saveButton.getPreferredSize().height));
				saveButton.addActionListener(e -> saveChanges());
				saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

				milestoneEditorFrame.add(settingsPanel);
				milestoneEditorFrame.add(saveButton, BorderLayout.SOUTH);

				milestoneEditorFrame.setLocationRelativeTo(null);
				milestoneEditorFrame.setVisible(true);
			}
		};

		String[] list = PanelUtils
				.convertSetToArray(((Map<String, Object>) get("MileStones", new HashMap<String, Object>())).keySet());

		milestonesPanel.add(addRemoveEditor.getAddButton("Add Milestone", "Add Milestone"));
		milestonesPanel.add(addRemoveEditor.getRemoveButton("Remove Milestone", "Remove Milestone", list));

		milestonesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(milestonesPanel, list);

		return milestonesPanel;
	}

	private JPanel createExtraOptionsPanel() {
		JPanel extraOptionsPanel = new JPanel();
		extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel, BoxLayout.Y_AXIS));
		extraOptionsPanel.setBorder(BorderFactory.createTitledBorder("Extra Options"));

		settingButtons.add(new BooleanSettingButton(extraOptionsPanel, "OnlyOneCumulative", getConfigData(),
				"Only One Cumulative"));
		settingButtons.add(new BooleanSettingButton(extraOptionsPanel, "ResetMilestonesMonthly", getConfigData(),
				"Reset Milestones Monthly"));
		settingButtons.add(new BooleanSettingButton(extraOptionsPanel, "EnableMonthlyAwards", getConfigData(),
				"Enable Monthly Awards"));
		settingButtons.add(new BooleanSettingButton(extraOptionsPanel, "EnableWeeklyAwards", getConfigData(),
				"Enable Weekly Awards"));
		settingButtons.add(new BooleanSettingButton(extraOptionsPanel, "EnableDailyRewards", getConfigData(),
				"Enable Daily Rewards"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);

		return extraOptionsPanel;
	}

	public JPanel createDailyAwardsPanel() {
		JPanel dailyAwardsPanel = new JPanel();
		dailyAwardsPanel.setLayout(new BoxLayout(dailyAwardsPanel, BoxLayout.Y_AXIS));
		dailyAwardsPanel.setBorder(BorderFactory.createTitledBorder("Daily Awards"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(dailyAwardsPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("DailyAwards." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("DailyAwards." + name + ".Rewards.Messages.Player",
						"&aYou came in " + name + " place in %TopVoter%!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("DailyAwards." + name + ".Rewards"), "DailyAwards." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("DailyAwards." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("DailyAwards." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("DailyAwards." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils
				.convertSetToArray(((Map<String, Object>) get("DailyAwards", new HashMap<String, Object>())).keySet());

		dailyAwardsPanel.add(addRemoveEditor.getAddButton("Add Daily Award", "Add Daily Award"));
		dailyAwardsPanel.add(addRemoveEditor.getRemoveButton("Remove Daily Award", "Remove Daily Award", list));
		dailyAwardsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(dailyAwardsPanel, list);

		return dailyAwardsPanel;
	}

	public JPanel createMonthlyAwardsPanel() {
		JPanel monthlyAwardsPanel = new JPanel();
		monthlyAwardsPanel.setLayout(new BoxLayout(monthlyAwardsPanel, BoxLayout.Y_AXIS));
		monthlyAwardsPanel.setBorder(BorderFactory.createTitledBorder("Monthly Awards"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(monthlyAwardsPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("MonthlyAwards." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("MonthlyAwards." + name + ".Rewards.Messages.Player",
						"&aYou came in " + name + " place in %TopVoter%!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("MonthlyAwards." + name + ".Rewards"), "MonthlyAwards." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("MonthlyAwards." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("MonthlyAwards." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("MonthlyAwards." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils.convertSetToArray(
				((Map<String, Object>) get("MonthlyAwards", new HashMap<String, Object>())).keySet());

		monthlyAwardsPanel.add(addRemoveEditor.getAddButton("Add Monthly Award", "Add Monthly Award"));
		monthlyAwardsPanel.add(addRemoveEditor.getRemoveButton("Remove Monthly Award", "Remove Monthly Award", list));
		monthlyAwardsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(monthlyAwardsPanel, list);

		return monthlyAwardsPanel;
	}

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> {
			new RewardEditor(get(path), path) {

				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							set(path + "." + change.getKey(), change.getValue());
						}
						save();
						editorFrame.dispose();
						openEditorGUI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void removePath(String path1) {
					remove(path + "." + path1);
					save();
				}

				@Override
				public Map<String, Object> updateData() {
					load();
					return (Map<String, Object>) get(path);
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

	public JPanel createVoteStreakWeekPanel() {
		JPanel voteStreakWeekPanel = new JPanel();
		voteStreakWeekPanel.setLayout(new BoxLayout(voteStreakWeekPanel, BoxLayout.Y_AXIS));
		voteStreakWeekPanel.setBorder(BorderFactory.createTitledBorder("Vote Streak Week"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(voteStreakWeekPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("VoteStreak.Week." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("VoteStreak.Week." + name + ".Enabled", false);
				set("VoteStreak.Week." + name + ".Rewards.Messages.Player",
						"&aYou voted for %Streak% %Type%'s in a row!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("VoteStreak.Week." + name + ".Rewards"), "VoteStreak.Week." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("VoteStreak.Week." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("VoteStreak.Week." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("VoteStreak.Week." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils.convertSetToArray(
				((Map<String, Object>) get("VoteStreak.Week", new HashMap<String, Object>())).keySet());

		voteStreakWeekPanel.add(addRemoveEditor.getAddButton("Add Vote Streak Week", "Add Vote Streak Week"));
		voteStreakWeekPanel
				.add(addRemoveEditor.getRemoveButton("Remove Vote Streak Week", "Remove Vote Streak Week", list));
		voteStreakWeekPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(voteStreakWeekPanel, list);

		return voteStreakWeekPanel;
	}

	public JPanel createVoteStreakMonthPanel() {
		JPanel voteStreakMonthPanel = new JPanel();
		voteStreakMonthPanel.setLayout(new BoxLayout(voteStreakMonthPanel, BoxLayout.Y_AXIS));
		voteStreakMonthPanel.setBorder(BorderFactory.createTitledBorder("Vote Streak Month"));

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(voteStreakMonthPanel.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("VoteStreak.Month." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("VoteStreak.Month." + name + ".Enabled", false);
				set("VoteStreak.Month." + name + ".Rewards.Messages.Player",
						"&aYou voted for %Streak% %Type%'s in a row!");
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new RewardEditor(get("VoteStreak.Month." + name + ".Rewards"),
						"VoteStreak.Month." + name + ".Rewards") {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("VoteStreak.Month." + name + ".Rewards." + change.getKey(), change.getValue());
							}
							save();
							editorFrame.dispose();
							openEditorGUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removePath(String path) {
						remove("VoteStreak.Month." + name + ".Rewards." + path);
						save();
					}

					@Override
					public Map<String, Object> updateData() {
						load();
						return (Map<String, Object>) get("VoteStreak.Month." + name + ".Rewards");
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
			}
		};

		String[] list = PanelUtils.convertSetToArray(
				((Map<String, Object>) get("VoteStreak.Month", new HashMap<String, Object>())).keySet());

		voteStreakMonthPanel.add(addRemoveEditor.getAddButton("Add Vote Streak Month", "Add Vote Streak Month"));
		voteStreakMonthPanel
				.add(addRemoveEditor.getRemoveButton("Remove Vote Streak Month", "Remove Vote Streak Month", list));
		voteStreakMonthPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		addRemoveEditor.getOptionsButtons(voteStreakMonthPanel, list);

		return voteStreakMonthPanel;
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
