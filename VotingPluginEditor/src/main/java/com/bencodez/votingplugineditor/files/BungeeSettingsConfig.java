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
import com.bencodez.votingplugineditor.SFTPSettings;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;

public class BungeeSettingsConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	public BungeeSettingsConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory,	sftp);
		settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("BungeeSettings Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel mainPanel = createMainEditorPanel();
		tabbedPane.addTab("Main Settings", mainPanel);

		JPanel globalDataPanel = createGlobalDataPanel();
		tabbedPane.addTab("Global Data", globalDataPanel);

		JPanel bungeeVotePartyPanel = createBungeeVotePartyPanel();
		tabbedPane.addTab("Bungee Vote Party", bungeeVotePartyPanel);

		JPanel pluginMessagePanel = createPluginMessagePanel();
		tabbedPane.addTab("Advanced", pluginMessagePanel);

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

		settingButtons.add(new BooleanSettingButton(panel, "UseBungeecord", getConfigData(), "Use Bungeecord"));
		settingButtons.add(new StringSettingButton(panel, "BungeeMethod", getConfigData(), "Bungee Method",
				"PLUGINMESSAGING", new String[] { "SOCKETS", "PLUGINMESSAGING", "MYSQL", "REDIS" }));

		JPanel redisPanel = new JPanel();
		redisPanel.setLayout(new BoxLayout(redisPanel, BoxLayout.Y_AXIS));
		redisPanel.setBorder(BorderFactory.createTitledBorder("Redis Settings"));
		settingButtons
				.add(new StringSettingButton(redisPanel, "Redis.Host", getConfigData(), "Redis Host", "localhost"));
		settingButtons.add(new IntSettingButton(redisPanel, "Redis.Port", getConfigData(), "Redis Port", 6379));
		settingButtons.add(
				new StringSettingButton(redisPanel, "Redis.Username", getConfigData(), "Redis Username", "default"));
		settingButtons
				.add(new StringSettingButton(redisPanel, "Redis.Password", getConfigData(), "Redis Password", ""));
		settingButtons.add(new StringSettingButton(redisPanel, "Redis.Prefix", getConfigData(), "Redis Prefix", ""));
		redisPanel.setVisible(false); // Initially hide the panel

		JButton toggleRedisButton = new JButton("Show/Hide Redis Settings");
		toggleRedisButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleRedisButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleRedisButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleRedisButton.getPreferredSize().height));
		toggleRedisButton.addActionListener(event -> redisPanel.setVisible(!redisPanel.isVisible()));

		panel.add(toggleRedisButton);
		panel.add(redisPanel);

		settingButtons.add(new BooleanSettingButton(panel, "BungeeDebug", getConfigData(), "Bungee Debug"));
		settingButtons.add(new BooleanSettingButton(panel, "BungeeBroadcast", getConfigData(), "Bungee Broadcast"));
		settingButtons.add(
				new BooleanSettingButton(panel, "BungeeBroadcastAlways", getConfigData(), "Bungee Broadcast Always"));
		settingButtons.add(new BooleanSettingButton(panel, "DisableBroadcast", getConfigData(), "Disable Broadcast"));
		settingButtons.add(new BooleanSettingButton(panel, "PerServerRewards", getConfigData(), "Per Server Rewards"));
		settingButtons
				.add(new BooleanSettingButton(panel, "PerServerMilestones", getConfigData(), "Per Server Milestones"));
		settingButtons.add(new BooleanSettingButton(panel, "PerServerPoints", getConfigData(), "Per Server Points"));
		settingButtons.add(
				new BooleanSettingButton(panel, "TriggerVotifierEvent", getConfigData(), "Trigger Votifier Event"));
		settingButtons.add(new BooleanSettingButton(panel, "GiveExtraAllSitesRewards", getConfigData(),
				"Give Extra All Sites Rewards"));
		settingButtons.add(new StringSettingButton(panel, "Server", getConfigData(), "Server", "PleaseSet"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		panel.add(Box.createVerticalStrut(10)); // Spacer

		return panel;
	}

	private JPanel createPluginMessagePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new StringSettingButton(panel, "PluginMessageChannel", getConfigData(),
				"Plugin Message Channel", "vp:vp"));
		settingButtons.add(new BooleanSettingButton(panel, "PluginMessageEncryption", getConfigData(),
				"Plugin Message Encryption"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		panel.add(Box.createVerticalStrut(10)); // Spacer

		return panel;
	}

	private JPanel createBungeeVotePartyPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton bungeeVotePartyRewardsButton = addRewardsButton("BungeeVotePartyRewards",
				"Edit Bungee Vote Party Rewards");
		panel.add(bungeeVotePartyRewardsButton);

		settingButtons.add(new StringListSettingButton(panel, "BungeeVotePartyGlobalCommands", getConfigData(),
				"Bungee Vote Party Global Commands"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		panel.add(Box.createVerticalStrut(10)); // Spacer

		return panel;
	}

	private JPanel createGlobalDataPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.Enabled", getConfigData(), "Enabled"));
		settingButtons
				.add(new BooleanSettingButton(panel, "GlobalData.UseMainMySQL", getConfigData(), "Use Main MySQL"));

		JPanel mysqlPanel = new JPanel();
		mysqlPanel.setLayout(new BoxLayout(mysqlPanel, BoxLayout.Y_AXIS));
		mysqlPanel.setBorder(BorderFactory.createTitledBorder("MySQL Settings"));
		settingButtons.add(new StringSettingButton(mysqlPanel, "GlobalData.Host", getConfigData(), "MySQL Host", ""));
		settingButtons.add(new IntSettingButton(mysqlPanel, "GlobalData.Port", getConfigData(), "MySQL Port", 3306));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "GlobalData.Database", getConfigData(), "MySQL Database", ""));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "GlobalData.Username", getConfigData(), "MySQL Username", ""));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "GlobalData.Password", getConfigData(), "MySQL Password", ""));
		settingButtons.add(
				new IntSettingButton(mysqlPanel, "GlobalData.MaxConnections", getConfigData(), "Max Connections", 1));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "GlobalData.Prefix", getConfigData(), "MySQL Prefix", ""));
		mysqlPanel.setVisible(false); // Initially hide the panel

		JButton toggleMySQLButton = new JButton("Show/Hide MySQL Settings");
		toggleMySQLButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleMySQLButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleMySQLButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleMySQLButton.getPreferredSize().height));
		toggleMySQLButton.addActionListener(event -> mysqlPanel.setVisible(!mysqlPanel.isVisible()));

		panel.add(toggleMySQLButton);
		panel.add(mysqlPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		panel.add(Box.createVerticalStrut(10)); // Spacer

		return panel;
	}

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
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