package com.bencodez.votingplugineditor.votesites;

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
import javax.swing.SwingUtilities;

import com.bencodez.votingplugineditor.VotingPluginEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringListSettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;
import com.bencodez.votingplugineditor.files.VoteSitesConfig;

public class VoteSiteEditor {

	private final List<SettingButton> buttons;
	private final String siteName;

	private VoteSiteEditor(String siteName) {
		this.siteName = siteName;
		buttons = new ArrayList<>();
	}

	public VoteSiteEditor(VoteSitesConfig voteSitesConfig, String siteName) {
		this(siteName);
		Map<String, Object> siteData = (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName,
				new HashMap<>());
		SwingUtilities.invokeLater(() -> createAndShowGUI(siteName, siteData, voteSitesConfig));
	}

	private void createAndShowGUI(String siteName, Map<String, Object> siteData, VoteSitesConfig voteSitesConfig) {
		JFrame frame = new JFrame("VoteSite Editor - " + siteName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(550, 600);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main", createMainPanel(siteName, siteData, voteSitesConfig));
		tabbedPane.addTab("Advanced Options", createAdvancedOptionsPanel(siteData, voteSitesConfig));
		frame.add(tabbedPane, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChanges(siteName, voteSitesConfig));
		frame.add(saveButton, BorderLayout.SOUTH);

		PanelUtils.adjustSettingButtonsMaxWidth(buttons);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createMainPanel(String voteSiteName, Map<String, Object> siteData, VoteSitesConfig voteSitesConfig) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		buttons.add(new BooleanSettingButton(panel, "Enabled", siteData, "VoteSite Enabled:"));
		panel.add(Box.createVerticalStrut(10));
		buttons.add(new StringSettingButton(panel, "Name", siteData, "Display Name", voteSiteName));
		buttons.add(new StringSettingButton(panel, "ServiceSite", siteData, "Service Site:", "NOT SET"));
		buttons.add(new StringSettingButton(panel, "VoteURL", siteData, "Voting URL:", "NOT SET"));
		buttons.add(new StringSettingButton(panel, "VoteDelay", siteData, "Vote Delay:", "24h",
				"Use a duration such as 24h. Legacy numbers are hours."));

		panel.add(createRewardButton(voteSitesConfig, "Rewards", "Edit Rewards", null));
		panel.add(Box.createVerticalStrut(10));
		return panel;
	}

	private JPanel createAdvancedOptionsPanel(Map<String, Object> siteData, VoteSitesConfig voteSitesConfig) {
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
		advancedPanel.setBorder(BorderFactory.createTitledBorder("Advanced Options"));

		buttons.add(new BooleanSettingButton(advancedPanel, "WaitUntilVoteDelay", siteData, "Wait Until Vote Delay:",
				false, "Blocks votes until the configured VoteDelay has passed"));
		buttons.add(new BooleanSettingButton(advancedPanel, "VoteDelayDaily", siteData, "Vote Delay Daily:", false,
				"Reset the vote delay at a fixed time each day"));
		buttons.add(new IntSettingButton(advancedPanel, "VoteDelayDailyHour", siteData, "Daily Reset Hour", 0,
				"Server hour from 0 (midnight) to 23 when daily reset is enabled") {
			@Override
			public Object getValue() {
				int hour = (int) super.getValue();
				if (hour < 0 || hour > 23) {
					throw new IllegalArgumentException("Enter a daily reset hour from 0 to 23.");
				}
				return hour;
			}
		});
		buttons.add(new BooleanSettingButton(advancedPanel, "ForceOffline", siteData, "Force Offline:", false,
				"Run this site's rewards while the player is offline; this changes offline vote handling"));
		buttons.add(new BooleanSettingButton(advancedPanel, "Hidden", siteData, "Hidden:", false,
				"Hide this VoteSite from supported GUIs and counters"));
		buttons.add(new StringSettingButton(advancedPanel, "PermissionToView", siteData, "Permission To View:", ""));
		buttons.add(new IntSettingButton(advancedPanel, "Priority", siteData, "Priority:", 5,
				"Higher-priority sites appear earlier in sorted VoteSite lists"));

		advancedPanel.add(PanelUtils.createSectionLabel("Display Item"));
		buttons.add(new StringSettingButton(advancedPanel, "DisplayItem.Material", siteData, "Display Item Material",
				"DIAMOND", PanelUtils.convertListToArray(VotingPluginEditor.getMaterials()), "Used in supported GUIs"));
		buttons.add(new IntSettingButton(advancedPanel, "DisplayItem.Amount", siteData, "Display Item Amount:", 1,
				"Used in supported GUIs"));
		buttons.add(new StringSettingButton(advancedPanel, "DisplayItem.Name", siteData, "Display Item Name:", ""));
		buttons.add(new StringListSettingButton(advancedPanel, "DisplayItem.Lore", siteData, "Display Item Lore", ""));

		advancedPanel.add(createRewardButton(voteSitesConfig, "CoolDownEndRewards", "Edit Cooldown End Rewards",
				"Runs when the site's cooldown ends if PerSiteCoolDownEvents is enabled"));
		advancedPanel.add(createRewardButton(voteSitesConfig, "WaitUntilVoteDelayRewards", "Edit Rejected Vote Rewards (7.2+)",
				"Runs when WaitUntilVoteDelay rejects a vote. Placeholders: %ServiceSite%, %SiteName%, %VoteDelay%, %VoteURL%"));
		return advancedPanel;
	}

	private JButton createRewardButton(VoteSitesConfig voteSitesConfig, String section, String label, String help) {
		String path = "VoteSites." + siteName + "." + section;
		JButton button = new JButton(label);
		button.setToolTipText(help);
		button.setHorizontalAlignment(SwingConstants.CENTER);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
		button.setAlignmentY(Component.CENTER_ALIGNMENT);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.addActionListener(event -> new RewardEditor(voteSitesConfig.get(path), siteName + "." + section) {
			@Override
			public void saveChanges(Map<String, Object> changes) {
				try {
					saveRewardChanges(voteSitesConfig, path, changes);
					JOptionPane.showMessageDialog(null, "Changes have been saved.");
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to save changes.");
				}
			}

			@Override
			public void removePath(String key) {
				voteSitesConfig.remove(path + "." + key);
				voteSitesConfig.save();
			}

			@Override
			public Map<String, Object> updateData() {
				Object reward = voteSitesConfig.get(path);
				return reward instanceof Map<?, ?> ? (Map<String, Object>) reward : new HashMap<>();
			}

			@Override
			public String getVotingPluginDirectory() {
				return voteSitesConfig.getPluginDirectory();
			}

			@Override
			public SFTPSettings getSFTPSetting() {
				return voteSitesConfig.getSFTPSettings();
			}
		});
		return button;
	}

	static void saveRewardChanges(VoteSitesConfig config, String path, Map<String, Object> changes) {
		for (Entry<String, Object> change : changes.entrySet()) {
			String key = change.getKey();
			config.set(key.isEmpty() ? path : path + "." + key, change.getValue());
		}
		config.save();
	}

	private void saveChanges(String voteSiteName, VoteSitesConfig voteSitesConfig) {
		Map<String, Object> changes = new HashMap<>();
		try {
			for (SettingButton button : buttons) {
				if (button.hasChanged()) {
					changes.put(button.getKey(), button.getValue());
				}
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Enter a whole number for each numeric setting.");
			return;
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		if (!changes.isEmpty()) {
			try {
				for (Entry<String, Object> change : changes.entrySet()) {
					voteSitesConfig.set("VoteSites." + voteSiteName + "." + change.getKey(), change.getValue());
				}
				voteSitesConfig.save();
				for (SettingButton button : buttons) {
					if (changes.containsKey(button.getKey())) {
						button.updateValue();
					}
				}
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
