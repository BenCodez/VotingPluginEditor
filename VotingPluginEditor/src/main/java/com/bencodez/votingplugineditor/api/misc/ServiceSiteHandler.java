package com.bencodez.votingplugineditor.api.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lombok.Getter;

public class ServiceSiteHandler {
	private static final String PRESET_SOURCE =
			"https://raw.githubusercontent.com/wiki/BenCodez/VotingPlugin/Minecraft-Server-Lists.md";

	@Getter
	private LinkedHashMap<String, String> serviceSites = new LinkedHashMap<>();

	public ServiceSiteHandler() {
		loadFromGithub();
	}

	public boolean contains(String service) {
		for (Entry<String, String> entry : serviceSites.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(service)) {
				return true;
			}
		}
		return false;
	}

	public void loadFromGithub() {
		try {
			readFromWeb(PRESET_SOURCE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String match(String service) {
		for (Entry<String, String> entry : serviceSites.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(service)) {
				return entry.getValue();
			}
		}
		return service;
	}

	public String matchReverse(String service) {
		for (Entry<String, String> entry : serviceSites.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(service)) {
				return entry.getKey();
			}
		}
		return service;
	}

	public void readFromWeb(String webURL) throws IOException {
		serviceSites.clear();
		URL url = new URL(webURL);
		try (InputStream is = url.openStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
					trimmed = trimmed.substring(2).trim();
				}

				String[] split = trimmed.split("\\s+-\\s+", 2);
				if (split.length == 2 && !split[0].isBlank() && !split[1].isBlank()) {
					serviceSites.put(stripMarkdown(split[0]), stripMarkdown(split[1]));
				}
			}
		}
	}

	private String stripMarkdown(String value) {
		return value.trim().replace("`", "").replace("**", "");
	}
}
