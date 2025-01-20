package com.bencodez.votingplugineditor;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.bencodez.votingplugineditor.files.BungeeSettingsConfig;
import com.bencodez.votingplugineditor.files.ConfigConfig;
import com.bencodez.votingplugineditor.files.GUIConfig;
import com.bencodez.votingplugineditor.files.ShopConfig;
import com.bencodez.votingplugineditor.files.SpecialRewardsConfig;
import com.bencodez.votingplugineditor.files.VoteSitesConfig;
import com.bencodez.votingplugineditor.generator.MaterialLoader;

import lombok.Getter;

public class VotingPluginEditor {
	private static JComboBox<String> fileDropdown;
	private static String directoryPath;
	private static final Map<String, Class<? extends YmlConfigHandler>> HANDLER_CLASSES = new HashMap<>();
	private static final Preferences prefs = Preferences.userNodeForPackage(VotingPluginEditor.class);
	private static final String PREF_DIRECTORY = "votingPluginDirectory";
	@Getter
	private static List<String> materials;

	static {
		HANDLER_CLASSES.put("VoteSites.yml", VoteSitesConfig.class);
		HANDLER_CLASSES.put("Config.yml", ConfigConfig.class);
		HANDLER_CLASSES.put("SpecialRewards.yml", SpecialRewardsConfig.class);
		HANDLER_CLASSES.put("GUI.yml", GUIConfig.class);
		HANDLER_CLASSES.put("Shop.yml", ShopConfig.class);
		HANDLER_CLASSES.put("BungeeSettings.yml", BungeeSettingsConfig.class);
	}

	public static void main(String[] args) {
		materials = MaterialLoader.loadMaterials();
		SwingUtilities.invokeLater(VotingPluginEditor::createAndShowGUI);
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("VotingPlugin File Selector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		frame.setLayout(new GridLayout(7, 1)); // Updated to 7 rows

		JButton editVoteSitesButton = new JButton("Edit VoteSites (Opens VoteSites.yml)");
		editVoteSitesButton.addActionListener(e -> openVoteSitesEditor());
		frame.add(editVoteSitesButton);

		JLabel label = new JLabel("Select a file to edit:", JLabel.CENTER);
		frame.add(label);

		fileDropdown = new JComboBox<>(HANDLER_CLASSES.keySet().toArray(new String[0]));
		frame.add(fileDropdown);

		JButton chooseDirButton = new JButton("Choose VotingPlugin Directory");
		chooseDirButton.addActionListener(e -> chooseDirectory(frame));
		frame.add(chooseDirButton);

		JButton openEditorButton = new JButton("Open Editor");
		openEditorButton.addActionListener(e -> openEditor());
		frame.add(openEditorButton);

		JButton backupButton = new JButton("Backup Files");
		backupButton.addActionListener(e -> backupFiles());
		frame.add(backupButton);

		JButton restoreButton = new JButton("Restore Files");
		restoreButton.addActionListener(e -> restoreFiles());
		frame.add(restoreButton);

		directoryPath = prefs.get(PREF_DIRECTORY, null);
		if (directoryPath != null) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(frame, "Using saved directory: " + directoryPath);
			});
		}

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static void openVoteSitesEditor() {
		if (directoryPath != null) {
			String filePath = directoryPath + File.separator + "VoteSites.yml";
			try {
				YmlConfigHandler handler = HANDLER_CLASSES.get("VoteSites.yml").getDeclaredConstructor(String.class)
						.newInstance(filePath);
				handler.openEditorGUI();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to open VoteSites.yml.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}

	private static void backupFiles() {
		if (directoryPath != null) {
			Path sourceDir = Paths.get(directoryPath);
			Path backupDir = Paths.get(directoryPath + "_backup");

			try {
				Files.walk(sourceDir).forEach(source -> {
					Path destination = backupDir.resolve(sourceDir.relativize(source));
					try {
						Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				JOptionPane.showMessageDialog(null, "Backup completed successfully.");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Backup failed.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}

	private static void restoreFiles() {
		if (directoryPath != null) {
			Path sourceDir = Paths.get(directoryPath + "_backup");
			Path destinationDir = Paths.get(directoryPath);

			try {
				Files.walk(sourceDir).forEach(source -> {
					Path destination = destinationDir.resolve(sourceDir.relativize(source));
					try {
						Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				JOptionPane.showMessageDialog(null, "Restore completed successfully.");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Restore failed.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}

	private static void chooseDirectory(JFrame frame) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select VotingPlugin Folder");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedDirectory = fileChooser.getSelectedFile();
			directoryPath = selectedDirectory.getAbsolutePath();
			prefs.put(PREF_DIRECTORY, directoryPath);
			JOptionPane.showMessageDialog(frame, "Directory saved: " + directoryPath);
		}
	}

	private static void openEditor() {
		String selectedFile = (String) fileDropdown.getSelectedItem();
		if (selectedFile != null && directoryPath != null) {
			try {
				String filePath = directoryPath + File.separator + selectedFile;
				YmlConfigHandler handler = HANDLER_CLASSES.get(selectedFile).getDeclaredConstructor(String.class)
						.newInstance(filePath);
				handler.openEditorGUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a file and a directory.");
		}
	}
}
