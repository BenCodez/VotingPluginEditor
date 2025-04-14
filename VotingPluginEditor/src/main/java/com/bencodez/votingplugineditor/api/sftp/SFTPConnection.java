
package com.bencodez.votingplugineditor.api.sftp;

import java.io.IOException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class SFTPConnection {

    public static SSHClient createSession(String host, int port, String user, String password) throws IOException {
        SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier()); // Disable strict host key checking
        sshClient.connect(host, port);
        sshClient.authPassword(user, password);
        return sshClient;
    }
}
