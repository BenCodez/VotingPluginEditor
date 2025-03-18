package com.bencodez.votingplugineditor;

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
import com.bencodez.votingplugineditor.server.BackEndServerEditor;
import com.bencodez.votingplugineditor.server.ProxyEditor;

import lombok.Getter;

public class VotingPluginEditor {
	@Getter
	private static List<String> materials;
	@Getter
	private static List<String> potionEffects;

	private static final String BACKEND_SERVERS_KEY = "backend_servers";
	@Getter
	private static Preferences prefs = Preferences.userNodeForPackage(VotingPluginEditor.class);

	private static final String PROXY_SERVERS_KEY = "proxy_servers";

	public static void saveProxyServers(List<String> servers) {
		String serializedServers = String.join(",", servers);
		prefs.put(PROXY_SERVERS_KEY, serializedServers);
	}

	public static List<String> loadProxyServers() {
		String serializedServers = prefs.get(PROXY_SERVERS_KEY, "");
		if (serializedServers.isEmpty()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(serializedServers.split(",")));
	}

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
		ArrayList<String> proxyServers = new ArrayList<>(loadProxyServers());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 500 + (backendServers.size() + proxyServers.size()) * 50);
		frame.setLayout(new GridLayout(8 + backendServers.size() + proxyServers.size(), 1));

		String version = getVersionFromPom();
		JLabel versionLabel = new JLabel("VotingPluginEditor: " + version);
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(versionLabel);

		JLabel secondLine = new JLabel("Ensure you have a backup before editing files.");
		secondLine.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(secondLine);

		AddRemoveEditor backendEditor = new AddRemoveEditor(frame.getWidth()) {
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

		frame.add(backendEditor.getAddButton("Add Backend Server", "Add Backend Server"));
		frame.add(backendEditor.getRemoveButton("Remove Backend Server", "Remove Backend Server",
				PanelUtils.convertListToArray(backendServers)));

		frame.add(Box.createRigidArea(new Dimension(0, 1)));
		JPanel optionsButtons = new JPanel();
		optionsButtons.setLayout(new BoxLayout(optionsButtons, BoxLayout.Y_AXIS));

		optionsButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		backendEditor.getOptionsButtons(optionsButtons, PanelUtils.convertListToArray(backendServers));
		frame.add(optionsButtons);
		
		frame.add(Box.createRigidArea(new Dimension(0, 1)));

		AddRemoveEditor proxyEditor = new AddRemoveEditor(frame.getWidth()) {
			@Override
			public void onItemSelect(String name) {
				new ProxyEditor(name);
			}

			@Override
			public void onItemRemove(String name) {
				proxyServers.remove(name);
				saveProxyServers(proxyServers);
				frame.dispose();
				createAndShowGUI();
			}

			@Override
			public void onItemAdd(String name) {
				proxyServers.add(name);
				saveProxyServers(proxyServers);
				frame.dispose();
				createAndShowGUI();
			}
		};

		frame.add(proxyEditor.getAddButton("Add Proxy Server", "Add Proxy Server"));
		frame.add(proxyEditor.getRemoveButton("Remove Proxy Server", "Remove Proxy Server",
				PanelUtils.convertListToArray(proxyServers)));

		frame.add(Box.createRigidArea(new Dimension(0, 1)));

		JPanel optionsButtons1 = new JPanel();
		optionsButtons1.setLayout(new BoxLayout(optionsButtons1, BoxLayout.Y_AXIS));

		optionsButtons1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		proxyEditor.getOptionsButtons(optionsButtons1, PanelUtils.convertListToArray(proxyServers));
		frame.add(optionsButtons1);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
