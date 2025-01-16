package com.bencodez.votingplugineditor.votesites;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

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

	public VoteSiteEditor(VoteSitesConfig voteSitesConfig, String siteName) {
		Map<String, Object> siteData = (Map<String, Object>) voteSitesConfig.get("VoteSites." + siteName,
				new HashMap<>());

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

		// Basic GUI components setup with missing value handling
		enabledCheckbox = new JCheckBox("Enabled", PanelUtils.getBooleanValue(siteData, "Enabled"));
		enabledCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(enabledCheckbox);
		panel.add(Box.createVerticalStrut(10));

		nameField = createLabelAndTextField(panel, "Display Name:",
				PanelUtils.getStringValue(siteData, "Name", "ExampleVoteSite"));
		serviceSiteField = createLabelAndTextField(panel, "Service Site:",
				PanelUtils.getStringValue(siteData, "ServiceSite", "PlanetMinecraft.com"));
		voteURLField = createLabelAndTextField(panel, "Vote URL:",
				PanelUtils.getStringValue(siteData, "VoteURL", "http://example-vote-url.com"));
		voteDelayField = createLabelAndTextField(panel, "Vote Delay (hours):",
				String.valueOf(PanelUtils.getIntValue(siteData, "VoteDelay", 24)));

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

		waitUntilVoteDelayCheckbox = new JCheckBox("Wait Until Vote Delay",
				PanelUtils.getBooleanValue(siteData, "WaitUntilVoteDelay"));
		advancedPanel.add(waitUntilVoteDelayCheckbox);
		voteDelayDailyCheckbox = new JCheckBox("Vote Delay Daily",
				PanelUtils.getBooleanValue(siteData, "VoteDelayDaily"));
		advancedPanel.add(voteDelayDailyCheckbox);
		forceOfflineCheckbox = new JCheckBox("Force Offline", PanelUtils.getBooleanValue(siteData, "ForceOffline"));
		advancedPanel.add(forceOfflineCheckbox);

		hiddenCheckbox = new JCheckBox("Hidden", PanelUtils.getBooleanValue(siteData, "Hidden"));
		advancedPanel.add(hiddenCheckbox);
		priorityField = createLabelAndTextField(advancedPanel, "Priority:",
				String.valueOf(PanelUtils.getIntValue(siteData, "Priority", 5)));

		materialField = createLabelAndTextField(advancedPanel, "DisplayItem Material:",
				PanelUtils.getStringValue(siteData, "DisplayItem.Material", "DIAMOND"));
		amountField = createLabelAndTextField(advancedPanel, "DisplayItem Amount:",
				String.valueOf(PanelUtils.getIntValue(siteData, "DisplayItem.Amount", 1)));

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
		Map<String, Object> currentState = new HashMap<>();
		currentState.put("Enabled", enabledCheckbox.isSelected());
		currentState.put("Name", nameField.getText());
		currentState.put("ServiceSite", serviceSiteField.getText());
		currentState.put("VoteURL", voteURLField.getText());
		currentState.put("VoteDelay", voteDelayField.getText());

		// Advanced
		currentState.put("DisplayItem.Amount", amountField.getText());
		currentState.put("WaitUntilVoteDelay", waitUntilVoteDelayCheckbox.isSelected());
		currentState.put("VoteDelayDaily", voteDelayDailyCheckbox.isSelected());
		currentState.put("ForceOffline", forceOfflineCheckbox.isSelected());
		currentState.put("DisplayItem.Material", materialField.getText());
		currentState.put("Hidden", hiddenCheckbox.isSelected());
		currentState.put("Priority", priorityField.getText());

		// Determining changes
		Map<String, Object> changes = new HashMap<>();
		for (String key : currentState.keySet()) {
			if (initialState.containsKey(key)) {
				if (!Objects.equals(initialState.get(key), currentState.get(key))) {
					changes.put(key, currentState.get(key));
				}
			} else if (initialState.containsKey(key.split(Pattern.quote("."))[0])) {
				Object s = PanelUtils.getState(key, initialState);
				if (s != null) {
					if (!Objects.equals(s, currentState.get(key))) {
						changes.put(key, currentState.get(key));
					}
				}
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