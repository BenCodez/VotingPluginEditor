package com.bencodez.votingplugineditor.api.edit.add;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.PanelUtils;

public abstract class AddRemoveEditor {
	public AddRemoveEditor() {
	}

	public JButton getAddButton(String label, String title) {
		JButton addButton = new JButton(label);
		addButton.addActionListener(event -> {
			new AddEditor(title) {

				@Override
				public void onAdd(String name) {
					onItemAdd(name);
				}
			};
		});
		return addButton;
	}

	public JButton getRemoveButton(String label, String title, String[] options) {
		JButton removeButton = new JButton(label);
		removeButton.addActionListener(event -> {
			new RemoveEditor(title, options) {

				@Override
				public void onRemove(String name) {
					onItemRemove(name);
				}
			};
		});
		return removeButton;
	}

	public JButton getRemoveButton(String label, String title, Set<String> options) {
		JButton removeButton = new JButton(label);
		removeButton.addActionListener(event -> {
			new RemoveEditor(title, PanelUtils.convertSetToArray(options)) {

				@Override
				public void onRemove(String name) {
					onItemRemove(name);
				}
			};
		});
		return removeButton;
	}

	public ArrayList<JButton> getOptionsButtons(JPanel panel, String[] options) {
		ArrayList<JButton> buttons = new ArrayList<JButton>();

		for (String option : options) {
			JButton button = new JButton(option);

			button.setAlignmentX(Component.LEFT_ALIGNMENT);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
			button.setSize(300, 30);
			button.setVerticalTextPosition(SwingConstants.CENTER);

			button.addActionListener(event -> {
				onItemSelect(option);
			});

			if (panel != null) {
				panel.add(button);

				// Add some spacing between buttons (optional)
				panel.add(Box.createRigidArea(new Dimension(0, 5)));
			}
			buttons.add(button);

		}
		return buttons;
	}

	public abstract void onItemSelect(String name);

	public abstract void onItemAdd(String name);

	public abstract void onItemRemove(String name);
}
