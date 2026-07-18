package com.bencodez.votingplugineditor.api.misc;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

/**
 * Checks VotingPlugin's current JavaScript engine setting before an editor saves
 * JavaScript-backed configuration.
 */
public class JavascriptRequirementHandler extends YmlConfigHandler {

    private static final String ENABLED_PATH = "JavascriptEngine.Enabled";

    public JavascriptRequirementHandler(String configFilePath, String votingPluginDirectory, SFTPSettings sftp) {
        super(configFilePath, votingPluginDirectory, sftp);
    }

    /**
     * Ensures JavaScript support is enabled, or lets the user explicitly continue
     * without enabling it.
     *
     * @param parent parent component for the prompt
     * @param feature description of the JavaScript-backed setting being saved
     * @return false only when the user cancels the save
     */
    public boolean ensureEnabled(Component parent, String feature) {
        load();
        if (isEnabled()) {
            return true;
        }

        Object[] options = { "Enable JavaScript", "Continue Disabled", "Cancel" };
        int result = JOptionPane.showOptionDialog(parent,
                feature + " requires JavascriptEngine.Enabled in Config.yml.\n\n"
                        + "Enable JavaScript now? The server must be restarted or reloaded for the change to take effect.",
                "JavaScript is disabled", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                options, options[0]);

        if (result == JOptionPane.YES_OPTION) {
            set(ENABLED_PATH, true);
            save();
            return true;
        }
        return result == JOptionPane.NO_OPTION;
    }

    public boolean isEnabled() {
        Object value = get(ENABLED_PATH, false);
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }

    @Override
    public void openEditorGUI() {
        // This helper has no standalone editor window.
    }
}
