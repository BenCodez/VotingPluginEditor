package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.JavascriptAwareItemEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

import lombok.Getter;

public class ShopConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	private JFrame frame;

	public ShopConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		settingButtons = new ArrayList<SettingButton>();
		changes = new HashMap<String, Object>();
	}

	@Getter
	private Map<String, Object> changes;

	@Override
	public void openEditorGUI() {
		frame = new JFrame("Shop.yml VoteShop Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Shops", createMainEditorPanel());
		tabbedPane.addTab("Global Settings", createGlobalSettingsPanel());

		frame.add(tabbedPane, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createGlobalSettingsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(PanelUtils.createSectionLabel("General VoteShop Settings"));

		settingButtons.add(new BooleanSettingButton(panel, "VoteShop.Enabled", getConfigData(), "VoteShop Enabled"));
		settingButtons.add(new StringSettingButton(panel, "VoteShop.Name", getConfigData(), "VoteShop GUI Name", "VoteShop"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteShop.BackButton", getConfigData(), "VoteShop Back Button"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteShop.HideLimitedReached", getConfigData(),
				"Hide items when their purchase limit is reached"));
		settingButtons.add(new StringSettingButton(panel, "VoteShop.LimitReached", getConfigData(),
				"VoteShop Limit Reached Message", "&aYou reached your limit"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteShop.RequireConfirmation", getConfigData(),
				"Require confirmation by default"));
		settingButtons.add(new StringSettingButton(panel, "VoteShop.Disabled", getConfigData(),
				"VoteShop Disabled Message", "&cVote shop disabled"));
		settingButtons.add(new BooleanSettingButton(panel, "VoteShop.ReopenGUIOnPurchase", getConfigData(),
				"Reopen GUI After Purchase"));
		settingButtons.add(new BooleanSettingButton(panel, "ShopConfirmPurchase.UseDialog", getConfigData(),
				"Use Dialog For Purchase Confirmation"));
		settingButtons.add(new StringSettingButton(panel, "ShopConfirmPurchase.Title", getConfigData(),
				"Confirmation Title", "Confirm Purchase?"));

		JPanel backButtonPanel = new JPanel();
		backButtonPanel.setLayout(new BoxLayout(backButtonPanel, BoxLayout.X_AXIS));
		JButton backButton = new JButton("Edit Default Back Button Item");
		backButton.addActionListener(e -> openItemEditor("VoteShop.BackButtonItem"));
		backButtonPanel.add(backButton);
		panel.add(backButtonPanel);

		JPanel confirmPanel = new JPanel();
		confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));

		JButton yesButton = new JButton("Confirmation Yes Item");
		yesButton.addActionListener(e -> openItemEditor("ShopConfirmPurchase.YesItem"));
		confirmPanel.add(yesButton);

		JButton noButton = new JButton("Confirmation No Item");
		noButton.addActionListener(e -> openItemEditor("ShopConfirmPurchase.NoItem"));
		confirmPanel.add(noButton);

		panel.add(confirmPanel);

		PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);
		return panel;
	}

	private void openItemEditor(String path) {
		Object current = get(path, new LinkedHashMap<String, Object>());
		Map<String, Object> itemData = current instanceof Map
				? (Map<String, Object>) current
				: new LinkedHashMap<String, Object>();

		new JavascriptAwareItemEditor(itemData, getConfigFilePath(), getPluginDirectory(), getSFTPSettings()) {
			@Override
			public void saveChanges(Map<String, Object> itemChanges) {
				for (Entry<String, Object> change : itemChanges.entrySet()) {
					getChanges().put(path + "." + change.getKey(), change.getValue());
				}
				if (!itemChanges.isEmpty()) {
					saveChange();
				}
			}

			@Override
			public void removeItemPath(String subPath) {
				remove(path + "." + subPath);
				save();
			}
		};
	}

	private String getConfigFilePath() {
		File parent = new File(filePath).getParentFile();
		return new File(parent == null ? new File(".") : parent, "Config.yml").getPath();
	}

	private void openShopEditor(String shop) {
		JFrame shopFrame = new JFrame("Shop: " + shop);
		shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		shopFrame.setSize(800, 600);
		shopFrame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new StringSettingButton(panel, "Shop." + shop + ".Identifier_Name", getConfigData(),
				"Shop identifier display name", shop));
		settingButtons.add(new IntSettingButton(panel, "Shop." + shop + ".Cost", getConfigData(), "Cost", 0));
		settingButtons.add(new StringSettingButton(panel, "Shop." + shop + ".Permission", getConfigData(),
				"Permission to view", ""));
		settingButtons.add(new BooleanSettingButton(panel, "Shop." + shop + ".HideOnNoPermission", getConfigData(),
				"Hide when player lacks permission"));
		settingButtons.add(new StringSettingButton(panel, "Shop." + shop + ".Category", getConfigData(),
				"Category to open instead of purchasing", ""));
		settingButtons.add(new BooleanSettingButton(panel, "Shop." + shop + ".RequireConfirmation", getConfigData(),
				"Require confirmation before purchase"));
		settingButtons.add(new BooleanSettingButton(panel, "Shop." + shop + ".CloseGUI", getConfigData(),
				"Close GUI on purchase"));

		JButton itemsButton = new JButton("Edit Display Item");
		itemsButton.addActionListener(event -> openItemEditor("Shop." + shop + ".DisplayItem"));
		panel.add(itemsButton);

		panel.add(addRewardsButton("Shop." + shop + ".Rewards", "Shop Rewards: " + shop));

		shopFrame.add(panel);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		shopFrame.add(saveButton, BorderLayout.SOUTH);

		shopFrame.setLocationRelativeTo(null);
		shopFrame.setVisible(true);
	}

	private JPanel createMainEditorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		Object rawShopData = getConfigData().get("Shop");
		Map<String, Object> shopData = rawShopData instanceof Map
				? (Map<String, Object>) rawShopData
				: new LinkedHashMap<String, Object>();

		AddRemoveEditor editor = new AddRemoveEditor(frame.getWidth()) {
			@Override
			public void onItemSelect(String name) {
				openShopEditor(name);
			}

			@Override
			public void onItemRemove(String name) {
				remove("Shop." + name);
				save();
				frame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				if (shopData.containsKey(name)) {
					JOptionPane.showMessageDialog(panel, "Shop already exists");
				} else {
					set("Shop." + name + ".Identifier_Name", name);
					set("Shop." + name + ".DisplayItem.Material", "STONE");
					set("Shop." + name + ".DisplayItem.Amount", 1);
					set("Shop." + name + ".DisplayItem.Name", "Example");
					set("Shop." + name + ".Cost", 3);
					set("Shop." + name + ".Permission", "");
					set("Shop." + name + ".HideOnNoPermission", true);
					set("Shop." + name + ".CloseGUI", true);
					set("Shop." + name + ".RequireConfirmation", false);
					set("Shop." + name + ".Rewards.Commands", new String[] { "example command" });
					save();
				}
				frame.dispose();
				openEditorGUI();
			}
		};

		panel.add(editor.getAddButton("Add A Shop", "Add VoteShop"));
		panel.add(editor.getRemoveButton("Remove a Shop", "Remove a Shop", shopData.keySet()));
		panel.add(Box.createRigidArea(new Dimension(0, 15)));
		panel.add(new JLabel("Click to edit shop:"));
		editor.getOptionsButtons(panel, PanelUtils.convertSetToArray(shopData.keySet()));

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
				public void saveChanges(Map<String, Object> rewardChanges) {
					try {
						for (Entry<String, Object> change : rewardChanges.entrySet()) {
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

	private void saveChange() {
		saveChanges();
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : settingButtons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}
		}

		changes.putAll(this.changes);
		this.changes.clear();

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
