package com.bencodez.votingplugineditor.api.edit.rewards;

import java.util.Map;

public abstract class SubRewardEditor {
	public abstract void saveChanges1(Map<String, Object> changes);

	public abstract void removePath1(String path);

	public abstract Map<String, Object> updateData1();

	public SubRewardEditor(Object data, String path) {
		new RewardEditor(data, path) {

			@Override
			public Map<String, Object> updateData() {
				return updateData1();
			}

			@Override
			public void saveChanges(Map<String, Object> changes) {
				saveChanges1(changes);
			}

			@Override
			public void removePath(String path) {
				removePath1(path);
			}

			@Override
			public String getVotingPluginDirectory() {
				return getVotingPluginDirectory1();
			}
		};
	}

	public abstract String getVotingPluginDirectory1();

}
