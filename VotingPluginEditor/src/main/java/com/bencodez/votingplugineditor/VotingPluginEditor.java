package com.bencodez.votingplugineditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.generator.MaterialLoader;
import com.bencodez.votingplugineditor.generator.PotionLoader;

import lombok.Getter;

public class VotingPluginEditor {
	@Getter
	private static List<String> materials;
	@Getter
	private static List<String> potionEffects;

	private static final String BACKEND_SERVERS_KEY = "backend_servers";
	@Getter
	private static Preferences prefs = Preferences.userNodeForPackage(VotingPluginEditor.class);

	public static void saveBackendServers(List<String> servers) {
		String serializedServers = String.join(",", servers);
		prefs.put(BACKEND_SERVERS_KEY, serializedServers);
	}

	public static List<String> loadBackendServers() {
		String serializedServers = prefs.get(BACKEND_SERVERS_KEY, "");
		if (serializedServers.isEmpty()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(serializedServers.split(",")));
	}

	private static String getVersionFromPom() {
		try {
			File pomFile = new File("pom.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(pomFile);
			doc.getDocumentElement().normalize();
			Element versionElement = (Element) doc.getElementsByTagName("version").item(0);
			return versionElement.getTextContent();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown";
		}
	}

	public static void main(String[] args) {
		materials = MaterialLoader.loadMaterials();
		potionEffects = PotionLoader.loadPotions();
		loadBackendServers();
		SwingUtilities.invokeLater(VotingPluginEditor::createAndShowGUI);
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("VotingPluginEditor Main Menu");

		ArrayList<String> backendServers = new ArrayList<>(loadBackendServers());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 500 + backendServers.size() * 50);
		frame.setLayout(new GridLayout(6 + backendServers.size(), 1));

		String version = getVersionFromPom();
		JLabel versionLabel = new JLabel("VotingPluginEditor: " + version);
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(versionLabel);

		JLabel secondLine = new JLabel("Ensure you have a backup before editing files.");
		secondLine.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(secondLine);

		AddRemoveEditor addRemoveEditor = new AddRemoveEditor(frame.getWidth()) {

			@Override
			public void onItemSelect(String name) {
				new BackEndServerEditor(name);
			}

			@Override
			public void onItemRemove(String name) {
				backendServers.remove(name);
				saveBackendServers(backendServers);
				frame.dispose();
				createAndShowGUI();
			}

			@Override
			public void onItemAdd(String name) {
				backendServers.add(name);
				saveBackendServers(backendServers);
				frame.dispose();
				createAndShowGUI();
			}
		};

		frame.add(addRemoveEditor.getAddButton("Add Backend Server", "Add Backend Server"));
		frame.add(addRemoveEditor.getRemoveButton("Remove Backend Server", "Remove Backend Server",
				PanelUtils.convertListToArray(backendServers)));

		frame.add(Box.createRigidArea(new Dimension(0, 1)));

		JPanel optionsButtons = new JPanel();
		optionsButtons.setLayout(new BoxLayout(optionsButtons, BoxLayout.Y_AXIS));

		optionsButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		addRemoveEditor.getOptionsButtons(optionsButtons, PanelUtils.convertListToArray(backendServers));
		frame.add(optionsButtons);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
