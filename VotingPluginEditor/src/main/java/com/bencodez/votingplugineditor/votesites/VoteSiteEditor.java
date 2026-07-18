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

	public VoteSiteEditor(VoteSitesConfig voteSitesConfig, String siteName) {
		Map<String, Object> siteData = (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName,
				new HashMap<>());
		buttons = new ArrayList<SettingButton>();
		this.siteName = siteName;
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
		buttons.add(new StringSettingButton(panel, "VoteDelay", siteData, "Vote Delay:", "24h"));

		JButton rewardsEdit = new JButton("Edit Rewards");
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> {
			new RewardEditor(siteData.get("Rewards"), voteSiteName + ".Rewards") {
				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							voteSitesConfig.set("VoteSites." + voteSiteName + ".Rewards." + change.getKey(),
									change.getValue());
						}
						voteSitesConfig.save();
						JOptionPane.showMessageDialog(null, "Changes have been saved.");
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Failed to save changes.");
					}
				}

				@Override
				public void removePath(String path) {
					voteSitesConfig.remove("VoteSites." + voteSiteName + ".Rewards." + path);
					voteSitesConfig.save();
				}

				@Override
				public Map<String, Object> updateData() {
					return (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName + ".Rewards",
							new HashMap<>());
				}

				@Override
				public String getVotingPluginDirectory() {
					return voteSitesConfig.getPluginDirectory();
				}

				@Override
				public SFTPSettings getSFTPSetting() {
					return voteSitesConfig.getSFTPSettings();
				}
			};
		});

		panel.add(rewardsEdit);
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
		buttons.add(new IntSettingButton(advancedPanel, "VoteDelayDailyHour", siteData, "Vote Delay Daily Hour:", 1,
				"Server hour from 1-24 used when VoteDelayDaily is enabled"));
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

		JButton coolDownEndRewardsEdit = new JButton("Edit CoolDownEndRewards");
		coolDownEndRewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		coolDownEndRewardsEdit
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, coolDownEndRewardsEdit.getPreferredSize().height));
		coolDownEndRewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		coolDownEndRewardsEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
		coolDownEndRewardsEdit.addActionListener(event -> {
			new RewardEditor(siteData.get("CoolDownEndRewards"), siteName + ".CoolDownEndRewards") {
				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							voteSitesConfig.set("VoteSites." + siteName + ".CoolDownEndRewards." + change.getKey(),
									change.getValue());
						}
						voteSitesConfig.save();
						JOptionPane.showMessageDialog(null, "Changes have been saved.");
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Failed to save changes.");
					}
				}

				@Override
				public void removePath(String path) {
					voteSitesConfig.remove("VoteSites." + siteName + ".CoolDownEndRewards." + path);
					voteSitesConfig.save();
				}

				@Override
				public Map<String, Object> updateData() {
					return (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName + ".CoolDownEndRewards",
							new HashMap<>());
				}

				@Override
				public String getVotingPluginDirectory() {
					return voteSitesConfig.getPluginDirectory();
				}

				@Override
				public SFTPSettings getSFTPSetting() {
					return voteSitesConfig.getSFTPSettings();
				}
			};
		});
		advancedPanel.add(coolDownEndRewardsEdit);
		return advancedPanel;
	}

	private void saveChanges(String voteSiteName, VoteSitesConfig voteSitesConfig) {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : buttons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}
		}

		if (!changes.isEmpty()) {
			try {
				for (Entry<String, Object> change : changes.entrySet()) {
					voteSitesConfig.set("VoteSites." + voteSiteName + "." + change.getKey(), change.getValue());
				}
				voteSitesConfig.save();
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
