package com.bencodez.votingplugineditor.files;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationSyncTest {
	@TempDir
	Path directory;

	@Test
	void voteCommandEditsKeepTheLabelAStringAndLeaveDefaultsAbsent() throws Exception {
		Path file = directory.resolve("Config.yml");
		String legacy = "UseVoteGUIMainCommand: true\nFutureOption:\n  Mode: 'off'\n  Entries: []\n";
		Files.writeString(file, legacy);
		ConfigConfig config = new ConfigConfig(file.toString(), directory.toString(), null);

		JPanel panel = config.createMainEditorPanel();
		JCheckBox enabled = components(panel, JCheckBox.class)
				.filter(box -> box.getText().equals("Enable Player Vote Command")).findFirst().orElseThrow();
		JTextField label = components(panel, JTextField.class)
				.filter(field -> field.getText().equals("vote")).findFirst().orElseThrow();

		assertTrue(enabled.isSelected());
		assertFalse(config.applyChanges());
		assertEquals(legacy, Files.readString(file));
		enabled.setSelected(false);
		label.setText("/ballot");
		assertThrows(IllegalArgumentException.class, config::applyChanges);
		assertEquals(legacy, Files.readString(file));
		assertNull(config.get("VoteCommandEnabled"));

		label.setText("123");
		assertTrue(config.applyChanges());
		config.load();
		assertEquals("123", config.get("VoteCommandName"));
		assertEquals(false, config.get("VoteCommandEnabled"));
		assertEquals(true, config.get("UseVoteGUIMainCommand"));
		assertEquals(Map.of("Mode", "off", "Entries", List.of()), config.get("FutureOption"));
		assertNull(config.get("LoadCommandAliases"));
		assertNull(config.get("PointsOnVote"));
	}

	@Test
	void votePartyReadsLegacyRemindersAndWritesCurrentTypedFieldsOnEdit() throws Exception {
		Path file = directory.resolve("SpecialRewards.yml");
		String legacy = String.join("\n",
				"VoteParty:",
				"  VoteReminderAtVotes: [5, 2]",
				"  VoteReminderCommands: ['say legacy']",
				"  VoteReminder:",
				"    AtVotes: []",
				"    Commands: []",
				"    Votes: ['12']",
				"    FutureOption: {}",
				"");
		Files.writeString(file, legacy);
		SpecialRewardsConfig config = new SpecialRewardsConfig(file.toString(), directory.toString(), null);
		JPanel panel = config.createVotePartyPanel();
		JTextArea thresholds = components(panel, JTextArea.class)
				.filter(area -> area.getText().equals("5\n2")).findFirst().orElseThrow();
		JTextArea commands = components(panel, JTextArea.class)
				.filter(area -> area.getText().equals("say legacy")).findFirst().orElseThrow();
		assertFalse(config.applyChanges());
		assertEquals(legacy, Files.readString(file));

		commands.setText("say updated\nsay again");
		thresholds.setText("a");
		assertThrows(NumberFormatException.class, config::applyChanges);
		assertEquals(legacy, Files.readString(file));
		assertEquals(List.of(), config.get("VoteParty.VoteReminder.Commands"));

		thresholds.setText("4\n 1 ");
		assertTrue(config.applyChanges());
		config.load();
		assertEquals(List.of(4, 1), config.get("VoteParty.VoteReminder.AtVotes"));
		assertEquals(List.of("say updated", "say again"), config.get("VoteParty.VoteReminder.Commands"));
		assertEquals(List.of(5, 2), config.get("VoteParty.VoteReminderAtVotes"));
		assertEquals(List.of("say legacy"), config.get("VoteParty.VoteReminderCommands"));
		assertEquals(List.of("12"), config.get("VoteParty.VoteReminder.Votes"));
		assertEquals(Map.of(), config.get("VoteParty.VoteReminder.FutureOption"));

		thresholds.setText("");
		commands.setText("");
		assertTrue(config.applyChanges());
		config.load();
		assertEquals(List.of(), config.get("VoteParty.VoteReminder.AtVotes"));
		assertEquals(List.of(), config.get("VoteParty.VoteReminder.Commands"));
		assertNull(config.get("VoteParty.VoteReminderAtVotes"));
		assertNull(config.get("VoteParty.VoteReminderCommands"));
	}

	private static <T extends Component> Stream<T> components(Container container, Class<T> type) {
		return Stream.of(container.getComponents()).flatMap(component -> {
			if (type.isInstance(component)) {
				return Stream.of(type.cast(component));
			}
			return component instanceof Container ? components((Container) component, type) : Stream.empty();
		});
	}
}
