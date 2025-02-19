package com.bencodez.votingplugineditor;

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

	public SFTPSettings(String host, int port, String user, String pass) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = pass;
	}

}
