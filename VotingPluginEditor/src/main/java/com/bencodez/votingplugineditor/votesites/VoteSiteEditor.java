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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.api.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.IntSettingButton;
import com.bencodez.votingplugineditor.api.SettingButton;
import com.bencodez.votingplugineditor.api.StringSettingButton;
import com.bencodez.votingplugineditor.files.VoteSitesConfig;
import com.bencodez.votingplugineditor.rewards.RewardEditor;

public class VoteSiteEditor {

	private static Map<String, Object> initialState;
	private static JCheckBox enabledCheckbox;
	private static JTextField nameField;
	private static JTextField serviceSiteField;
	private static JTextField voteURLField;
	private static JTextField voteDelayField;

	// Advanced Options
	private static JCheckBox waitUntilVoteDelayCheckbox;
	private static JCheckBox voteDelayDailyCheckbox;
	private static JCheckBox forceOfflineCheckbox;
	private static JTextField materialField;
	private static JTextField amountField;
	private static JCheckBox hiddenCheckbox;
	private static JTextField priorityField;
	private static JPanel advancedPanel;
	private static boolean advancedVisible = false;

	private List<SettingButton> buttons;

	public VoteSiteEditor(VoteSitesConfig voteSitesConfig, String siteName) {
		Map<String, Object> siteData = (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName,
				new HashMap<>());
		buttons = new ArrayList<SettingButton>();
		SwingUtilities.invokeLater(() -> createAndShowGUI(siteName, siteData, voteSitesConfig));
	}

	private void createAndShowGUI(String siteName, Map<String, Object> siteData, VoteSitesConfig voteSitesConfig) {
		JFrame frame = new JFrame("VoteSite Editor - " + siteName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());

		JPanel panel = createMainPanel(siteName, siteData, voteSitesConfig);

		// Add panel to frame
		frame.add(panel, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		loadInitialState(siteData);
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

		buttons.add(new IntSettingButton(panel, "VoteDelay", siteData, "VoteDelay:", 24));

		JButton rewardsEdit = new JButton("Edit Rewards");
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> {
			new RewardEditor((Map<String, Object>) siteData.get("Rewards")) {

				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							System.out.println("VoteSites." + voteSiteName + ".Rewards." + change.getKey() + " = "
									+ change.getValue());
							boolean isInt = false;
							try {
								Integer.parseInt((String) change.getValue());
								isInt = true;
							} catch (Exception e) {

							}
							if (isInt) {
								voteSitesConfig.set("VoteSites." + voteSiteName + ".Rewards." + change.getKey(),
										Integer.parseInt((String) change.getValue()));
							} else {
								voteSitesConfig.set("VoteSites." + voteSiteName + ".Rewards." + change.getKey(),
										change.getValue());
							}
						}
						voteSitesConfig.save();
						initialState.putAll(changes);
						JOptionPane.showMessageDialog(null, "Changes have been saved.");
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Failed to save changes.");
					}
				}
			};
		});
		panel.add(rewardsEdit);

		panel.add(Box.createVerticalStrut(10));

		// Advanced Options Button
		JButton advancedButton = new JButton("Advanced Options");
		advancedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		advancedButton.addActionListener(e -> toggleAdvancedOptions());
		panel.add(advancedButton);
		panel.add(Box.createVerticalStrut(10));

		// Advanced Options Panel
		advancedPanel = createAdvancedOptionsPanel(siteData);
		panel.add(advancedPanel);
		advancedPanel.setVisible(advancedVisible);
		panel.add(Box.createVerticalStrut(20));

		// Save Button
		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> saveChanges(voteSiteName, voteSitesConfig));
		panel.add(saveButton);

		return panel;
	}

	private JTextField createLabelAndTextField(JPanel panel, String labelText, String initialValue) {
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		subPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
		label.setAlignmentY(Component.CENTER_ALIGNMENT);

		JTextField textField = new JTextField(initialValue);
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
		textField.setAlignmentY(Component.CENTER_ALIGNMENT);

		subPanel.add(label);
		subPanel.add(Box.createHorizontalStrut(10));
		subPanel.add(textField);

		panel.add(subPanel);
		return textField;
	}

	private JPanel createAdvancedOptionsPanel(Map<String, Object> siteData) {
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
		advancedPanel.setBorder(BorderFactory.createTitledBorder("Advanced Options"));
		buttons.add(new BooleanSettingButton(advancedPanel, "WaitUntilVoteDelay", siteData,
				"Wait Until Vote Delay (Blocks votes with VoteDelay):"));

		buttons.add(new BooleanSettingButton(advancedPanel, "VoteDelayDaily", siteData,
				"VoteDelayDaily (Makes vote delay work based on time of day):"));

		buttons.add(new BooleanSettingButton(advancedPanel, "ForceOffline", siteData,
				"ForceOffline (Forces runs rewards while player is offline):"));

		buttons.add(new BooleanSettingButton(advancedPanel, "Hidden", siteData,
				"Hidden (Hide votesite in GUI's and from counters):"));

		buttons.add(new IntSettingButton(advancedPanel, "Priority", siteData, "Priority (Used to orders sites in GUI):",
				5));

		materialField = createLabelAndTextField(advancedPanel, "DisplayItem Material:",
				PanelUtils.getStringValue(siteData, "DisplayItem.Material", "DIAMOND"));

		buttons.add(new StringSettingButton(advancedPanel, "DisplayItem.Material", siteData,
				"Display Item Material (Used in certain GUI's", "DIAMOND"));

		buttons.add(new IntSettingButton(advancedPanel, "DisplayItem.Amount", siteData,
				"Display Item Amount (Used in certain GUI's):", 1));

		return advancedPanel;
	}

	private void toggleAdvancedOptions() {
		advancedVisible = !advancedVisible;
		advancedPanel.setVisible(advancedVisible);
	}

	private void loadInitialState(Map<String, Object> siteData) {
		initialState = new HashMap<>(siteData);
	}

	private void saveChanges(String voteSiteName, VoteSitesConfig voteSitesConfig) {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : buttons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}

		}

		// Notify & save changes
		if (!changes.isEmpty()) {
			try {
				for (Entry<String, Object> change : changes.entrySet()) {
					System.out.println("VoteSites." + voteSiteName + "." + change.getKey() + " = " + change.getValue());
					boolean isInt = false;
					try {
						Integer.parseInt((String) change.getValue());
						isInt = true;
					} catch (Exception e) {

					}
					if (isInt) {
						voteSitesConfig.set("VoteSites." + voteSiteName + "." + change.getKey(),
								Integer.parseInt((String) change.getValue()));
					} else {
						voteSitesConfig.set("VoteSites." + voteSiteName + "." + change.getKey(), change.getValue());
					}
				}
				voteSitesConfig.save();
				initialState.putAll(changes);
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