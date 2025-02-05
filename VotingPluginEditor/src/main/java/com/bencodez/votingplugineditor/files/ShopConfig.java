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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.item.ItemEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.settng.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.settng.IntSettingButton;
import com.bencodez.votingplugineditor.api.settng.SettingButton;
import com.bencodez.votingplugineditor.api.settng.StringSettingButton;

import lombok.Getter;

public class ShopConfig extends YmlConfigHandler {
	private final List<SettingButton> settingButtons;

	private JFrame frame;

	public ShopConfig(String filePath) {
		super(filePath);
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

	  JPanel mainPanel = createMainEditorPanel();
	  tabbedPane.addTab("Shops", mainPanel);

	  JPanel globalPanel = createGlobalSettingsPanel();
	  tabbedPane.addTab("Global Settings", globalPanel);

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
		  settingButtons.add(new BooleanSettingButton(panel, "VoteShop.BackButton", getConfigData(), "Vote Shop BackButton"));
		  settingButtons.add(new BooleanSettingButton(panel, "VoteShop.HideLimitedReached", getConfigData(), "Hide items in vote shop which user can not buy"));
		  settingButtons.add(new StringSettingButton(panel, "VoteShop.LimitReached", getConfigData(), "VoteShop LimitReached", "&aYou reached your limit"));
		  settingButtons.add(new BooleanSettingButton(panel, "VoteShop.RequireConfirmation", getConfigData(), "VoteShop RequireConfirmation (Global setting)"));
		  settingButtons.add(new StringSettingButton(panel, "VoteShop.Disabled", getConfigData(), "VoteShop Disabled Message", "&cVote shop disabled"));
		  settingButtons.add(new BooleanSettingButton(panel, "VoteShop.ReopenGUIOnPurchase", getConfigData(), "ReopenGUIOnPurchase"));
		  settingButtons.add(new BooleanSettingButton(panel, "VoteShop.HideLimitReached", getConfigData(), "HideLimitReached"));
		  settingButtons.add(new StringSettingButton(panel, "ShopConfirmPurchase.Title", getConfigData(), "ShopConfirmPurchase Title", "Confirm Purchase?"));

		  JPanel confirmPanel = new JPanel();
		  confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));

		  JButton yesButton = new JButton("Confirmation Yes Item");
		  yesButton.addActionListener(e -> {
		   new ItemEditor((Map<String, Object>) get("ShopConfirmPurchase.YesItem")) {
		    @Override
		    public void saveChanges(Map<String, Object> changes) {
		     for (Entry<String, Object> change : changes.entrySet()) {
		      getChanges().put("ShopConfirmPurchase.YesItem." + change.getKey(), change.getValue());
		     }
		     if (!changes.isEmpty()) {
		      saveChange();
		     }
		    }

		    @Override
		    public void removeItemPath(String path) {
		     remove("ShopConfirmPurchase.YesItem." + path);
		     save();
		    }
		   };
		  });
		  confirmPanel.add(yesButton);

		  JButton noButton = new JButton("Confirmation No Item");
		  noButton.addActionListener(e -> {
		   new ItemEditor((Map<String, Object>) get("ShopConfirmPurchase.NoItem")) {
		    @Override
		    public void saveChanges(Map<String, Object> changes) {
		     for (Entry<String, Object> change : changes.entrySet()) {
		      getChanges().put("ShopConfirmPurchase.NoItem." + change.getKey(), change.getValue());
		     }
		     if (!changes.isEmpty()) {
		      saveChange();
		     }
		    }

		    @Override
		    public void removeItemPath(String path) {
		     remove("ShopConfirmPurchase.NoItem." + path);
		     save();
		    }
		   };
		  });
		  confirmPanel.add(noButton);

		  panel.add(confirmPanel);
		  
		  PanelUtils.adjustSettingButtonsMaxWidth(settingButtons);

		  return panel;
		 }

	private void openShopEditor(String shop) {
		JFrame frame = new JFrame("Shop: " + shop);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		settingButtons.add(new StringSettingButton(panel, "Shop." + shop + ".Identifier_Name", getConfigData(),
				"Shop identifier display name", shop));
		settingButtons.add(new IntSettingButton(panel, "Shop." + shop + ".Cost", getConfigData(), "Cost", 0));

		settingButtons.add(new StringSettingButton(panel, "Shop." + shop + ".Permission", getConfigData(),
				"Shop permission to view", ""));

		settingButtons.add(new BooleanSettingButton(panel, "Shop." + shop + ".RequireConfirmation", getConfigData(),
				"Require confirmation before purchase"));

		settingButtons.add(
				new BooleanSettingButton(panel, "Shop." + shop + ".CloseGUI", getConfigData(), "CloseGUI on purchase"));

		JButton itemsButton = new JButton("Edit Display Item");
		itemsButton.addActionListener(event -> {
			new ItemEditor((Map<String, Object>) get("Shop." + shop)) {

				@Override
				public void saveChanges(Map<String, Object> changes) {
					for (Entry<String, Object> change : changes.entrySet()) {
						getChanges().put("Shop." + shop + "." + change.getKey(), change.getValue());
					}
					if (!changes.isEmpty()) {
						saveChange();
					}
				}

				@Override
				public void removeItemPath(String path) {
					remove("Shop." + shop + "." + path);
					save();
				}
			};
		});
		panel.add(itemsButton);

		panel.add(addRewardsButton("Shop." + shop, "Shop Rewards: " + shop));

		frame.add(panel);

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
		Map<String, Object> shopData = (Map<String, Object>) data.get("Shop");

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
					set("Shop." + name + ".Cost", 3);
					set("Shop." + name + ".Material", "STONE");
					set("Shop." + name + ".Amount", 1);
					set("Shop." + name + ".Name", "Example");
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