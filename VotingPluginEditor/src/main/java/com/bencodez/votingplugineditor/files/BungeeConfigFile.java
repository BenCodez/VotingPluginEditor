
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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
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
		JFrame frame = new JFrame("Config.yml Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 900);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel mainPanel = createMainEditorPanel();
		tabbedPane.addTab("Main Settings", mainPanel);

		frame.add(tabbedPane, BorderLayout.CENTER);

		JPanel votePartyPanel = createVotePartyPanel();
		tabbedPane.addTab("Vote Party Settings", votePartyPanel);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createVotePartyPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new BooleanSettingButton(panel, "VoteParty.Enabled", getConfigData(), "Vote Party Enabled"));
		settingButtons.add(new IntSettingButton(panel, "VoteParty.VotesRequired", getConfigData(),
				"Vote Party Votes Required", 100));
		settingButtons.add(new IntSettingButton(panel, "VoteParty.IncreaseVotesRequired", getConfigData(),
				"Vote Party Increase Votes Required", 0));
		settingButtons.add(new StringSettingButton(panel, "VoteParty.Broadcast", getConfigData(),
				"Vote Party Broadcast", "&aVote party has been reached!"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private JPanel createMainEditorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(createMySQLSettingsPanel());
		settingButtons.add(new BooleanSettingButton(panel, "Debug", getConfigData(), "Debug"));
		settingButtons.add(new BooleanSettingButton(panel, "AllowUnJoined", getConfigData(), "Allow UnJoined"));
		settingButtons.add(new StringSettingButton(panel, "BungeeMethod", getConfigData(), "Bungee Method",
				"PLUGINMESSAGING", new String[] { "PLUGINMESSAGING", "REDIS", "SOCKETS", "MYSQL","MQTT" }));
		settingButtons.add(
				new BooleanSettingButton(panel, "SendVotesToAllServers", getConfigData(), "Send Votes To All Servers"));

		settingButtons.add(new BooleanSettingButton(panel, "StoreMonthTotalsWithDate", getConfigData(),
				"Store Month Totals With Date"));
		settingButtons.add(new BooleanSettingButton(panel, "UseMonthDateTotalsAsPrimaryTotal", getConfigData(),
				"Use Month Date Totals As Primary Total"));

		settingButtons.add(
				new StringSettingButton(panel, "BedrockPlayerPrefix", getConfigData(), "Bedrock Player Prefix", "."));

		settingButtons.add(new BooleanSettingButton(panel, "Broadcast", getConfigData(), "Broadcast"));
		settingButtons.add(new BooleanSettingButton(panel, "PluginMessageEncryption", getConfigData(),
				"Plugin Message Encryption"));
		settingButtons.add(new BooleanSettingButton(panel, "AllowUnJoined", getConfigData(), "Allow UnJoined"));

		settingButtons.add(new BooleanSettingButton(panel, "UUIDLookup", getConfigData(), "UUID Lookup"));
		settingButtons.add(new BooleanSettingButton(panel, "OnlineMode", getConfigData(), "Online Mode", true));

		settingButtons.add(new IntSettingButton(panel, "TimeHourOffSet", getConfigData(), "Time Hour Offset", 0));

		settingButtons.add(new IntSettingButton(panel, "PointsOnVote", getConfigData(), "Points On Vote", 1));
		settingButtons.add(new IntSettingButton(panel, "VoteCacheTime", getConfigData(), "Vote Cache Time", -1));

		settingButtons.add(new IntSettingButton(panel, "MaxAmountOfVotesPerDay", getConfigData(),
				"Max Amount Of Votes Per Day", -1));
		settingButtons.add(new IntSettingButton(panel, "LimitVotePoints", getConfigData(), "Limit Vote Points", -1));

		settingButtons
				.add(new BooleanSettingButton(panel, "GlobalData.Enabled", getConfigData(), "Global Data Enabled"));
		settingButtons.add(new BooleanSettingButton(panel, "GlobalData.UseMainMySQL", getConfigData(),
				"Use Main MySQL for Global Data"));

		settingButtons
				.add(new BooleanSettingButton(panel, "BungeeManageTotals", getConfigData(), "Bungee Manage Totals"));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private Object getConfigData(String path) {
		return get(path);
	}

	private JPanel createMySQLSettingsPanel() {
		JPanel mysqlPanel = new JPanel();
		mysqlPanel.setLayout(new BoxLayout(mysqlPanel, BoxLayout.Y_AXIS));
		mysqlPanel.setBorder(BorderFactory.createTitledBorder("MySQL Settings"));

		ArrayList<SettingButton> settingButtons = new ArrayList<SettingButton>();

		settingButtons.add(new StringSettingButton(mysqlPanel, "Host", getConfigData(), "MySQL Host", "192.168.0.156"));
		settingButtons.add(new IntSettingButton(mysqlPanel, "Port", getConfigData(), "MySQL Port", 3306));
		settingButtons.add(new StringSettingButton(mysqlPanel, "Database", getConfigData(), "MySQL Database", "db"));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "Password", getConfigData(), "MySQL Password", "mD9!Zui9GH"));
		settingButtons
				.add(new IntSettingButton(mysqlPanel, "MaxConnections", getConfigData(), "MySQL Max Connections", 1));
		settingButtons.add(new StringSettingButton(mysqlPanel, "Prefix", getConfigData(), "MySQL Prefix", ""));
		settingButtons
				.add(new StringSettingButton(mysqlPanel, "Name", getConfigData(), "MySQL Name", "VotingPlugin_Users"));

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
