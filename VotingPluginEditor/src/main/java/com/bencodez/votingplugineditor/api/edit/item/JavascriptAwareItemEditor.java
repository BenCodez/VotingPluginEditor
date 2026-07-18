package com.bencodez.votingplugineditor.api.edit.item;

import java.util.Map;

import com.bencodez.votingplugineditor.api.misc.JavascriptRequirementHandler;
import com.bencodez.votingplugineditor.api.sftp.SFTPSettings;

/**
 * Item editor variant that checks VotingPlugin's current JavaScript engine
 * setting before saving ConditionalJavascript values.
 */
public abstract class JavascriptAwareItemEditor extends ItemEditor {

    public JavascriptAwareItemEditor(Map<String, Object> data, String configFilePath,
            String votingPluginDirectory, SFTPSettings sftp) {
        super(data, new JavascriptRequirementHandler(configFilePath, votingPluginDirectory, sftp));
    }
}
