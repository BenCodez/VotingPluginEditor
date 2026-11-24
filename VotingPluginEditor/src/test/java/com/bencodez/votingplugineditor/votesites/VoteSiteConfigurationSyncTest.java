package com.bencodez.votingplugineditor.votesites;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.bencodez.votingplugineditor.files.VoteSitesConfig;

class VoteSiteConfigurationSyncTest {
	@TempDir
	Path directory;

	@Test
	void rejectedVoteRewardRoundTripsAlongsideLegacyDelayAndUnknownValues() throws Exception {
		Path file = directory.resolve("VoteSites.yml");
		Files.writeString(file, String.join("\n",
				"VoteSites:",
				"  Example:",
				"    VoteDelay: 12.5",
				"    VoteDelayMin: 15",
				"    Rewards:",
				"      Messages:",
				"        Player: Thanks",
				"    FutureOption:",
				"      Entries:",
				"        - {Name: 'yes', Value: true}",
				"    WaitUntilVoteDelayRewards:",
				"      FutureRewardOption: []",
				"  Other:",
				"    Enabled: false",
				"EverySiteReward: {}",
				""));
		VoteSitesConfig config = new VoteSitesConfig(file.toString(), directory.toString(), null);
		String site = "VoteSites.Example";
		String reward = site + ".WaitUntilVoteDelayRewards";
		String message = "Try again after %VoteDelay%";
		VoteSiteEditor.saveRewardChanges(config, reward, Map.of("Messages.Player", List.of(message)));
		config.load();

		assertEquals(List.of(message), config.get(reward + ".Messages.Player"));
		assertEquals(List.of(), config.get(reward + ".FutureRewardOption"));
		assertEquals(12.5, config.get(site + ".VoteDelay"));
		assertEquals(15, config.get(site + ".VoteDelayMin"));
		assertEquals("Thanks", config.get(site + ".Rewards.Messages.Player"));
		assertEquals(List.of(Map.of("Name", "yes", "Value", true)), config.get(site + ".FutureOption.Entries"));
		assertNull(config.get(site + ".VoteDelayDailyHour"));
		assertNull(config.get("VoteSites.Other.WaitUntilVoteDelayRewards"));
		assertEquals(Map.of(), config.get("EverySiteReward"));
	}
}
