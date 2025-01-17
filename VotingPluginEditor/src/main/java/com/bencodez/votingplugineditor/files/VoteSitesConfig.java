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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.add.AddEditor;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.api.edit.add.RemoveEditor;
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

		Map<String, Object> map = (Map<String, Object>) get("VoteSites", new HashMap<String, Object>());

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor() {

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
		};

		panel.add(addRemoveEditor.getAddButton("Add VoteSite", "Add VoteSite"));
		panel.add(addRemoveEditor.getRemoveButton("Remove VoteSite", "Remove VoteSite",
				PanelUtils.convertSetToArray(map.keySet())));

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
