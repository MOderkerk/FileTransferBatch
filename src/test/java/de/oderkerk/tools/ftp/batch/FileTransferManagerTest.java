package de.oderkerk.tools.ftp.batch;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.ftp.session.FtpSession;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileTransferManagerTest {


    FTPSClient ftpClient;
    FileTransferManager fileTransferManager ;
    FtpSession ftpSession;

    @BeforeEach
    void setUp(){
        ftpClient = mock(FTPSClient.class);
        ftpSession = mock(FtpSession.class);
        fileTransferManager = new FileTransferManager("test",21,"test","test");
        fileTransferManager.setFtpClient(ftpClient);
        fileTransferManager.setFtpSession(ftpSession);
    }
    @Test
    void connect() throws IOException {


        doNothing().when(ftpClient).connect(Mockito.anyString(),Mockito.anyInt());
        when(ftpClient.login(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        assertDoesNotThrow(fileTransferManager::connect);
    }

    @Test
    void closeConnection() throws IOException {

        doNothing().when(ftpClient).connect(Mockito.anyString(),Mockito.anyInt());
        when(ftpClient.login(Mockito.anyString(),Mockito.anyString())).thenReturn(true);

        assertDoesNotThrow(fileTransferManager::connect);
        assertDoesNotThrow(fileTransferManager::closeConnection);
    }

    @Test
    void downloadFileOkWithClosedSession() throws IOException {
        doNothing().when(ftpClient).connect(Mockito.anyString(),Mockito.anyInt());
        when(ftpClient.login(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        when(ftpSession.isOpen()).thenReturn(false);
        doNothing().when(ftpSession).read(anyString(),any());
        assertDoesNotThrow(()->fileTransferManager.downloadFile("test","test"));


    }
    @Test
    void downloadFileOkWithOpenSession() {


        when(ftpSession.isOpen()).thenReturn(true);
        assertDoesNotThrow(()->fileTransferManager.downloadFile("test","test"));


    }

    @Test
    void uploadFileWithOpenSessionFileNotExisting() throws IOException {
        when(ftpSession.isOpen()).thenReturn(true);
        when(ftpSession.exists(anyString())).thenReturn(false);
        assertDoesNotThrow(()->fileTransferManager.uploadFile("test","test",false,true));
    }
    @Test
    void uploadFileWithOpenSessionFileExisting() throws IOException {
        when(ftpSession.isOpen()).thenReturn(true);
        when(ftpSession.exists(anyString())).thenReturn(true);
        assertDoesNotThrow(()->fileTransferManager.uploadFile("test","test",false,true));
    }
    @Test
    void uploadFileWithOpenSessionFileNotExistingNoOverrite() throws IOException {
        when(ftpSession.isOpen()).thenReturn(true);
        when(ftpSession.exists(anyString())).thenReturn(true);
        assertThrows(FileAlreadyExistsException.class,()->fileTransferManager.uploadFile("test","test",false,false));
    }
    @Test
    void uploadFileWithClosedSessionFileExistingAppend() throws IOException {
        when(ftpSession.isOpen()).thenReturn(false);
        when(ftpSession.exists(anyString())).thenReturn(true);
        assertDoesNotThrow(()->fileTransferManager.uploadFile("test","test",true,false));
    }
}