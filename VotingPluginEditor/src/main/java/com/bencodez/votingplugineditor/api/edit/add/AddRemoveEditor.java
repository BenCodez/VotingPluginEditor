package com.bencodez.votingplugineditor.api.edit.add;

import javax.swing.JButton;

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

	public abstract void onItemAdd(String name);

	public abstract void onItemRemove(String name);
}
