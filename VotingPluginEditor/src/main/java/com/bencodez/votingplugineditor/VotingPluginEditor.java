package com.bencodez.votingplugineditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.files.BungeeSettingsConfig;
import com.bencodez.votingplugineditor.files.ConfigConfig;
import com.bencodez.votingplugineditor.files.GUIConfig;
import com.bencodez.votingplugineditor.files.RewardFilesConfig;
import com.bencodez.votingplugineditor.files.ShopConfig;
import com.bencodez.votingplugineditor.files.SpecialRewardsConfig;
import com.bencodez.votingplugineditor.files.VoteSitesConfig;
import com.bencodez.votingplugineditor.generator.MaterialLoader;
import com.bencodez.votingplugineditor.generator.PotionLoader;

import lombok.Getter;

public class VotingPluginEditor {
	private static JComboBox<String> fileDropdown;
	public static String directoryPath;
	private static final Map<String, Class<? extends YmlConfigHandler>> HANDLER_CLASSES = new HashMap<>();
	private static final Preferences prefs = Preferences.userNodeForPackage(VotingPluginEditor.class);
	private static final String PREF_DIRECTORY = "votingPluginDirectory";
	@Getter
	private static List<String> materials;
	@Getter
	private static List<String> potionEffects;

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
		potionEffects = PotionLoader.loadPotions();
		SwingUtilities.invokeLater(VotingPluginEditor::createAndShowGUI);
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("VotingPlugin File Selector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		frame.setLayout(new GridLayout(8, 1)); // Updated to 7 rows

		JButton editVoteSitesButton = new JButton("Edit VoteSites (Opens VoteSites.yml)");
		editVoteSitesButton.addActionListener(e -> openVoteSitesEditor());
		frame.add(editVoteSitesButton);

		JButton editRewardFilesButton = new JButton("Edit Reward Files");
		editRewardFilesButton.addActionListener(e -> openRewardFileEditor());
		frame.add(editRewardFilesButton);

		JLabel label = new JLabel("Select a file to edit:", JLabel.CENTER);
		frame.add(label);

		fileDropdown = new JComboBox<>(HANDLER_CLASSES.keySet().toArray(new String[0]));
		frame.add(fileDropdown);

		JButton openEditorButton = new JButton("Open Editor");
		openEditorButton.addActionListener(e -> openEditor());
		frame.add(openEditorButton);

		JButton backupButton = new JButton("Backup Files");
		backupButton.addActionListener(e -> backupFiles());
		frame.add(backupButton);

		JButton restoreButton = new JButton("Restore Files");
		restoreButton.addActionListener(e -> restoreFiles());
		frame.add(restoreButton);

		JButton chooseDirButton = new JButton("Choose VotingPlugin Directory");
		chooseDirButton.addActionListener(e -> chooseDirectory(frame));
		frame.add(chooseDirButton);

		directoryPath = prefs.get(PREF_DIRECTORY, null);
		if (directoryPath != null) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(frame, "Using saved directory: " + directoryPath);
			});
		}

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static void openRewardFileEditor() {
		JFrame rewardFrame = new JFrame("Edit Reward Files");
		rewardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		File rewardsFolder = new File(directoryPath, "Rewards");

		ArrayList<String> rewardFiles = new ArrayList<String>();
		if (rewardsFolder.exists() && rewardsFolder.isDirectory()) {

			String[] excludedFiles = { "ExampleBasic", "ExampleAdvanced" };

			for (File file : rewardsFolder.listFiles()) {
				if (file.isFile() && !Arrays.asList(excludedFiles).contains(file.getName())) {
					rewardFiles.add(file.getName());
				}
			}
		} else {
			System.out.println("Rewards folder does not exist.");
		}

		rewardFrame.setSize(600, 300 + rewardFiles.size() * 30);
		rewardFrame.setLayout(new BorderLayout());

		JPanel rewardPanel = new JPanel();
		rewardPanel.setLayout(new BoxLayout(rewardPanel, BoxLayout.Y_AXIS));
		rewardPanel.setBorder(BorderFactory.createTitledBorder("Reward Files"));

		AddRemoveEditor rewardEditor = new AddRemoveEditor(rewardFrame.getWidth()) {

			@Override
			public void onItemSelect(String name) {
				new RewardFilesConfig(
						new File(directoryPath + File.separator + "Rewards" + File.separator + name).getAbsolutePath(),
						name);
			}

			@Override
			public void onItemAdd(String name) {
				try {
					File newFile = new File(directoryPath + File.separator + "Rewards" + File.separator + name);
					if (newFile.createNewFile()) {
						rewardFiles.add(name);
						JOptionPane.showMessageDialog(rewardFrame, "Reward file added: " + name);
					} else {
						JOptionPane.showMessageDialog(rewardFrame, "Failed to add reward file: " + name);
					}
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(rewardFrame, "Error adding reward file: " + name);
				}
				rewardFrame.dispose();
				openRewardFileEditor();
			}

			@Override
			public void onItemRemove(String name) {
				int confirmation = JOptionPane.showConfirmDialog(rewardFrame,
						"Are you sure you want to delete the reward file: " + name + "?", "Confirm Delete",
						JOptionPane.YES_NO_OPTION);

				if (confirmation == JOptionPane.YES_OPTION) {
					File fileToRemove = new File(directoryPath + File.separator + "Rewards" + File.separator + name);
					if (fileToRemove.delete()) {
						rewardFiles.remove(name);
						JOptionPane.showMessageDialog(rewardFrame, "Reward file removed: " + name);
					} else {
						JOptionPane.showMessageDialog(rewardFrame, "Failed to remove reward file: " + name);
					}
				}
				rewardFrame.dispose();
				openRewardFileEditor();

			}

		};

		rewardPanel.add(rewardEditor.getAddButton("Add Reward File", "Add Reward File"));
		rewardPanel.add(rewardEditor.getRemoveButton("Remove Reward File", "Remove Reward File",
				PanelUtils.convertListToArray(rewardFiles)));

		rewardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		rewardEditor.getOptionsButtons(rewardPanel, PanelUtils.convertListToArray(rewardFiles));

		rewardFrame.add(rewardPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(e -> {
			// Implement save logic here
			JOptionPane.showMessageDialog(rewardFrame, "Changes saved.");
		});

		rewardFrame.add(saveButton, BorderLayout.SOUTH);

		rewardFrame.setLocationRelativeTo(null);
		rewardFrame.setVisible(true);
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
