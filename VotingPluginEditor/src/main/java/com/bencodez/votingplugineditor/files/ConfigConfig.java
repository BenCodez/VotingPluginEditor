
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
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;

public class ConfigConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	public ConfigConfig(String filePath) {
		super(filePath);
		settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("Config.yml Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JPanel mainPanel = createMainEditorPanel();
		frame.add(mainPanel, BorderLayout.CENTER);

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

		settingButtons.add(new BooleanSettingButton(panel, "AdvancedServiceSiteHandling", getConfigData(),
				"Advanced Service Site Handling"));
		settingButtons.add(new BooleanSettingButton(panel, "StoreMonthTotalsWithDate", getConfigData(),
				"Store Month Totals With Date"));
		settingButtons.add(new BooleanSettingButton(panel, "UseMonthDateTotalsAsPrimaryTotal", getConfigData(),
				"Use Month Date Totals As Primary Total"));

		settingButtons.add(new StringSettingButton(panel, "DataStorage", getConfigData(), "Data Storage", "SQLITE",
				new String[] { "SQLITE", "MYSQL" }));

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);

		panel.add(createMySQLSettingsPanel());
		panel.add(createVoteRemindingPanel());

		return panel;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getConfigData(String path) {
		Object data = get(path);
		if (data instanceof Map) {
			return (Map<String, Object>) data;
		}
		return new HashMap<>();
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

		voteRemindingPanel.setVisible(false); // Initially hide the panel

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		this.settingButtons.addAll(settingButtons);

		JButton toggleButton = new JButton("Show/Hide Vote Reminding Settings");
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
		toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleButton.getPreferredSize().height));
		toggleButton.addActionListener(event -> voteRemindingPanel.setVisible(!voteRemindingPanel.isVisible()));

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(toggleButton);
		containerPanel.add(voteRemindingPanel);

		return containerPanel;
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
			new RewardEditor(getConfigData(path)) {

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
					remove(path + "." + path);
					save();
				}

				@Override
				public Map<String, Object> updateData() {
					return getConfigData(path);
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
