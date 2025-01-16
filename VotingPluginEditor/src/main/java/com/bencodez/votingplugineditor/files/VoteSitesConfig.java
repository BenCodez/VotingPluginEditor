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

import com.bencodez.votingplugineditor.YmlConfigHandler;
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
		int size = 30;

		JButton newVoteSite = new JButton("Add VoteSite");
		newVoteSite.setMaximumSize(new Dimension(Integer.MAX_VALUE, newVoteSite.getPreferredSize().height));
		newVoteSite.setSize(300, 30);
		panel.add(newVoteSite);

		Map<String, Object> map = (Map<String, Object>) get("VoteSites", new HashMap<String, Object>());
		for (String voteSite : map.keySet()) {
			JButton voteSiteButton = new JButton(voteSite);
			size = size + 30;
			voteSiteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			voteSiteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, voteSiteButton.getPreferredSize().height));
			voteSiteButton.setSize(300, 30);
			voteSiteButton.setVerticalTextPosition(SwingConstants.CENTER);

			voteSiteButton.addActionListener(event -> {
				System.out.println(voteSite);
				new VoteSiteEditor(this, voteSite);
			});
			panel.add(voteSiteButton);

			// Add some spacing between buttons (optional)
			editorFrame.add(Box.createRigidArea(new Dimension(0, 5)));

			editorFrame.add(panel);
		}

		//System.out.println("" + map.toString());

		editorFrame.setSize(300, size);
		// editorFrame.add(saveButton, BorderLayout.SOUTH);
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}
}
