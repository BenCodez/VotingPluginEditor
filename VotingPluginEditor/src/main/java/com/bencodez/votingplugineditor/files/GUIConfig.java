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

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.ItemEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

public class GUIConfig extends YmlConfigHandler {

	private final List<SettingButton> settingButtons;

	public GUIConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		this.settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("GUI.yml Editor");
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

		Map<String, Object> data = getConfigData();

		// GUIMethod Settings
		panel.add(PanelUtils.createSectionLabel("GUIMethod"));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Today", data, "Today GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.TopVoter", data, "TopVoter GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Last", data, "Last GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Next", data, "Next GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Total", data, "Total GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.URL", data, "VoteURL GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST", "BOOK" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Best", data, "Best GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Streak", data, "Streak GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.GUI", data, "Main GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));

		// LastMonthGUI Setting
		// panel.add(createSectionLabel("LastMonthGUI"));
		settingButtons.add(new BooleanSettingButton(panel, "LastMonthGUI", data, "Enable LastMonthGUI:"));

		JButton editVoteGUI = new JButton("Edit VoteGUI");
		editVoteGUI.addActionListener(event -> {
			openVoteGUIEditor();
		});
		panel.add(editVoteGUI);

		// (Add more settings as needed, organized into sections)

		panel.add(Box.createVerticalStrut(10)); // Spacer
		return panel;
	}

	public void openVoteGUIEditor() {
		JFrame frame = new JFrame("GUI Config Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		Map<String, Object> map = (Map<String, Object>) get("CHEST.VoteGUI", new HashMap<String, Object>());

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(frame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("CHEST.VoteGUI." + name);
				save();
				frame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				set("CHEST.VoteGUI." + name + ".Item.Material", "STONE");
				set("CHEST.VoteGUI." + name + ".Item.Amount", 1);
				save();
				frame.dispose();
				openVoteGUIEditor();
			}

			@Override
			public void onItemSelect(String name) {
				new ItemEditor((Map<String, Object>) get(map, name + ".Item", new HashMap<String, Object>())) {

					@Override
					public void saveChanges(Map<String, Object> changes) {
						try {
							for (Entry<String, Object> change : changes.entrySet()) {
								set("CHEST.VoteGUI." + name + ".Item." + change.getKey(), change.getValue());
							}
							save();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void removeItemPath(String path) {
						remove("CHEST.VoteGUI." + name + ".Item." + path);
						save();
					}
				};
			}
		};

		panel.add(addRemoveEditor.getAddButton("Add Item/Slot", "Add Item/Slot"));
		panel.add(addRemoveEditor.getRemoveButton("Remove Item/Slot", "Remove Item/Slot",
				PanelUtils.convertSetToArray(map.keySet())));

		addRemoveEditor.getOptionsButtons(panel, PanelUtils.convertSetToArray(map.keySet()));

		frame.add(panel);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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