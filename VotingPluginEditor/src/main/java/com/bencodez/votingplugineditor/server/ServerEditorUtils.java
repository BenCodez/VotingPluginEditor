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
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.RenameFlags;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

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
	public static ArrayList<String> listRemoteYmlFiles(String remoteDir, SFTPSettings settings) throws IOException {
		ArrayList<String> files = new ArrayList<>();
		try (SSHClient sshClient = new SSHClient()) {
			sshClient.addHostKeyVerifier(new PromiscuousVerifier());
			sshClient.connect(settings.getHost(), settings.getPort());
			sshClient.authPassword(settings.getUser(), settings.getPassword());

			try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
				List<RemoteResourceInfo> list = sftpClient.ls(remoteDir);
				for (RemoteResourceInfo entry : list) {
					if (entry.getName().endsWith(".yml")) {
						files.add(entry.getName());
					}
				}
			}
		}
		return files;
	}

	// Downloads a remote file (by its full remotePath) into the given local File.
	public static void downloadRemoteFile(String remotePath, File localFile, SFTPSettings settings) throws IOException {
		try (SSHClient sshClient = new SSHClient()) {
			sshClient.addHostKeyVerifier(new PromiscuousVerifier());
			sshClient.connect(settings.getHost(), settings.getPort());
			sshClient.authPassword(settings.getUser(), settings.getPassword());

			try (SFTPClient sftpClient = sshClient.newSFTPClient();
					FileOutputStream fos = new FileOutputStream(localFile)) {
				sftpClient.get(remotePath, localFile.getAbsolutePath());
			}
		}
	}

	// Uploads a local file (localFile) to the specified remotePath.
	public static void uploadRemoteFile(File localFile, String remotePath, SFTPSettings settings) throws IOException {
		try (SSHClient sshClient = new SSHClient()) {
			sshClient.addHostKeyVerifier(new PromiscuousVerifier());
			sshClient.connect(settings.getHost(), settings.getPort());
			sshClient.authPassword(settings.getUser(), settings.getPassword());

			try (SFTPClient sftpClient = sshClient.newSFTPClient();
					FileInputStream fis = new FileInputStream(localFile)) {
			}
		}
	}

	public static void backupRemoteDirectory(SFTPClient sftpClient, String sourceDir, String backupDir)
			throws IOException {
		requireSeparateRemoteTrees(sftpClient, sourceDir, backupDir);
		copyRemoteDirectory(sftpClient, sourceDir, backupDir);
	}

	public static void restoreRemoteDirectory(SFTPClient sftpClient, String backupDir, String destinationDir)
			throws IOException {
		requireSeparateRemoteTrees(sftpClient, backupDir, destinationDir);
		if (!copyRemoteDirectory(sftpClient, backupDir, destinationDir)) {
			throw new IOException("The remote backup contains no files to restore.");
		}
	}

	private static void requireSeparateRemoteTrees(SFTPClient client, String source, String destination)
			throws IOException {
		String remoteSource = client.canonicalize(source);
		String remoteDestination;
		try {
			remoteDestination = client.canonicalize(destination);
		} catch (SFTPException error) {
			if (error.getStatusCode() != Response.StatusCode.NO_SUCH_FILE) {
				throw error;
			}
			int separator = destination.lastIndexOf('/');
			String parent = separator < 0 ? "." : destination.substring(0, Math.max(separator, 1));
			String remoteParent = client.canonicalize(parent);
			remoteDestination = ("/".equals(remoteParent) ? "/" : remoteParent + "/")
					+ destination.substring(separator + 1);
		}
		if (remoteSource.equals(remoteDestination) || "/".equals(remoteSource) || "/".equals(remoteDestination)
				|| remoteSource.startsWith(remoteDestination + "/") || remoteDestination.startsWith(remoteSource + "/")) {
			throw new IOException("The remote directory and its backup must be separate. Select a remote subdirectory.");
		}
	}

	private static boolean copyRemoteDirectory(SFTPClient client, String sourceDir, String destinationDir)
			throws IOException {
		List<RemoteResourceInfo> entries = client.ls(sourceDir);
		try {
			client.mkdir(destinationDir);
		} catch (IOException error) {
			if (client.lstat(destinationDir).getType() != FileMode.Type.DIRECTORY) {
				throw error;
			}
		}

		boolean copiedFile = false;
		for (RemoteResourceInfo entry : entries) {
			String source = sourceDir + "/" + entry.getName();
			String destination = destinationDir + "/" + entry.getName();
			if (entry.isDirectory()) {
				copiedFile |= copyRemoteDirectory(client, source, destination);
			} else {
				copyRemoteFile(client, source, destination);
				copiedFile = true;
			}
		}
		return copiedFile;
	}

	private static void copyRemoteFile(SFTPClient client, String source, String destination) throws IOException {
		FileAttributes sourceAttributes = client.stat(source);
		if (sourceAttributes.getType() != FileMode.Type.REGULAR) {
			throw new IOException("Remote source is not a regular file: " + source);
		}
		FileAttributes target;
		try {
			target = client.lstat(destination);
			if (target.getType() != FileMode.Type.REGULAR) {
				throw new IOException("Remote destination is not a regular file: " + destination);
			}
		} catch (SFTPException error) {
			if (error.getStatusCode() != Response.StatusCode.NO_SUCH_FILE) {
				throw error;
			}
			target = null;
		}

		String temporary = destination.substring(0, destination.lastIndexOf('/') + 1) + ".vpe-" + UUID.randomUUID();
		boolean created = false;
		try {
			try (RemoteFile input = client.open(source, EnumSet.of(OpenMode.READ))) {
				FileAttributes privateMode = new FileAttributes.Builder().withPermissions(0600).build();
				try (RemoteFile output = client.open(temporary, EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.EXCL),
						privateMode)) {
					created = true;
					int packetSize = client.getSFTPEngine().getSubsystem().getRemoteMaxPacketSize()
							- output.getOutgoingPacketOverhead();
					if (packetSize <= 0) {
						throw new IOException("SFTP packet size is too small to copy files.");
					}
					byte[] buffer = new byte[Math.min(8192, packetSize)];
					long offset = 0;
					int length;
					while ((length = input.read(offset, buffer, 0, buffer.length)) > 0) {
						output.write(offset, buffer, 0, length);
						offset += length;
					}
					if (length == 0 || (sourceAttributes.has(FileAttributes.Flag.SIZE) && offset != sourceAttributes.getSize())) {
						throw new IOException("Remote source size changed during copy: " + source);
					}
				}
			}
			FileAttributes modeSource = target == null ? sourceAttributes : target;
			client.chmod(temporary, modeSource.getMode().getPermissionsMask());
			if (target != null) {
				client.rename(temporary, destination, EnumSet.of(RenameFlags.ATOMIC, RenameFlags.OVERWRITE));
			} else {
				client.rename(temporary, destination);
			}
		} catch (IOException error) {
			if (created) {
				try {
					client.rm(temporary);
				} catch (IOException cleanupError) {
					error.addSuppressed(cleanupError);
				}
			}
			throw error;
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
	public static void saveSFTPSettings(boolean enabled, String propertyPrefix, String server, String host, int port,
			String user, String plainPassword, SecretKey secretKey) {
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
		properties.setProperty(keyPrefix + ".enabled", "" + enabled);
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
		boolean enabled = Boolean.parseBoolean(properties.getProperty(keyPrefix + ".enabled", "false"));
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
		return new SFTPSettings(enabled, host, port, user, password);
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