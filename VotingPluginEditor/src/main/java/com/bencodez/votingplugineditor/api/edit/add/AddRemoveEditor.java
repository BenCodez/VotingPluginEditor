package com.bencodez.votingplugineditor.api.edit.add;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

public abstract class AddRemoveEditor {
	private int width;

	public AddRemoveEditor(int width) {
		this.width = width;
	}

	public JButton getAddButton(String label, String title) {
		JButton addButton = new JButton(label);
		addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
		addButton.setSize(width, 30);
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

	public JButton getAddButton(String label, String title, List<String> options) {
		JButton addButton = new JButton(label);
		addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
		addButton.setSize(width, 30);
		addButton.addActionListener(event -> {
			new AddEditor(title, options) {

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
		removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeButton.getPreferredSize().height));
		removeButton.setSize(width, 30);
		removeButton.addActionListener(event -> {
			if (options.length == 0) {
				JOptionPane.showMessageDialog(null, "Nothing to remove");
			} else {
				new RemoveEditor(title, options) {

					@Override
					public void onRemove(String name) {
						onItemRemove(name);
					}
				};
			}
		});
		return removeButton;
	}

	public JButton getRemoveButton(String label, String title, Set<String> options) {
		JButton removeButton = new JButton(label);
		removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeButton.getPreferredSize().height));
		removeButton.setSize(width, 30);
		removeButton.addActionListener(event -> {
			if (options.size() == 0) {
				JOptionPane.showMessageDialog(null, "Nothing to delete");
			} else {
				new RemoveEditor(title, PanelUtils.convertSetToArray(options)) {

					@Override
					public void onRemove(String name) {
						onItemRemove(name);
					}
				};
			}
		});
		return removeButton;
	}

	public ArrayList<JButton> getOptionsButtons(JPanel panel, String[] options) {
		return getOptionsButtons(panel, options, true);
	}

	public ArrayList<JButton> getOptionsButtons(JPanel panel, String[] options, boolean space) {
		ArrayList<JButton> buttons = new ArrayList<JButton>();

		for (String option : options) {
			JButton button = new JButton(option);

			button.setAlignmentX(Component.LEFT_ALIGNMENT);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
			button.setSize(width, 30);
			button.setVerticalTextPosition(SwingConstants.CENTER);

			button.addActionListener(event -> {
				onItemSelect(option);
			});

			if (panel != null) {
				panel.add(button);

				if (space)
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
