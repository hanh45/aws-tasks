package com.dm.awstasks.ec2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dm.awstasks.ec2.ssh.SshClient;

public class InstanceInteractionIntegTest extends AbstractEc2IntegrationInteractionTest {

    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

    @Test
    public void testScpUploadToAllInstances() throws Exception {
        File privateKeyFile = new File(_ec2Conf.getPrivateKeyFile());
        SshClient sshClient = _instanceGroup.createSshClient("ubuntu", privateKeyFile);
        File localFile = new File("README.markdown");
        String remoteDir = "~/";
        sshClient.uploadFile(localFile, remoteDir);

        File localDestinationFolder = _folder.newFolder("localDestinationFolder");
        sshClient.downloadFile(remoteDir + localFile.getName(), localDestinationFolder, false);
        assertEquals(1, localDestinationFolder.list().length);
        assertEquals(localFile.length(), new File(localDestinationFolder, localFile.getName()).length());
    }

    @Test
    public void testScpUploadToSpecificInstances() throws Exception {
        File privateKeyFile = new File(_ec2Conf.getPrivateKeyFile());
        SshClient sshClient = _instanceGroup.createSshClient("ubuntu", privateKeyFile);
        File localFile = new File("build.xml");
        String remoteDir = "~/";
        sshClient.uploadFile(localFile, remoteDir, new int[] { 0 });

        File localDestinationFolder = _folder.newFolder("localDestinationFolder");
        sshClient.downloadFile(remoteDir + localFile.getName(), localDestinationFolder, false, new int[] { 0 });
        assertEquals(1, localDestinationFolder.list().length);
        assertEquals(localFile.length(), new File(localDestinationFolder, localFile.getName()).length());

        try {
            sshClient.downloadFile(remoteDir + localFile.getName(), localDestinationFolder, false, new int[] { 1 });
            fail("should throw exception");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testSshExecutionToAllInstances() throws Exception {
        File privateKeyFile = new File(_ec2Conf.getPrivateKeyFile());
        SshClient sshClient = _instanceGroup.createSshClient("ubuntu", privateKeyFile);
        sshClient.executeCommand("ls -l");
        String noneExistingFile = "abcfi";
        try {
            sshClient.executeCommand("rm " + noneExistingFile);
            fail("should throw exception");
        } catch (Exception e) {
            // expected
        }
        sshClient.executeCommand("touch " + noneExistingFile);
        sshClient.executeCommand("rm " + noneExistingFile);
    }

    @Test
    public void testShhExecutionToSpecificInstances() throws Exception {
        File privateKeyFile = new File(_ec2Conf.getPrivateKeyFile());
        SshClient sshClient1 = _instanceGroup.createSshClient("ubuntu", privateKeyFile);

        String noneExistingFile = "abcfi";
        sshClient1.executeCommand("touch " + noneExistingFile, new int[] { 0 });
        sshClient1.executeCommand("rm " + noneExistingFile, new int[] { 0 });
        try {
            sshClient1.executeCommand("rm " + noneExistingFile, new int[] { 1 });
            fail("should throw exception");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testSshExecutionFromFile() throws Exception {
        File privateKeyFile = new File(_ec2Conf.getPrivateKeyFile());
        SshClient sshClient = _instanceGroup.createSshClient("ubuntu", privateKeyFile);
        File commandFile = _folder.newFile("commands.txt");
        FileWriter fileWriter = new FileWriter(commandFile);
        fileWriter.write("ls -l\n");
        String noneExistingFile = "abcfi";
        fileWriter.write("touch " + noneExistingFile + "\n");
        fileWriter.write("rm " + noneExistingFile + "\n");
        fileWriter.close();
        sshClient.executeCommandFile(commandFile, new int[] { 0 });
    }

}
