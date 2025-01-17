package com.bencodez.votingplugineditor.files;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.rewards.AddEditor;
import com.bencodez.votingplugineditor.rewards.RemoveEditor;
import com.bencodez.votingplugineditor.votesites.VoteSiteEditor;

public class VoteSitesConfig extends YmlConfigHandler {
	public VoteSitesConfig(String filePath) {
		super(filePath);
	}

	@Override
	public void openEditorGUI() {
		JFrame editorFrame = new JFrame("Editing VoteSites - " + new File(filePath).getName());
		editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// editorFrame.setLayout(new GridLayout(4, 1));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		int size = 60;

		JButton addButton = new JButton("Add VoteSite");
		addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
		addButton.addActionListener(event -> {
			new AddEditor("Add VoteSite") {

				@Override
				public void onAdd(String name) {
					set("VoteSites." + name + ".Enabled", true);
					set("VoteSites." + name + ".VoteDelay", 24);
					set("VoteSites." + name + ".Name", name);
					set("VoteSites." + name + ".DisplayItem.Material", "DIAMOND");
					set("VoteSites." + name + ".DisplayItem.Amount", 1);
					set("VoteSites." + name + ".VoteURL", "PLEASE SET");
					set("VoteSites." + name + ".ServiceSite", "PLEASE SET");
					set("VoteSites." + name + ".Rewards.Messages.Player", "You voted");

					save();
					editorFrame.dispose();
					openEditorGUI();
				}
			};
		});
		panel.add(addButton);

		Map<String, Object> map = (Map<String, Object>) get("VoteSites", new HashMap<String, Object>());

		JButton removeButton = new JButton("Remove VoteSite");
		removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeButton.getPreferredSize().height));
		removeButton.addActionListener(event -> {
			new RemoveEditor("Remove VoteGUI Slot", PanelUtils.convertSetToArray(map.keySet())) {

				@Override
				public void onRemove(String name) {
					remove("VoteSites." + name);
					save();
					editorFrame.dispose();
					openEditorGUI();
				}
			};
		});
		panel.add(removeButton);

		for (String voteSite : map.keySet()) {
			JButton voteSiteButton = new JButton(voteSite);

			voteSiteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			voteSiteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, voteSiteButton.getPreferredSize().height));
			voteSiteButton.setSize(300, 30);
			voteSiteButton.setVerticalTextPosition(SwingConstants.CENTER);
			size = size + voteSiteButton.getHeight();

			voteSiteButton.addActionListener(event -> {
				System.out.println(voteSite);
				new VoteSiteEditor(this, voteSite);
			});
			panel.add(voteSiteButton);

			// Add some spacing between buttons (optional)
			editorFrame.add(Box.createRigidArea(new Dimension(0, 5)));

			editorFrame.add(panel);
		}

		// System.out.println("" + map.toString());

		editorFrame.setSize(300, size);
		// editorFrame.add(saveButton, BorderLayout.SOUTH);
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}
}
