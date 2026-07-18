package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
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
import javax.swing.JTabbedPane;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

public class BungeeConfigFile extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	public BungeeConfigFile(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("Proxy bungeeconfig.yml Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(850, 750);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Main Settings", createMainEditorPanel());
		tabs.addTab("Database", createDatabasePanel());
		tabs.addTab("Transport", createTransportPanel());
		tabs.addTab("Caches and Logging", createCachePanel());
		tabs.addTab("Proxy Broadcast", createProxyBroadcastPanel());
		tabs.addTab("Vote Party", createVotePartyPanel());
		frame.add(tabs, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createMainEditorPanel() {
		JPanel panel = panel();
		settingButtons.add(new BooleanSettingButton(panel, "Debug", getConfigData(), "Debug"));
		settingButtons.add(new BooleanSettingButton(panel, "SendVotesToAllServers", getConfigData(),
				"Send Votes To All Servers"));
		settingButtons.add(new StringListSettingButton(panel, "BlockedServers", getConfigData(), "Blocked Servers"));
		settingButtons.add(new StringListSettingButton(panel, "WhiteListedServers", getConfigData(),
				"Whitelisted Servers"));
		settingButtons.add(new BooleanSettingButton(panel, "WaitForUserOnline", getConfigData(), "Wait For User Online"));
		settingButtons.add(new BooleanSettingButton(panel, "AllowUnJoined", getConfigData(), "Allow Unjoined Players"));
		settingButtons.add(new IntSettingButton(panel, "PointsOnVote", getConfigData(), "Points On Vote", 1));
		settingButtons.add(new BooleanSettingButton(panel, "BungeeManageTotals", getConfigData(),
				"Proxy Manages Totals"));
		settingButtons.add(new StringSettingButton(panel, "BedrockPlayerPrefix", getConfigData(),
				"Bedrock Player Prefix", "."));
		settingButtons.add(new IntSettingButton(panel, "VoteCacheTime", getConfigData(), "Vote Cache Time (days)", -1));
		settingButtons.add(new IntSettingButton(panel, "MaxAmountOfVotesPerDay", getConfigData(),
				"Maximum Votes Per Day", -1));
		settingButtons.add(new IntSettingButton(panel, "LimitVotePoints", getConfigData(), "Vote Point Limit", -1));
		settingButtons.add(new IntSettingButton(panel, "TimeHourOffSet", getConfigData(), "Time Hour Offset", 0));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createDatabasePanel() {
		JPanel panel = panel();
		settingButtons.add(new StringSettingButton(panel, "Database.Host", getConfigData(), "Database Host", ""));
		settingButtons.add(new IntSettingButton(panel, "Database.Port", getConfigData(), "Database Port", 3306));
		settingButtons.add(new StringSettingButton(panel, "Database.Database", getConfigData(), "Database Name", ""));
		settingButtons.add(new StringSettingButton(panel, "Database.Username", getConfigData(), "Database Username", ""));
		settingButtons.add(new StringSettingButton(panel, "Database.Password", getConfigData(), "Database Password", ""));
		settingButtons.add(new IntSettingButton(panel, "Database.MaxConnections", getConfigData(),
				"Maximum Connections", 1));
		settingButtons.add(new StringSettingButton(panel, "Database.Name", getConfigData(), "Table Name Override", ""));
		settingButtons.add(new StringSettingButton(panel, "Database.Prefix", getConfigData(), "Table Prefix", ""));
		settingButtons.add(new StringSettingButton(panel, "Database.DbType", getConfigData(), "Database Type", "MYSQL",
				new String[] { "MYSQL", "MARIADB", "POSTGRESQL" }));
		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.Enabled", getConfigData(), "Global Data Enabled"));
		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.UseMainMySQL", getConfigData(),
				"Global Data Uses Main Database"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createTransportPanel() {
		JPanel panel = panel();
		settingButtons.add(new StringSettingButton(panel, "BungeeMethod", getConfigData(), "Bungee Method",
				"PLUGINMESSAGING", new String[] { "PLUGINMESSAGING", "REDIS", "MQTT", "SOCKETS", "MYSQL" }));
		settingButtons.add(new StringSettingButton(panel, "PluginMessageChannel", getConfigData(),
				"Plugin Message Channel", "vp:vp"));
		settingButtons.add(new BooleanSettingButton(panel, "PluginMessageEncryption", getConfigData(),
				"Plugin Message Encryption"));
		settingButtons.add(new StringSettingButton(panel, "Redis.Host", getConfigData(), "Redis Host", "localhost"));
		settingButtons.add(new IntSettingButton(panel, "Redis.Port", getConfigData(), "Redis Port", 6379));
		settingButtons.add(new StringSettingButton(panel, "Redis.Username", getConfigData(), "Redis Username", ""));
		settingButtons.add(new StringSettingButton(panel, "Redis.Password", getConfigData(), "Redis Password", ""));
		settingButtons.add(new StringSettingButton(panel, "Redis.Prefix", getConfigData(), "Redis Prefix", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.ClientID", getConfigData(), "MQTT Client ID", "proxy"));
		settingButtons.add(new StringSettingButton(panel, "MQTT.BrokerURL", getConfigData(), "MQTT Broker URL",
				"tcp://localhost:1883"));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Username", getConfigData(), "MQTT Username", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Password", getConfigData(), "MQTT Password", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Prefix", getConfigData(), "MQTT Prefix", ""));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createCachePanel() {
		JPanel panel = panel();
		settingButtons.add(new BooleanSettingButton(panel, "VoteCache.UseMySQL", getConfigData(),
				"Store Vote Cache In Database"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteCache.UseMainMySQL", getConfigData(),
				"Vote Cache Uses Main Database"));
		settingButtons.add(new BooleanSettingButton(panel, "NonVotedCache.UseMySQL", getConfigData(),
				"Store Non-Voted Cache In Database"));
		settingButtons.add(new BooleanSettingButton(panel, "NonVotedCache.UseMainMySQL", getConfigData(),
				"Non-Voted Cache Uses Main Database"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteLogging.Enabled", getConfigData(),
				"Database Vote Logging"));
		settingButtons.add(new IntSettingButton(panel, "VoteLogging.PurgeDays", getConfigData(),
				"Vote Log Purge Days", 30));
		settingButtons.add(new BooleanSettingButton(panel, "VoteLogging.UseMainMySQL", getConfigData(),
				"Vote Logging Uses Main Database"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createProxyBroadcastPanel() {
		JPanel panel = panel();
		settingButtons.add(new BooleanSettingButton(panel, "ProxyBroadcast.Enabled", getConfigData(),
				"Proxy Broadcast Enabled"));
		settingButtons.add(new StringSettingButton(panel, "ProxyBroadcast.Scope.Mode", getConfigData(), "Scope Mode",
				"ALL_SERVERS", new String[] { "PLAYER_SERVER", "ALL_SERVERS", "SERVERS", "ALL_EXCEPT" }));
		settingButtons.add(new StringListSettingButton(panel, "ProxyBroadcast.Scope.Servers", getConfigData(),
				"Scope Servers"));
		settingButtons.add(new StringSettingButton(panel, "ProxyBroadcast.OfflineMode", getConfigData(),
				"Offline Mode", "FORWARD", new String[] { "NONE", "QUEUE", "FORWARD" }));
		settingButtons.add(new StringListSettingButton(panel, "ProxyBroadcast.OfflineForward.Servers", getConfigData(),
				"Offline Forward Servers"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createVotePartyPanel() {
		JPanel panel = panel();
		settingButtons.add(new BooleanSettingButton(panel, "VoteParty.Enabled", getConfigData(), "Vote Party Enabled"));
		settingButtons.add(new IntSettingButton(panel, "VoteParty.VotesRequired", getConfigData(),
				"Votes Required", 100));
		settingButtons.add(new IntSettingButton(panel, "VoteParty.IncreaseVotesRequired", getConfigData(),
				"Increase Votes Required", 0));
		settingButtons.add(new StringSettingButton(panel, "VoteParty.Broadcast", getConfigData(),
				"Vote Party Broadcast", "&aVote party has been reached!"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel panel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return panel;
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