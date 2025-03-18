package com.bencodez.votingplugineditor.api.sftp;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTPConnection {

	public static Session createSession(String host, int port, String user, String password) throws JSchException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		session.setPassword(password);
		
		Properties config = new Properties();
		config.put("kex", "ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521," +
		        "diffie-hellman-group-exchange-sha256,diffie-hellman-group14-sha256," +
		        "diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1");
		config.put("server_host_key", "ssh-ed25519,ssh-rsa,ssh-dss");
		config.put("StrictHostKeyChecking", "no");

		session.setConfig(config);
		session.connect();
		return session;
	}
}
