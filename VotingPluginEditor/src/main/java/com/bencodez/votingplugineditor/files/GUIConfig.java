package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bencodez.votingplugineditor.YmlConfigHandler;
import com.bencodez.votingplugineditor.api.BooleanSettingButton;
import com.bencodez.votingplugineditor.api.IntSettingButton;
import com.bencodez.votingplugineditor.api.SettingButton;
import com.bencodez.votingplugineditor.api.StringSettingButton;

public class GUIConfig extends YmlConfigHandler {

	private final List<SettingButton> settingButtons;

	public GUIConfig(String filePath) {
		super(filePath);
		this.settingButtons = new ArrayList<SettingButton>();
	}

	@Override
	public void openEditorGUI() {
		JFrame frame = new JFrame("GUI Config Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());

		JPanel mainPanel = createMainEditorPanel();
		frame.add(mainPanel, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save and Apply Changes");
		saveButton.addActionListener(e -> saveChanges());
		frame.add(saveButton, BorderLayout.SOUTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private Component createSectionLabel(String title) {
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		labelPanel.add(Box.createHorizontalGlue());
		labelPanel.add(new JLabel(title));
		labelPanel.add(Box.createHorizontalGlue());
		return labelPanel;
	}

	private JPanel createMainEditorPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		Map<String, Object> data = getConfigData();

		// GUIMethod Settings
		panel.add(createSectionLabel("GUIMethod"));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Today", data, "Today GUI Method", "CHEST",
				new String[] { "CHAT", "CHEST" }));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.TopVoter", data, "TopVoter GUI Method", "CHEST"));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Last", data, "Last GUI Method", "CHEST"));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Next", data, "Next GUI Method", "CHEST"));
		settingButtons.add(new StringSettingButton(panel, "GUIMethod.Total", data, "Total GUI Method", "CHEST"));

		// LastMonthGUI Setting
		// panel.add(createSectionLabel("LastMonthGUI"));
		settingButtons.add(new BooleanSettingButton(panel, "LastMonthGUI", data, "Enable LastMonthGUI:"));

		// (Add more settings as needed, organized into sections)

		panel.add(Box.createVerticalStrut(10)); // Spacer
		return panel;
	}

	private void saveChanges() {
		Map<String, Object> changes = new HashMap<>();
		for (SettingButton button : settingButtons) {
			if (button.hasChanged()) {
				changes.put(button.getKey(), button.getValue());
				button.updateValue();
			}

		}

		// Notify & save changes
		if (!changes.isEmpty()) {
			try {
				for (Entry<String, Object> change : changes.entrySet()) {
					set(change.getKey(), change.getValue());

				}
				save();
				JOptionPane.showMessageDialog(null, "Changes have been saved.");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to save changes.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "No changes detected.");
		}
	}
}