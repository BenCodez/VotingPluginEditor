package com.bencodez.votingplugineditor.files;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bencodez.votingplugineditor.YmlConfigHandler;

public class ConfigConfig extends YmlConfigHandler {
    public ConfigConfig(String filePath) {
        super(filePath);
    }
    
    @Override
    public void openEditorGUI() {
        JFrame editorFrame = new JFrame("Editing VoteSites - " + new File(filePath).getName());
        editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editorFrame.setSize(400, 300);
        editorFrame.setLayout(new BorderLayout());

        editorFrame.setLocationRelativeTo(null);
        editorFrame.setVisible(true);
    }
}