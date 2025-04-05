package com.bencodez.votingplugineditor.api.edit.add;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.bencodez.votingplugineditor.api.misc.PanelUtils;

public abstract class AddEditor {

	private JFrame frame;

	public AddEditor(String name) {
		open(name, null);
	}

	private void open(String name, List<String> options) {

		String input = "";
		if (options != null && options.size() > 1) {

			Object obj = JOptionPane.showInputDialog(frame, name, "Confirm", JOptionPane.PLAIN_MESSAGE, null,
					PanelUtils.convertListToArray(options), options.get(0));
			if (obj != null) {
				input = obj.toString();
			}
		} else {
			Object obj = JOptionPane.showInputDialog(frame, name, "Confirm", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if (obj != null) {
				input = obj.toString();
			}
		}
		if (input != null && !input.isEmpty()) {
			onAdd(input);
		}
	}

	public AddEditor(String name, List<String> options) {
		open(name, options);
	}

	public abstract void onAdd(String name);

}
