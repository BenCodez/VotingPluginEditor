package com.bencodez.votingplugineditor.server;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.bencodez.votingplugineditor.PanelUtils;
import com.bencodez.votingplugineditor.SFTPConnection;
import com.bencodez.votingplugineditor.SFTPSettings;
import com.bencodez.votingplugineditor.VotingPluginEditor;
import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.add.AddRemoveEditor;
import com.bencodez.votingplugineditor.files.BungeeSettingsConfig;
import com.bencodez.votingplugineditor.files.ConfigConfig;
import com.bencodez.votingplugineditor.files.GUIConfig;
import com.bencodez.votingplugineditor.files.RewardFilesConfig;
import com.bencodez.votingplugineditor.files.ShopConfig;
import com.bencodez.votingplugineditor.files.SpecialRewardsConfig;
import com.bencodez.votingplugineditor.files.VoteSitesConfig;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class BackEndServerEditor {

	private static JComboBox<String> fileDropdown;
	public static String directoryPath;
	private static final Map<String, Class<? extends YmlConfigHandler>> HANDLER_CLASSES = new HashMap<>();
	private static final String PREF_DIRECTORY = "votingPluginDirectory";

	static {
		HANDLER_CLASSES.put("VoteSites.yml", VoteSitesConfig.class);
		HANDLER_CLASSES.put("Config.yml", ConfigConfig.class);
		HANDLER_CLASSES.put("SpecialRewards.yml", SpecialRewardsConfig.class);
		HANDLER_CLASSES.put("GUI.yml", GUIConfig.class);
		HANDLER_CLASSES.put("Shop.yml", ShopConfig.class);
		HANDLER_CLASSES.put("BungeeSettings.yml", BungeeSettingsConfig.class);
	}

	private static String server = "";

	public BackEndServerEditor(String name) {
		BackEndServerEditor.server = name;
		createAndShowGUI(name);
	}

	public static SFTPSettings getSFTPSettingsFromFields() {
		return new SFTPSettings(sftpHostField.getText(), Integer.parseInt(sftpPortField.getText()),
				sftpUserField.getText(), new String(sftpPasswordField.getPassword()));
	}

	private static JComboBox<String> storageTypeDropdown;
	private static JTextField sftpHostField;
	private static JTextField sftpPortField;
	private static JTextField sftpUserField;
	private static JPasswordField sftpPasswordField;

	private static JLabel sftpHostLabel;
	private static JLabel sftpPortLabel;
	private static JLabel sftpUserLabel;
	private static JLabel sftpPasswordLabel;

	// Method to convert SecretKey to a string
	private static String secretKeyToString(SecretKey secretKey) {
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}

	// Method to convert a string back to SecretKey
	private static SecretKey stringToSecretKey(String keyString) {
		byte[] decodedKey = Base64.getDecoder().decode(keyString);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}

	private static SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256); // for example, 256-bit AES
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String encryptPassword(String password, SecretKey secretKey)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	private static String decryptPassword(String encryptedPassword, SecretKey secretKey)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
		return new String(decryptedBytes, "UTF-8");
	}

	private void openSFTPSettingsDialog(JFrame parentFrame) {
		JDialog sftpDialog = new JDialog(parentFrame, "SFTP Settings", true);
		sftpDialog.setSize(300, 200);
		sftpDialog.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;

		sftpDialog.add(sftpHostLabel, gbc);
		gbc.gridx++;
		sftpDialog.add(sftpHostField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		sftpDialog.add(sftpPortLabel, gbc);
		gbc.gridx++;
		sftpDialog.add(sftpPortField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		sftpDialog.add(sftpUserLabel, gbc);
		gbc.gridx++;
		sftpDialog.add(sftpUserField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		sftpDialog.add(sftpPasswordLabel, gbc);
		gbc.gridx++;
		sftpDialog.add(sftpPasswordField, gbc);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			saveSFTPSettings(server, sftpHostField.getText(), Integer.parseInt(sftpPortField.getText()),
					sftpUserField.getText(), generateSecretKey());
			sftpDialog.dispose();
		});
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		sftpDialog.add(saveButton, gbc);

		sftpDialog.setLocationRelativeTo(parentFrame);
		sftpDialog.setVisible(true);
	}

	private void createAndShowGUI(String server) {
		JFrame frame = new JFrame("VotingPluginEditor Back-End Server: " + server);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(400, 600);
		frame.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.NORTH; // Anchor components to the top

		JLabel secondLine = new JLabel("Ensure you have a backup before editing files.");
		secondLine.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(secondLine, gbc);

		gbc.gridy++;
		JButton editVoteSitesButton = new JButton("Edit VoteSites (Opens VoteSites.yml)");
		editVoteSitesButton.addActionListener(e -> openVoteSitesEditor());
		frame.add(editVoteSitesButton, gbc);

		gbc.gridy++;
		JButton editRewardFilesButton = new JButton("Edit Reward Files");
		editRewardFilesButton.addActionListener(e -> openRewardFileEditor());
		frame.add(editRewardFilesButton, gbc);

		gbc.gridy++;
		JButton openConfigButton = new JButton("Open Config.yml");
		openConfigButton.addActionListener(e -> openSpecificFileEditor("Config.yml"));
		frame.add(openConfigButton, gbc);

		gbc.gridy++;
		JButton openSpecialRewardsButton = new JButton("Open SpecialRewards.yml (Special Rewards)");
		openSpecialRewardsButton.addActionListener(e -> openSpecificFileEditor("SpecialRewards.yml"));
		frame.add(openSpecialRewardsButton, gbc);

		gbc.gridy++;
		JButton openGUIButton = new JButton("Open GUI.yml (GUI's)");
		openGUIButton.addActionListener(e -> openSpecificFileEditor("GUI.yml"));
		frame.add(openGUIButton, gbc);

		gbc.gridy++;
		JButton openShopButton = new JButton("Edit VoteShop");
		openShopButton.addActionListener(e -> openSpecificFileEditor("Shop.yml"));
		frame.add(openShopButton, gbc);

		gbc.gridy++;
		JButton openBungeeSettingsButton = new JButton("Open BungeeSettings.yml");
		openBungeeSettingsButton.addActionListener(e -> openSpecificFileEditor("BungeeSettings.yml"));
		frame.add(openBungeeSettingsButton, gbc);

		gbc.gridy++;
		JButton openEditorButton = new JButton("Open Editor (Select file)");
		openEditorButton.addActionListener(e -> openEditor());
		frame.add(openEditorButton, gbc);

		gbc.gridy++;
		JButton backupButton = new JButton("Backup Files");
		backupButton.addActionListener(e -> backupFiles(frame));
		frame.add(backupButton, gbc);

		gbc.gridy++;
		JButton restoreButton = new JButton("Restore Files");
		restoreButton.addActionListener(e -> restoreFiles(frame));
		frame.add(restoreButton, gbc);

		gbc.gridy++;
		JButton chooseDirButton = new JButton("Choose VotingPlugin Directory");
		chooseDirButton.addActionListener(e -> chooseDirectory(frame));
		frame.add(chooseDirButton, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		frame.add(new JLabel("Storage Type:"), gbc);
		gbc.gridx++;
		storageTypeDropdown = new JComboBox<>(new String[] { "Local Path", "SFTP" });
		storageTypeDropdown.addActionListener(e -> toggleSFTPSettings());
		frame.add(storageTypeDropdown, gbc);

		sftpHostLabel = new JLabel("SFTP Host:");
		sftpHostField = new JTextField();
		sftpPortLabel = new JLabel("SFTP Port:");
		sftpPortField = new JTextField();
		sftpPortField.setText("22");
		sftpUserLabel = new JLabel("SFTP User:");
		sftpUserField = new JTextField();
		sftpPasswordLabel = new JLabel("SFTP Password:");
		sftpPasswordField = new JPasswordField();

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JButton sftpSettingsButton = new JButton("SFTP Settings");
		sftpSettingsButton.addActionListener(e -> openSFTPSettingsDialog(frame));
		frame.add(sftpSettingsButton, gbc);

		toggleSFTPSettings();

		SFTPSettings sftpSettings = loadSFTPSettings(server);
		sftpHostField.setText(sftpSettings.getHost());
		sftpPortField.setText(String.valueOf(sftpSettings.getPort()));
		sftpUserField.setText(sftpSettings.getUser());
		if (!sftpSettings.getPassword().isEmpty()) {
			sftpPasswordField.setText(sftpSettings.getPassword());
		}

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		directoryPath = VotingPluginEditor.getPrefs().get(server + PREF_DIRECTORY, null);
		if (directoryPath != null) {
			String storageType = "Local Path";
			if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
				storageType = "SFTP";
			}
			String message = "Using saved directory: " + directoryPath + " (" + storageType + ")";
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(frame, message);
			});
		}
	}

	private static void openRewardFileEditor() {
		JFrame rewardFrame = new JFrame("Edit Reward Files");
		rewardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Determine if we are in SFTP mode.
		boolean isSFTP = "SFTP".equals(storageTypeDropdown.getSelectedItem());
		SFTPSettings settings = getSFTPSettingsFromFields();
		final ArrayList<String> rewardFiles = new ArrayList<>();

		if (isSFTP) {
			// In SFTP mode, list remote files in the "Rewards" folder.
			String remoteRewardsDir = directoryPath + "/Rewards";
			try {
				rewardFiles.addAll(listRemoteYmlFiles(remoteRewardsDir, settings));
			} catch (Exception e) {
				System.out.println("Remote Rewards folder does not exist or cannot be accessed.");
				e.printStackTrace();
			}
		} else {
			// Local mode: list files from local Rewards folder.
			File rewardsFolder = new File(directoryPath, "Rewards");
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
		}

		rewardFrame.setSize(600, 300 + rewardFiles.size() * 30);
		rewardFrame.setLayout(new BorderLayout());

		JPanel rewardPanel = new JPanel();
		rewardPanel.setLayout(new BoxLayout(rewardPanel, BoxLayout.Y_AXIS));
		rewardPanel.setBorder(BorderFactory.createTitledBorder("Reward Files"));

		AddRemoveEditor rewardEditor = new AddRemoveEditor(rewardFrame.getWidth()) {

			@Override
			public void onItemSelect(String name) {
				if (isSFTP) {
					// Download the remote file to a temporary file then open the editor.
					String remoteFilePath = directoryPath + "/Rewards/" + name;
					try {
						File tempFile = File.createTempFile(name, ".tmp");
						tempFile.deleteOnExit();
						downloadRemoteFile(remoteFilePath, tempFile, settings);
						// Pass the temp file's path to your editor.
						new RewardFilesConfig(tempFile.getAbsolutePath(), name, directoryPath, settings);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(rewardFrame, "Failed to open remote reward file: " + name);
					}
				} else {
					new RewardFilesConfig(new File(directoryPath + File.separator + "Rewards" + File.separator + name)
							.getAbsolutePath(), name, directoryPath, settings);
				}
			}

			@Override
			public void onItemAdd(String name) {
				if (isSFTP) {
					// In SFTP mode, create an empty remote file.
					String remoteRewardsDir = directoryPath + "/Rewards";
					String remoteFile = remoteRewardsDir + "/" + name;
					try {
						// Ensure the remote directory exists.
						Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(),
								settings.getUser(), settings.getPassword());
						ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
						sftpChannel.connect();
						try {
							sftpChannel.mkdir(remoteRewardsDir);
						} catch (SftpException ex) {
							// Directory may already exist; ignore error.
						}
						// Create an empty temporary file and upload it.
						File tempFile = File.createTempFile("empty", null);
						tempFile.deleteOnExit();
						sftpChannel.put(new FileInputStream(tempFile), remoteFile);
						sftpChannel.disconnect();
						session.disconnect();
						rewardFiles.add(name);
						JOptionPane.showMessageDialog(rewardFrame, "Reward file added: " + name);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(rewardFrame, "Failed to add remote reward file: " + name);
					}
				} else {
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
					if (isSFTP) {
						String remoteFile = directoryPath + "/Rewards/" + name;
						try {
							Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(),
									settings.getUser(), settings.getPassword());
							ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
							sftpChannel.connect();
							sftpChannel.rm(remoteFile);
							sftpChannel.disconnect();
							session.disconnect();
							rewardFiles.remove(name);
							JOptionPane.showMessageDialog(rewardFrame, "Reward file removed: " + name);
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(rewardFrame, "Failed to remove remote reward file: " + name);
						}
					} else {
						File fileToRemove = new File(
								directoryPath + File.separator + "Rewards" + File.separator + name);
						if (fileToRemove.delete()) {
							rewardFiles.remove(name);
							JOptionPane.showMessageDialog(rewardFrame, "Reward file removed: " + name);
						} else {
							JOptionPane.showMessageDialog(rewardFrame, "Failed to remove reward file: " + name);
						}
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
			// Implement save logic here – in SFTP mode, after editing, the file should be
			// uploaded back.
			JOptionPane.showMessageDialog(rewardFrame, "Changes saved.");
		});

		rewardFrame.add(saveButton, BorderLayout.SOUTH);

		rewardFrame.setLocationRelativeTo(null);
		rewardFrame.setVisible(true);
	}

	private void toggleSFTPSettings() {
		boolean isSFTP = "SFTP".equals(storageTypeDropdown.getSelectedItem());
		sftpHostLabel.setVisible(isSFTP);
		sftpHostField.setVisible(isSFTP);
		sftpPortLabel.setVisible(isSFTP);
		sftpPortField.setVisible(isSFTP);
		sftpUserLabel.setVisible(isSFTP);
		sftpUserField.setVisible(isSFTP);
		sftpPasswordLabel.setVisible(isSFTP);
		sftpPasswordField.setVisible(isSFTP);
	}

	private static void openSpecificFileEditor(String fileName) {
		if (directoryPath != null) {
			SFTPSettings settings = getSFTPSettingsFromFields();
			if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
				String remoteFilePath = directoryPath + "/" + fileName;
				try {
					File tempFile = File.createTempFile(fileName, ".tmp");
					tempFile.deleteOnExit();
					downloadRemoteFile(remoteFilePath, tempFile, settings);
					YmlConfigHandler handler = HANDLER_CLASSES.get(fileName)
							.getDeclaredConstructor(String.class, String.class, SFTPSettings.class)
							.newInstance(tempFile.getAbsolutePath(), directoryPath, settings);
					handler.openEditorGUI();
					// After editing, you could call uploadRemoteFile(tempFile, remoteFilePath,
					// settings)
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to open remote " + fileName);
				}
			} else {
				String filePath = directoryPath + File.separator + fileName;
				try {
					YmlConfigHandler handler = HANDLER_CLASSES.get(fileName)
							.getDeclaredConstructor(String.class, String.class, SFTPSettings.class)
							.newInstance(filePath, directoryPath, settings);
					handler.openEditorGUI();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to open " + fileName);
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}

	private static void openEditor() {
		SFTPSettings settings = getSFTPSettingsFromFields();
		if (directoryPath != null) {
			String[] files;
			if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
				try {
					ArrayList<String> remoteFiles = listRemoteYmlFiles(directoryPath, settings);
					files = remoteFiles.toArray(new String[0]);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to list remote files.");
					return;
				}
			} else {
				files = HANDLER_CLASSES.keySet().toArray(new String[0]);
			}
			if (files != null && files.length > 0) {
				String selectedFile = (String) JOptionPane.showInputDialog(null, "Select a file to edit:",
						"File Selection", JOptionPane.PLAIN_MESSAGE, null, files, files[0]);
				if (selectedFile != null && selectedFile.length() > 0) {
					openSpecificFileEditor(selectedFile);
				} else {
					JOptionPane.showMessageDialog(null, "No file selected.");
				}
			} else {
				JOptionPane.showMessageDialog(null, "No .yml files found in the directory.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}

	// Helper method to list remote .yml files
	private static ArrayList<String> listRemoteYmlFiles(String remoteDir, SFTPSettings settings)
			throws JSchException, SftpException {
		ArrayList<String> files = new ArrayList<>();
		Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(), settings.getUser(),
				settings.getPassword());
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();
		@SuppressWarnings("unchecked")
		java.util.Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(remoteDir);
		for (ChannelSftp.LsEntry entry : list) {
			String name = entry.getFilename();
			if (name.endsWith(".yml")) {
				files.add(name);
			}
		}
		sftpChannel.disconnect();
		session.disconnect();
		return files;
	}

	private static void chooseDirectory(JFrame frame) {
		if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
			String host = sftpHostField.getText();
			int port = Integer.parseInt(sftpPortField.getText());
			String user = sftpUserField.getText();
			String password = new String(sftpPasswordField.getPassword());

			saveSFTPSettings(server, host, port, user, generateSecretKey());

			try {
				Session session = SFTPConnection.createSession(host, port, user, password);
				Channel channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) channel;

				// Start navigation at the current working directory on the SFTP server
				String remotePath = sftpChannel.pwd();
				boolean finished = false;
				while (!finished) {
					// List entries in the current remotePath and filter directories
					@SuppressWarnings("unchecked")
					java.util.Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(remotePath);
					ArrayList<String> options = new ArrayList<>();

					// Option to select the current directory
					options.add("<<Select current directory>>");
					// Option to go up, if we're not at root
					if (!remotePath.equals("/")) {
						options.add("<<Go up>>");
					}
					// Add subdirectories
					for (ChannelSftp.LsEntry entry : list) {
						String filename = entry.getFilename();
						if (entry.getAttrs().isDir() && !filename.equals(".") && !filename.equals("..")) {
							options.add(filename);
						}
					}

					// Show a dialog with the current directory and options
					String selected = (String) JOptionPane.showInputDialog(frame,
							"Current Remote Directory:\n" + remotePath + "\n\nSelect an option:",
							"Remote Directory Navigation", JOptionPane.QUESTION_MESSAGE, null,
							options.toArray(new String[0]), options.get(0));

					if (selected == null) {
						// User cancelled - exit the loop without saving
						return;
					} else if (selected.equals("<<Select current directory>>")) {
						finished = true;
					} else if (selected.equals("<<Go up>>")) {
						// Move to the parent directory.
						// Use simple logic: if remotePath is "/" then remain there.
						File temp = new File(remotePath);
						String parent = temp.getParent();
						remotePath = (parent == null || parent.isEmpty()) ? "/" : parent;
					} else {
						// User selected a subdirectory; append it to remotePath.
						if (remotePath.equals("/")) {
							remotePath = remotePath + selected;
						} else {
							remotePath = remotePath + "/" + selected;
						}
					}
				}

				// Save the selected remote path
				directoryPath = remotePath;
				VotingPluginEditor.getPrefs().put(server + PREF_DIRECTORY, directoryPath);
				JOptionPane.showMessageDialog(frame, "Remote directory saved: " + directoryPath);

				sftpChannel.disconnect();
				session.disconnect();
			} catch (JSchException | SftpException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"Failed to connect to SFTP server or list directories:\n" + e.getMessage());
			}
		} else {
			// Local directory selection remains unchanged
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select VotingPlugin Folder");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);

			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedDirectory = fileChooser.getSelectedFile();
				directoryPath = selectedDirectory.getAbsolutePath();
				VotingPluginEditor.getPrefs().put(server + PREF_DIRECTORY, directoryPath);
				JOptionPane.showMessageDialog(frame, "Directory saved: " + directoryPath);
			}
		}
	}

	// Helper method to download a remote file to a local file
	private static void downloadRemoteFile(String remotePath, File localFile, SFTPSettings settings)
			throws JSchException, SftpException, IOException {
		Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(), settings.getUser(),
				settings.getPassword());
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();
		try (FileOutputStream fos = new FileOutputStream(localFile)) {
			sftpChannel.get(remotePath, fos);
		}
		sftpChannel.disconnect();
		session.disconnect();
	}

	// Helper method to upload a local file to a remote path
	private static void uploadRemoteFile(File localFile, String remotePath, SFTPSettings settings)
			throws JSchException, SftpException, IOException {
		Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(), settings.getUser(),
				settings.getPassword());
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();
		try (FileInputStream fis = new FileInputStream(localFile)) {
			sftpChannel.put(fis, remotePath);
		}
		sftpChannel.disconnect();
		session.disconnect();
	}

	// Remote recursive backup: copy all files from sourceDir to backupDir on the
	// SFTP server.
	private static void backupRemoteDirectory(ChannelSftp sftpChannel, String sourceDir, String backupDir)
			throws SftpException {
		try {
			sftpChannel.mkdir(backupDir);
		} catch (SftpException e) {
			// If directory exists, ignore error (error code may vary)
		}
		@SuppressWarnings("unchecked")
		java.util.Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(sourceDir);
		for (ChannelSftp.LsEntry entry : list) {
			String filename = entry.getFilename();
			if (filename.equals(".") || filename.equals(".."))
				continue;
			String sourcePath = sourceDir + "/" + filename;
			String backupPath = backupDir + "/" + filename;
			if (entry.getAttrs().isDir()) {
				backupRemoteDirectory(sftpChannel, sourcePath, backupPath);
			} else {
				try {
					File tempFile = File.createTempFile("sftp_backup", null);
					tempFile.deleteOnExit();
					sftpChannel.get(sourcePath, new FileOutputStream(tempFile));
					sftpChannel.put(new FileInputStream(tempFile), backupPath);
					tempFile.delete();
				} catch (IOException ex) {
					throw new SftpException(0, "Backup failed for file: " + sourcePath, ex);
				}
			}
		}
	}

	// Remote recursive restore: copy all files from backupDir to destinationDir on
	// the SFTP server.
	private static void restoreRemoteDirectory(ChannelSftp sftpChannel, String backupDir, String destinationDir)
			throws SftpException {
		try {
			sftpChannel.mkdir(destinationDir);
		} catch (SftpException e) {
			// Ignore if already exists
		}
		@SuppressWarnings("unchecked")
		java.util.Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(backupDir);
		for (ChannelSftp.LsEntry entry : list) {
			String filename = entry.getFilename();
			if (filename.equals(".") || filename.equals(".."))
				continue;
			String backupPath = backupDir + "/" + filename;
			String destPath = destinationDir + "/" + filename;
			if (entry.getAttrs().isDir()) {
				restoreRemoteDirectory(sftpChannel, backupPath, destPath);
			} else {
				try {
					File tempFile = File.createTempFile("sftp_restore", null);
					tempFile.deleteOnExit();
					sftpChannel.get(backupPath, new FileOutputStream(tempFile));
					sftpChannel.put(new FileInputStream(tempFile), destPath);
					tempFile.delete();
				} catch (IOException ex) {
					throw new SftpException(0, "Restore failed for file: " + backupPath, ex);
				}
			}
		}
	}

	// Modified backupFiles to work with both local and SFTP storage
	private static void backupFiles(JFrame frame) {
		if (directoryPath != null) {
			if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
				SFTPSettings settings = getSFTPSettingsFromFields();
				try {
					Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(),
							settings.getUser(), settings.getPassword());
					ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
					sftpChannel.connect();
					String backupDir = directoryPath + "_backup";
					backupRemoteDirectory(sftpChannel, directoryPath, backupDir);
					JOptionPane.showMessageDialog(frame, "Remote backup completed successfully.");
					sftpChannel.disconnect();
					session.disconnect();
				} catch (JSchException | SftpException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Remote backup failed:\n" + e.getMessage());
				}
			} else {
				// Local backup remains unchanged
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
					JOptionPane.showMessageDialog(frame, "Local backup completed successfully.");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Local backup failed.");
				}
			}
		} else {
			JOptionPane.showMessageDialog(frame, "Please select a directory.");
		}
	}

	// Modified restoreFiles to work with both local and SFTP storage
	private static void restoreFiles(JFrame frame) {
		if (directoryPath != null) {
			if ("SFTP".equals(storageTypeDropdown.getSelectedItem())) {
				SFTPSettings settings = getSFTPSettingsFromFields();
				try {
					Session session = SFTPConnection.createSession(settings.getHost(), settings.getPort(),
							settings.getUser(), settings.getPassword());
					ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
					sftpChannel.connect();
					String backupDir = directoryPath + "_backup";
					restoreRemoteDirectory(sftpChannel, backupDir, directoryPath);
					JOptionPane.showMessageDialog(frame, "Remote restore completed successfully.");
					sftpChannel.disconnect();
					session.disconnect();
				} catch (JSchException | SftpException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Remote restore failed:\n" + e.getMessage());
				}
			} else {
				// Local restore remains unchanged
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
					JOptionPane.showMessageDialog(frame, "Local restore completed successfully.");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Local restore failed.");
				}
			}
		} else {
			JOptionPane.showMessageDialog(frame, "Please select a directory.");
		}
	}

	private static final String SFTP_SETTINGS_FILE = "sftp_settings.properties";

	private static void saveSFTPSettings(String server, String host, int port, String user, SecretKey secretKey) {
		Properties properties = new Properties();
		try (FileInputStream in = new FileInputStream(SFTP_SETTINGS_FILE)) {
			properties.load(in);
		} catch (IOException e) {
			// File might not exist yet, which is fine
		}
		properties.setProperty(server + ".host", host);
		properties.setProperty(server + ".port", String.valueOf(port));
		properties.setProperty(server + ".user", user);
		try {
			properties.setProperty(server + ".password",
					encryptPassword(new String(sftpPasswordField.getPassword()), secretKey));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		properties.setProperty(server + ".enabled", "" + "SFTP".equals(storageTypeDropdown.getSelectedItem()));
		properties.setProperty(server + ".secretKey", secretKeyToString(secretKey));

		try (FileOutputStream out = new FileOutputStream(SFTP_SETTINGS_FILE)) {
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static SFTPSettings loadSFTPSettings(String server) {
		Properties properties = new Properties();
		File file = new File(SFTP_SETTINGS_FILE);
		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file)) {
				properties.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String host = properties.getProperty(server + ".host", "");
		int port = Integer.parseInt(properties.getProperty(server + ".port", "22"));
		String user = properties.getProperty(server + ".user", "");
		String encryptedPassword = properties.getProperty(server + ".password", "");
		String secretKeyString = properties.getProperty(server + ".secretKey", "");
		String password = "";

		if (!secretKeyString.isEmpty()) {

			SecretKey secretKey = stringToSecretKey(secretKeyString);
			try {

				password = decryptPassword(encryptedPassword, secretKey);
			} catch (GeneralSecurityException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		boolean enabled = Boolean.parseBoolean(properties.getProperty(server + ".enabled", "false"));
		if (enabled) {
			storageTypeDropdown.setSelectedItem("SFTP");
		}
		return new SFTPSettings(host, port, user, password);
	}

	private static void openVoteSitesEditor() {
		if (directoryPath != null) {
			String fileName = "VoteSites.yml";
			openSpecificFileEditor(fileName);
		} else {
			JOptionPane.showMessageDialog(null, "Please select a directory.");
		}
	}
}
