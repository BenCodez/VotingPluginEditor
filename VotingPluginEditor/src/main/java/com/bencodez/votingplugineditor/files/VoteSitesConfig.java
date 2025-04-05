package com.bencodez.votingplugineditor.files;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.misc.LoadingDialog;
import com.bencodez.votingplugineditor.api.misc.PanelUtils;
import com.bencodez.votingplugineditor.api.misc.ServiceSiteHandler;
import com.bencodez.votingplugineditor.api.misc.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;
import com.bencodez.votingplugineditor.votesites.VoteSiteEditor;

public class VoteSitesConfig extends YmlConfigHandler {
	public VoteSitesConfig(String filePath, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
	}

	@Override
	public void openEditorGUI() {
		JFrame editorFrame = new JFrame("Editing VoteSites - " + new File(filePath).getName());
		editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		Map<String, Object> map = (Map<String, Object>) get("VoteSites", new HashMap<String, Object>());
		int size = 180 + map.size() * 30;

		VoteSitesConfig config = this;

		editorFrame.setSize(300, size);

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(editorFrame.getWidth()) {

			@Override
			public void onItemRemove(String name) {
				remove("VoteSites." + name);
				save();
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemAdd(String name) {
				if (map.containsKey(name)) {
					JOptionPane.showMessageDialog(panel, "VoteSite already exists");

				} else {
					name = name.replaceAll(".", "_");
					set("VoteSites." + name + ".Enabled", true);
					set("VoteSites." + name + ".VoteDelay", 24);
					set("VoteSites." + name + ".Name", name);
					set("VoteSites." + name + ".DisplayItem.Material", "DIAMOND");
					set("VoteSites." + name + ".DisplayItem.Amount", 1);
					set("VoteSites." + name + ".VoteURL", "PLEASE SET");
					set("VoteSites." + name + ".ServiceSite", "PLEASE SET");
					set("VoteSites." + name + ".Rewards.Messages.Player", "You voted");

					save();
				}
				editorFrame.dispose();
				openEditorGUI();
			}

			@Override
			public void onItemSelect(String name) {
				new VoteSiteEditor(config, name);
			}
		};

		panel.add(addRemoveEditor.getAddButton("Add VoteSite", "Add VoteSite"));
		JButton addFromPresetButton = new JButton("Add VoteSite from Preset");
		addFromPresetButton.setHorizontalAlignment(SwingConstants.CENTER);
		addFromPresetButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, addFromPresetButton.getPreferredSize().height));
		addFromPresetButton.setAlignmentY(Component.CENTER_ALIGNMENT);

		addFromPresetButton.addActionListener(event -> {
			JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
			LoadingDialog loadingDialog = new LoadingDialog(parentFrame);

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					ServiceSiteHandler serviceSiteHandler = new ServiceSiteHandler();
					HashSet<String> existingServiceSites = new HashSet<>();

					for (Entry<String, Object> entry : map.entrySet()) {
						Map<String, Object> voteSite = (Map<String, Object>) entry.getValue();
						if (voteSite.containsKey("ServiceSite")) {
							existingServiceSites.add(((String) voteSite.get("ServiceSite")).toLowerCase());
						}
					}

					String[] presets = serviceSiteHandler.getServiceSites().entrySet().stream()
							.filter(entry -> !existingServiceSites.contains(entry.getValue().toLowerCase()))
							.map(Entry::getKey).toArray(String[]::new);

					SwingUtilities.invokeLater(() -> {
						loadingDialog.dispose();
						String selectedPreset = (String) JOptionPane.showInputDialog(panel, "Select a VoteSite to add:",
								"Add VoteSite from Preset", JOptionPane.PLAIN_MESSAGE, null, presets,
								presets.length > 0 ? presets[0] : null);
						if (selectedPreset != null && !selectedPreset.isEmpty()) {
							String name = selectedPreset.replaceAll("\\.", "_").replaceAll("\"", "");
							set("VoteSites." + name + ".Enabled", true);
							set("VoteSites." + name + ".VoteDelay", 24);
							set("VoteSites." + name + ".Name", name);
							set("VoteSites." + name + ".DisplayItem.Material", "DIAMOND");
							set("VoteSites." + name + ".DisplayItem.Amount", 1);
							set("VoteSites." + name + ".VoteURL", selectedPreset.replace("\"", ""));
							set("VoteSites." + name + ".ServiceSite",
									serviceSiteHandler.getServiceSites().get(selectedPreset).replace("\"", ""));
							set("VoteSites." + name + ".Rewards.Messages.Player", "You voted");

							save();
							editorFrame.dispose();
							openEditorGUI();
						}
					});
					return null;
				}

				@Override
				protected void done() {
					loadingDialog.dispose();
				}
			};

			worker.execute();
			loadingDialog.setVisible(true);
		});

		panel.add(addFromPresetButton);
		panel.add(addRemoveEditor.getRemoveButton("Remove VoteSite", "Remove VoteSite",
				PanelUtils.convertSetToArray(map.keySet())));

		panel.add(Box.createRigidArea(new Dimension(0, 15)));

		// Add the new button for EverySiteReward
		JButton everySiteRewardButton = new JButton("Edit EverySiteReward");
		everySiteRewardButton.setHorizontalAlignment(SwingConstants.CENTER);
		everySiteRewardButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, everySiteRewardButton.getPreferredSize().height));
		everySiteRewardButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		everySiteRewardButton.addActionListener(event -> {
			new RewardEditor(get("EverySiteReward"), "EverySiteReward") {

				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							set("EverySiteReward." + change.getKey(), change.getValue());
						}
						save();
						editorFrame.dispose();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void removePath(String path) {
					remove("EverySiteReward." + path);
					save();
				}

				@Override
				public Map<String, Object> updateData() {
					load();
					return (Map<String, Object>) get("EverySiteReward");
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
		panel.add(everySiteRewardButton);

		panel.add(Box.createRigidArea(new Dimension(0, 15)));

		addRemoveEditor.getOptionsButtons(panel, PanelUtils.convertSetToArray(map.keySet()));

		editorFrame.add(panel);
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}

}
