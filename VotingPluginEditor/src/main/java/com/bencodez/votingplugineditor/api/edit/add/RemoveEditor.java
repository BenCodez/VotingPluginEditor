package com.bencodez.votingplugineditor.api.edit.add;

import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public abstract class RemoveEditor {

	private JFrame frame;

	private void open(String name, List<String> options) {

		String input = "";

		Object obj = JOptionPane.showInputDialog(frame, name, "Confirm Remove", JOptionPane.PLAIN_MESSAGE, null,
				options.toArray(), options.get(0));
		if (obj != null) {
			input = obj.toString();
		}

		if (input != null && !input.isEmpty()) {
			onRemove(input);
		}
	}

	public RemoveEditor(String name, List<String> options) {
		open(name, options);
	}
	
	public RemoveEditor(String name, String... options) {
		open(name, Arrays.asList(options));
	}

	public abstract void onRemove(String name);

}