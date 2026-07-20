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
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

public class BungeeSettingsConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	public BungeeSettingsConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("BungeeSettings Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main Settings", createMainEditorPanel());
		tabbedPane.addTab("Global Data", createGlobalDataPanel());
		tabbedPane.addTab("Bungee Vote Party", createBungeeVotePartyPanel());
		tabbedPane.addTab("Advanced", createPluginMessagePanel());

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

		settingButtons.add(new BooleanSettingButton(panel, "UseBungeecord", getConfigData(), "Use Proxy Features"));
		settingButtons.add(new StringSettingButton(panel, "BungeeMethod", getConfigData(), "Bungee Method",
				"PLUGINMESSAGING", new String[] { "PLUGINMESSAGING", "REDIS", "MQTT", "MYSQL", "SOCKETS" }));
		panel.add(createRedisPanel());
		panel.add(createMqttPanel());
		settingButtons.add(new BooleanSettingButton(panel, "BungeeDebug", getConfigData(), "Bungee Debug"));
		settingButtons.add(new BooleanSettingButton(panel, "PerServerRewards", getConfigData(), "Per Server Rewards"));
		settingButtons.add(new BooleanSettingButton(panel, "PerServerPoints", getConfigData(), "Per Server Points"));
		settingButtons.add(new BooleanSettingButton(panel, "TriggerVotifierEvent", getConfigData(), "Trigger Votifier Event"));
		settingButtons.add(new BooleanSettingButton(panel, "GiveExtraAllSitesRewards", getConfigData(),
				"Give Extra All Sites Rewards"));
		settingButtons.add(new StringSettingButton(panel, "Server", getConfigData(), "Unique Backend Server Name", "PleaseSet"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		panel.add(Box.createVerticalStrut(10));
		return panel;
	}

	private JPanel createRedisPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Redis Settings"));
		settingButtons.add(new StringSettingButton(panel, "Redis.Host", getConfigData(), "Redis Host", "localhost"));
		settingButtons.add(new IntSettingButton(panel, "Redis.Port", getConfigData(), "Redis Port", 6379));
		settingButtons.add(new StringSettingButton(panel, "Redis.Username", getConfigData(), "Redis Username", "default"));
		settingButtons.add(new StringSettingButton(panel, "Redis.Password", getConfigData(), "Redis Password", ""));
		settingButtons.add(new StringSettingButton(panel, "Redis.Prefix", getConfigData(), "Redis Prefix", ""));
		settingButtons.add(new IntSettingButton(panel, "Redis.Db-Index", getConfigData(), "Redis Database Index", 0));
		return createTogglePanel("Show/Hide Redis Settings", panel);
	}

	private JPanel createMqttPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("MQTT Settings"));
		settingButtons.add(new StringSettingButton(panel, "MQTT.ClientID", getConfigData(), "MQTT Client ID", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.BrokerURL", getConfigData(), "Broker URL",
				"tcp://localhost:1883"));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Username", getConfigData(), "MQTT Username", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Password", getConfigData(), "MQTT Password", ""));
		settingButtons.add(new StringSettingButton(panel, "MQTT.Prefix", getConfigData(), "MQTT Prefix", ""));
		return createTogglePanel("Show/Hide MQTT Settings", panel);
	}

	private JPanel createTogglePanel(String buttonText, JPanel settingsPanel) {
		settingsPanel.setVisible(false);
		JButton toggleButton = new JButton(buttonText);
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.addActionListener(event -> settingsPanel.setVisible(!settingsPanel.isVisible()));
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(toggleButton);
		container.add(settingsPanel);
		return container;
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
		return panel;
	}

	private JPanel createBungeeVotePartyPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(addRewardsButton("BungeeVotePartyRewards", "Edit Bungee Vote Party Rewards"));
		settingButtons.add(new StringListSettingButton(panel, "BungeeVotePartyGlobalCommands", getConfigData(),
				"Bungee Vote Party Global Commands"));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createGlobalDataPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.Enabled", getConfigData(), "Enabled"));
		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.UseMainMySQL", getConfigData(),
				"Use Main Database Connection"));

		JPanel databasePanel = new JPanel();
		databasePanel.setLayout(new BoxLayout(databasePanel, BoxLayout.Y_AXIS));
		databasePanel.setBorder(BorderFactory.createTitledBorder("Global Data Database Settings"));
		settingButtons.add(new StringSettingButton(databasePanel, "GlobalData.Host", getConfigData(), "Host", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "GlobalData.Port", getConfigData(), "Port", 3306));
		settingButtons.add(new StringSettingButton(databasePanel, "GlobalData.Database", getConfigData(), "Database", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "GlobalData.Username", getConfigData(), "Username", ""));
		settingButtons.add(new StringSettingButton(databasePanel, "GlobalData.Password", getConfigData(), "Password", ""));
		settingButtons.add(new IntSettingButton(databasePanel, "GlobalData.MaxConnections", getConfigData(),
				"Max Connections", 1));
		settingButtons.add(new StringSettingButton(databasePanel, "GlobalData.Prefix", getConfigData(), "Prefix", ""));
		panel.add(createTogglePanel("Show/Hide Global Data Database Settings", databasePanel));
		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> new RewardEditor(get(path), path) {
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
