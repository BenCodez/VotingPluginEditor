package com.bencodez.votingplugineditor.files;

import java.util.Map;
import java.util.Map.Entry;

import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.edit.rewards.RewardEditor;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

public class RewardFilesConfig extends YmlConfigHandler {
	// private final List<SettingButton> settingButtons;
	private final String name;

	public RewardFilesConfig(String filePath, String name, String votingPluginDirectory, SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		this.name = name;
		// settingButtons = new ArrayList<SettingButton>();
		openEditorGUI();
	}

	public RewardFilesConfig(String filePath, String name, boolean open, String votingPluginDirectory,
			SFTPSettings sftp) {
		super(filePath, votingPluginDirectory, sftp);
		this.name = name;
		if (open) {
			openEditorGUI();
		}
	}

	@Override
	public void openEditorGUI() {
		new RewardEditor((Map<String, Object>) getConfigData(), name) {

			@Override
			public void saveChanges(Map<String, Object> changes) {
				try {
					for (Entry<String, Object> change : changes.entrySet()) {
						set(change.getKey(), change.getValue());
					}
					save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void removePath(String subPath) {
				remove(subPath);
				save();
			}

			@Override
			public Map<String, Object> updateData() {
				load();
				return getConfigData();
			}

			@Override
			public String getVotingPluginDirectory() {
				return getPluginDirectory();
			}

			@Override
			public SFTPSettings getSFTPSetting() {
				return getSFTPSettings();
			}
		};
	}
}