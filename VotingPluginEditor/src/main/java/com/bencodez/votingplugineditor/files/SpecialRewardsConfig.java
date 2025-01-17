package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;

public class SpecialRewardsConfig extends YmlConfigHandler {
	public SpecialRewardsConfig(String filePath) {
		super(filePath);
	}

	JFrame editorFrame;

	@Override
	public void openEditorGUI() {
		editorFrame = new JFrame("Editing SpecialRewards.yml - " + new File(filePath).getName());
		editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editorFrame.setSize(300, 600);

		// editorFrame.setLayout(new GridLayout(4, 1));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(addRewardsButton("FirstVote", "FirstVote Rewards"));

		panel.add(addRewardsButton("FirstVoteToday", "FirstVoteToday Rewards"));

		panel.add(addRewardsButton("AllSites", "AllSites Rewards"));

		panel.add(addRewardsButton("AlmostAllSites", "AlmostAllSites Rewards"));

		editorFrame.add(panel);

		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}

	public JButton addRewardsButton(String path, String name) {
		JButton rewardsEdit = new JButton(name);
		rewardsEdit.setHorizontalAlignment(SwingConstants.CENTER);
		rewardsEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, rewardsEdit.getPreferredSize().height));
		rewardsEdit.setAlignmentY(Component.CENTER_ALIGNMENT);
		rewardsEdit.addActionListener(event -> {
			new RewardEditor((Map<String, Object>) get(path)) {

				@Override
				public void saveChanges(Map<String, Object> changes) {
					try {
						for (Entry<String, Object> change : changes.entrySet()) {
							set(path + "." + change.getKey(), change.getValue());
						}
						save();
						editorFrame.dispose();
						openEditorGUI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void removePath(String path1) {
					remove(path + "." + path1);
					save();
				}

				@Override
				public Map<String, Object> updateData() {
					load();
					return (Map<String, Object>) get(path);
				}
			};
		});
		return rewardsEdit;
	}
}
