package com.bencodez.votingplugineditor.rewards;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.api.IntSettingButton;
import com.bencodez.votingplugineditor.api.SettingButton;
import com.bencodez.votingplugineditor.api.StringListSettingButton;

public abstract class RewardEditor {
	private JFrame frame;

	private Map<String, Object> configData; // Holds the initial config values
	private Map<String, Object> initialState; // To track the original values

	private List<SettingButton> buttons;

	public RewardEditor(Map<String, Object> data) {
		configData = data;
		initialState = new HashMap<>(data);
		buttons = new ArrayList<SettingButton>();
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		frame = new JFrame("Reward Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());

		JPanel panel = createMainPanel();
		frame.add(panel, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		buttons.add(new IntSettingButton(panel, "Money", configData, "Money:", 0));
		buttons.add(new IntSettingButton(panel, "EXP", configData, "EXP:", 0));
		buttons.add(new IntSettingButton(panel, "EXPLevels", configData, "Exp Levels:", 0));
		buttons.add(new IntSettingButton(panel, "Chance", configData, "Chance to give this entire reward", 0));

		buttons.add(new StringListSettingButton(panel, "Commands", configData, "Commands (one per line, no /):"));

		buttons.add(new StringListSettingButton(panel, "Messages.Player", configData, "Messages (use %player%):"));

		// Save Button
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveChanges());
		panel.add(saveButton);

		return panel;
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : buttons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
			}
		}

		// Save changes if there are any
		if (!changes.isEmpty()) {
			try { // Specify your path
				saveChanges(changes);
				JOptionPane.showMessageDialog(frame, "Changes have been saved.");
				// Update the initial state after saving
				initialState.putAll(changes);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to save changes.");
			}
		} else {
			JOptionPane.showMessageDialog(frame, "No changes detected.");
		}
	}

	public abstract void saveChanges(Map<String, Object> changes);
}
