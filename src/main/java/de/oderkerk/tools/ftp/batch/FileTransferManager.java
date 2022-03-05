package de.oderkerk.tools.ftp.batch;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Objects;

/**
 * Ftp Client for all transfer functions
 */
@Component
@Slf4j
@Getter
@Setter
public class FileTransferManager {

    /**
     * File Transfer manager init
     *
     * @param host     target server
     * @param port     target port
     * @param user     user for login
     * @param password password for login
     */
    public FileTransferManager(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }


    /**
     * ftp session to use
     */
    private FtpSession ftpSession;
    /**
     * Ftp Client
     */
    private FTPClient ftpClient;
    /**
     * target server for connection
     */
    private String host;
    /**
     *
     */
    private String user;
    private int port;
    private String password;

    /**
     * Connect to the ftp server
     *
     * @throws IOException Ftp connection errors
     */
    public void connect() throws IOException {
        log.info("Connect to FTP server {} on port {}", this.host, this.port);
        if (Objects.isNull(this.ftpClient)) ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        ftpClient.login(user, password);
        if (Objects.isNull(ftpSession)) this.ftpSession = new FtpSession(ftpClient);
    }

    /**
     * Close the connection to the ftp server
     */
    public void closeConnection() {
        log.info("Closing connection");
        if (!ftpSession.isOpen()) {
            ftpSession.close();
        }

    }

    /**
     * download source to target
     *
     * @param source source on the ftp server
     * @param target local target
     * @throws IOException Ftp connection errors
     */
    public void downloadFile(String source, String target) throws IOException {
        log.info("Start downloading file {} to {}", source, target);
        if (!ftpSession.isOpen()) {
            connect();
        }
        FileOutputStream fos = new FileOutputStream(target);
        ftpSession.read(source, fos);
        fos.close();
        log.info("Complete downloading file {} to {}", source, target);

    }

    /**
     * upload the source to target
     *
     * @param source    local file
     * @param target    target path
     * @param append    abbend data to existing file
     * @param overwrite overwrite (true) if file exists , else get an error
     * @throws IOException Ftp connection errors
     */
    public void uploadFile(String source, String target, boolean append, boolean overwrite) throws IOException {
        log.info("Start uploading file {} to {}", source, target);
        if (!ftpSession.isOpen()) {
            connect();
        }
        if (ftpSession.exists(target)) {
            if (overwrite) {
                log.info("Overwriting target file");
                ftpSession.remove(target);
            } else {
                if (!append) throw new FileAlreadyExistsException(target + " already exists");
            }
        }
        FileInputStream fis = new FileInputStream(source);

        if (append) {
            log.info("Appending data to file");
            ftpSession.append(fis, target);
        } else {
            log.info("Sending new file");
            ftpSession.write(fis, target);
        }
        fis.close();
        log.info("Complete uploading file {} to {}", source, target);

    }
}
