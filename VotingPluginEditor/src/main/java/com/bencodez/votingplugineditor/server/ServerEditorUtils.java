package com.bencodez.votingplugineditor.server;

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
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.bencodez.votingplugineditor.api.sftp.SFTPConnection;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ServerEditorUtils {
	private static final String SFTP_SETTINGS_FILE = "sftp_settings.properties";

	// ----- Encryption & Key Methods --------

	// Converts a SecretKey into a Base64 string.
	public static String secretKeyToString(SecretKey secretKey) {
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}

	// Recreates a SecretKey from its Base64 string form.
	public static SecretKey stringToSecretKey(String keyString) {
		byte[] decodedKey = Base64.getDecoder().decode(keyString);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}

	// Generates a new 256-bit AES SecretKey.
	public static SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256); // 256-bit AES
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Encrypts the given password and returns the Base64–encoded cipher text.
	public static String encryptPassword(String password, SecretKey secretKey)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	// Decrypts the given Base64–encoded cipher text and returns the original
	// password.
	public static String decryptPassword(String encryptedPassword, SecretKey secretKey)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
		return new String(decryptedBytes, "UTF-8");
	}

	// ----- SFTP File Operations --------

	// Lists all remote .yml files in the provided remote folder.
	public static ArrayList<String> listRemoteYmlFiles(String remoteDir, SFTPSettings settings)
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

	// Downloads a remote file (by its full remotePath) into the given local File.
	public static void downloadRemoteFile(String remotePath, File localFile, SFTPSettings settings)
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

	// Uploads a local file (localFile) to the specified remotePath.
	public static void uploadRemoteFile(File localFile, String remotePath, SFTPSettings settings)
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

	// Recursively backs up the remote directory from sourceDir to backupDir.
	public static void backupRemoteDirectory(ChannelSftp sftpChannel, String sourceDir, String backupDir)
			throws SftpException {
		try {
			sftpChannel.mkdir(backupDir);
		} catch (SftpException e) {
			// If directory exists, ignore error.
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

	// Recursively restores files from backupDir to destinationDir on the remote
	// server.
	public static void restoreRemoteDirectory(ChannelSftp sftpChannel, String backupDir, String destinationDir)
			throws SftpException {
		try {
			sftpChannel.mkdir(destinationDir);
		} catch (SftpException e) {
			// Ignore if directory exists.
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

	// ----- SFTP Settings persistence methods --------

	/**
	 * Saves SFTP settings to disk. The property keys are built from a
	 * propertyPrefix (for example "proxy" or empty for backend) and the server
	 * name.
	 *
	 * @param propertyPrefix a prefix (can be empty) to use for keys.
	 * @param server         the server identifier.
	 * @param host           the SFTP host.
	 * @param port           the SFTP port.
	 * @param user           the SFTP user.
	 * @param plainPassword  the plain–text password.
	 * @param secretKey      the SecretKey used to encrypt the password.
	 */
	public static void saveSFTPSettings(String propertyPrefix, String server, String host, int port, String user,
			String plainPassword, SecretKey secretKey) {
		Properties properties = new Properties();
		// Use the properties file; if it already exists, load it first.
		try (FileInputStream in = new FileInputStream(SFTP_SETTINGS_FILE)) {
			properties.load(in);
		} catch (IOException e) {
			// File might not exist yet.
		}
		// Build key prefix (if propertyPrefix is not empty, add a dot)
		String keyPrefix = (propertyPrefix == null || propertyPrefix.isEmpty()) ? server
				: propertyPrefix + "." + server;

		properties.setProperty(keyPrefix + ".host", host);
		properties.setProperty(keyPrefix + ".port", String.valueOf(port));
		properties.setProperty(keyPrefix + ".user", user);
		try {
			properties.setProperty(keyPrefix + ".password", encryptPassword(plainPassword, secretKey));
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// We also store whether SFTP mode is enabled
		properties.setProperty(keyPrefix + ".enabled", "true");
		properties.setProperty(keyPrefix + ".secretKey", secretKeyToString(secretKey));
		try (FileOutputStream out = new FileOutputStream(SFTP_SETTINGS_FILE)) {
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads SFTP settings from disk for the given server using the propertyPrefix.
	 *
	 * @param propertyPrefix a prefix (for example "proxy" or empty for backend).
	 * @param server         the server identifier.
	 * @return an SFTPSettings object with the stored settings.
	 */
	public static SFTPSettings loadSFTPSettings(String propertyPrefix, String server) {
		Properties properties = new Properties();
		File file = new File(SFTP_SETTINGS_FILE);
		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file)) {
				properties.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String keyPrefix = (propertyPrefix == null || propertyPrefix.isEmpty()) ? server
				: propertyPrefix + "." + server;

		String host = properties.getProperty(keyPrefix + ".host", "");
		int port = Integer.parseInt(properties.getProperty(keyPrefix + ".port", "22"));
		String user = properties.getProperty(keyPrefix + ".user", "");
		String encryptedPassword = properties.getProperty(keyPrefix + ".password", "");
		String secretKeyString = properties.getProperty(keyPrefix + ".secretKey", "");
		String password = "";
		if (!secretKeyString.isEmpty()) {
			SecretKey secretKey = stringToSecretKey(secretKeyString);
			try {
				password = decryptPassword(encryptedPassword, secretKey);
			} catch (GeneralSecurityException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return new SFTPSettings(host, port, user, password);
	}

	// ----- Local Backup and Restore Helpers --------

	// Backups local files from source directory to destination (using _backup
	// suffix)
	public static void backupLocalDirectory(String directoryPath) throws IOException {
		Path sourceDir = Paths.get(directoryPath);
		Path backupDir = Paths.get(directoryPath + "_backup");
		Files.walk(sourceDir).forEach(source -> {
			Path destination = backupDir.resolve(sourceDir.relativize(source));
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// Restores local files from backup to the original directory.
	public static void restoreLocalDirectory(String directoryPath) throws IOException {
		Path sourceDir = Paths.get(directoryPath + "_backup");
		Path destinationDir = Paths.get(directoryPath);
		Files.walk(sourceDir).forEach(source -> {
			Path destination = destinationDir.resolve(sourceDir.relativize(source));
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}