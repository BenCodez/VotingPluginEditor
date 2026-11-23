package com.bencodez.votingplugineditor.server;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.SessionFactory;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.RenameFlags;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ServerEditorUtilsTest {
	@Rule public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void backupAndRestoreUseRemoteBytesEvenWhenLocalPathsExist() throws IOException {
		Path local = temporary.newFolder("config_backup").toPath();
		Path collision = Files.write(local.resolve("settings.yml"), bytes("local"));
		String backup = local.toString();
		String live = backup.substring(0, backup.length() - "_backup".length());
		RemoteTree remote = new RemoteTree();
		remote.dirs.add(live);
		remote.dirs.add(backup);
		remote.dirs.add(live + "/empty");
		remote.dirs.add(live + "/nested");
		remote.files.put(live + "/nested/extra.yml", bytes("nested"));
		remote.files.put(live + "/settings.yml", bytes("server"));
		remote.files.put(backup + "/settings.yml", bytes("stale"));

		ServerEditorUtils.backupRemoteDirectory(remote, live, backup);
		assertArrayEquals(bytes("server"), remote.files.get(backup + "/settings.yml"));
		assertArrayEquals(bytes("nested"), remote.files.get(backup + "/nested/extra.yml"));
		assertTrue(remote.dirs.contains(backup + "/empty"));
		remote.files.put(live + "/settings.yml", bytes("edited"));
		ServerEditorUtils.restoreRemoteDirectory(remote, backup, live);
		assertArrayEquals(bytes("server"), remote.files.get(live + "/settings.yml"));
		assertArrayEquals(bytes("server"), remote.files.get(backup + "/settings.yml"));
		assertArrayEquals(bytes("local"), Files.readAllBytes(collision));
		remote.assertClean();
	}

	@Test
	public void missingAndEmptyBackupsFail() throws IOException {
		RemoteTree remote = new RemoteTree();
		assertThrows(IOException.class, () -> ServerEditorUtils.restoreRemoteDirectory(remote, "/backup", "/live"));
		assertFalse(remote.dirs.contains("/live"));
		remote.dirs.add("/backup");
		remote.dirs.add("/backup/nested");
		IOException error = assertThrows(IOException.class,
				() -> ServerEditorUtils.restoreRemoteDirectory(remote, "/backup", "/live"));
		assertTrue(error.getMessage().contains("no files"));
		assertTrue(remote.files.isEmpty());
		remote.assertClean();
	}

	@Test
	public void partialCopyKeepsCompletedFilesAndPreservesCurrentTarget() throws IOException {
		RemoteTree remote = new RemoteTree();
		remote.dirs.add("/backup");
		remote.dirs.add("/live");
		remote.files.put("/backup/first", bytes("complete"));
		remote.files.put("/backup/second", new byte[16384]);
		remote.files.put("/live/second", bytes("original"));
		remote.failRead = "/backup/second";
		assertThrows(IOException.class, () -> ServerEditorUtils.restoreRemoteDirectory(remote, "/backup", "/live"));
		assertArrayEquals(bytes("complete"), remote.files.get("/live/first"));
		assertArrayEquals(bytes("original"), remote.files.get("/live/second"));
		assertTrue(remote.removedTemporary);
		remote.assertClean();
	}

	@Test
	public void rejectedReplacementPreservesExistingTargetAndCleansUp() throws IOException {
		RemoteTree remote = new RemoteTree();
		remote.dirs.add("/backup");
		remote.dirs.add("/live");
		remote.files.put("/backup/file", bytes("backup"));
		remote.files.put("/live/file", bytes("original"));
		remote.allowReplacement = false;
		assertThrows(IOException.class, () -> ServerEditorUtils.restoreRemoteDirectory(remote, "/backup", "/live"));
		assertArrayEquals(bytes("original"), remote.files.get("/live/file"));
		assertTrue(remote.removedTemporary);
		remote.assertClean();
	}

	@Test
	public void failedOutputClosePreservesOriginalAndClosesBothHandles() throws IOException {
		RemoteTree remote = new RemoteTree();
		remote.dirs.add("/backup");
		remote.dirs.add("/live");
		remote.files.put("/backup/file", bytes("backup"));
		remote.files.put("/live/file", bytes("original"));
		remote.failOutputClose = true;
		assertThrows(IOException.class, () -> ServerEditorUtils.restoreRemoteDirectory(remote, "/backup", "/live"));
		assertArrayEquals(bytes("original"), remote.files.get("/live/file"));
		assertTrue(remote.removedTemporary);
		remote.assertClean();
	}

	@Test
	public void overlappingTreesFailBeforeCreatingDirectories() throws IOException {
		RemoteTree remote = new RemoteTree();
		remote.dirs.add("/");
		remote.dirs.add("/config");
		assertThrows(IOException.class, () -> ServerEditorUtils.backupRemoteDirectory(remote, "/", "/_backup"));
		assertThrows(IOException.class,
				() -> ServerEditorUtils.backupRemoteDirectory(remote, "/config", "/config/nested"));
		assertFalse(remote.dirs.contains("/_backup"));
		assertFalse(remote.dirs.contains("/config/nested"));
	}

	private static byte[] bytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	private static SFTPEngine engine() throws IOException {
		Session.Subsystem subsystem = (Session.Subsystem) Proxy.newProxyInstance(
				Session.Subsystem.class.getClassLoader(), new Class<?>[] { Session.Subsystem.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "getInputStream": return new ByteArrayInputStream(new byte[0]);
					case "getOutputStream": return new ByteArrayOutputStream();
					case "getRemoteMaxPacketSize": return 32768;
					default: throw new AssertionError(method);
					}
				});
		Session session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "getLoggerFactory": return LoggerFactory.DEFAULT;
					case "startSubsystem": return subsystem;
					default: throw new AssertionError(method);
					}
				});
		return new SFTPEngine(new SessionFactory() {
			@Override public Session startSession() { return session; }
			@Override public java.net.InetSocketAddress getRemoteSocketAddress() { return null; }
		});
	}

	private static final class RemoteTree extends SFTPClient {
		final Set<String> dirs = new LinkedHashSet<>();
		final Map<String, byte[]> files = new LinkedHashMap<>();
		int openHandles;
		String failRead;
		boolean removedTemporary;
		boolean allowReplacement = true;
		boolean failOutputClose;

		RemoteTree() throws IOException {
			super(engine());
		}

		@Override public List<RemoteResourceInfo> ls(String path) throws IOException {
			if (!dirs.contains(path)) {
				throw new SFTPException(Response.StatusCode.NO_SUCH_FILE, path);
			}
			Set<String> paths = new LinkedHashSet<>(dirs);
			paths.addAll(files.keySet());
			List<RemoteResourceInfo> children = new ArrayList<>();
			for (String child : paths) {
				if (child.startsWith(path + "/") && child.lastIndexOf('/') == path.length()) {
					children.add(new RemoteResourceInfo(getSFTPEngine().getPathHelper().getComponents(child), stat(child)));
				}
			}
			return children;
		}

		@Override public String canonicalize(String path) {
			return path;
		}

		@Override public void mkdir(String path) throws IOException {
			if (!dirs.add(path)) {
				throw new IOException("Already exists");
			}
		}

		@Override public FileAttributes stat(String path) throws IOException {
			if (dirs.contains(path)) {
				return new FileAttributes.Builder().withType(FileMode.Type.DIRECTORY).build();
			}
			if (files.containsKey(path)) {
				return new FileAttributes.Builder().withType(FileMode.Type.REGULAR)
						.withSize(files.get(path).length).withPermissions(0640).build();
			}
			throw new SFTPException(Response.StatusCode.NO_SUCH_FILE, path);
		}

		@Override public FileAttributes lstat(String path) throws IOException {
			return stat(path);
		}

		@Override public RemoteFile open(String path, Set<OpenMode> modes) throws IOException {
			assertEquals(java.util.EnumSet.of(OpenMode.READ), modes);
			assertTrue(files.containsKey(path));
			return handle(path);
		}

		@Override public RemoteFile open(String path, Set<OpenMode> modes, FileAttributes attrs) {
			assertEquals(java.util.EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.EXCL), modes);
			assertEquals(0600, attrs.getMode().getPermissionsMask());
			assertNull(files.putIfAbsent(path, new byte[0]));
			return handle(path);
		}

		private RemoteFile handle(String path) {
			openHandles++;
			return new RemoteFile(getSFTPEngine(), path, new byte[0]) {
				@Override public int read(long offset, byte[] target, int start, int len) throws IOException {
					if (path.equals(failRead) && offset > 0) {
						throw new IOException("Simulated remote read failure");
					}
					byte[] content = files.get(path);
					if (offset >= content.length) {
						return -1;
					}
					int count = Math.min(len, content.length - (int) offset);
					System.arraycopy(content, (int) offset, target, start, count);
					return count;
				}

				@Override public void write(long offset, byte[] data, int start, int len) {
					byte[] content = Arrays.copyOf(files.get(path), (int) offset + len);
					System.arraycopy(data, start, content, (int) offset, len);
					files.put(path, content);
				}

				@Override public void close() throws IOException {
					openHandles--;
					if (failOutputClose && path.contains("/.vpe-")) {
						throw new IOException("Simulated remote close failure");
					}
				}
			};
		}

		@Override public void chmod(String path, int mode) {
			assertTrue(files.containsKey(path));
			assertEquals(0640, mode);
		}

		@Override public void rename(String from, String to) {
			assertFalse(files.containsKey(to));
			publish(from, to);
		}

		@Override public void rename(String from, String to, Set<RenameFlags> flags) throws IOException {
			assertEquals(java.util.EnumSet.of(RenameFlags.ATOMIC, RenameFlags.OVERWRITE), flags);
			if (!allowReplacement) {
				throw new IOException("Atomic replacement is unavailable");
			}
			publish(from, to);
		}

		private void publish(String from, String to) {
			assertEquals(0, openHandles);
			assertTrue(files.containsKey(from));
			files.put(to, files.remove(from));
		}

		@Override public void rm(String path) {
			assertTrue(path.contains("/.vpe-"));
			assertNotNull(files.remove(path));
			removedTemporary = true;
		}

		void assertClean() {
			assertEquals(0, openHandles);
			assertFalse(files.keySet().stream().anyMatch(path -> path.contains("/.vpe-")));
		}
	}
}
