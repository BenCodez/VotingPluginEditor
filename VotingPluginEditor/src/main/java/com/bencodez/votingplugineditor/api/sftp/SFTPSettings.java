package com.bencodez.votingplugineditor.api.sftp;

import lombok.Getter;
import lombok.Setter;

public class SFTPSettings {
	@Getter
	@Setter
	private String host;
	@Getter
	@Setter
	private int port;
	@Getter
	@Setter
	private String user;
	@Getter
	@Setter
	private String password;

	@Getter
	@Setter
	private boolean enabled;

	public SFTPSettings(boolean enabled, String host, int port, String user, String pass) {
		this.enabled = enabled;
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = pass;
	}

}
