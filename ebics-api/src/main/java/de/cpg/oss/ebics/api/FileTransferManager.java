package de.cpg.oss.ebics.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileTransferManager {

    int BLOCK_SIZE = 1024 * 1024;

    FileTransfer createUploadTransaction(OrderType orderType,
                                         InputStream inputStream);

    FileTransfer createDownloadTransaction(OrderType orderType,
                                           int numSegments,
                                           byte[] nonce,
                                           byte[] remoteTransactionId);

    FileTransfer save(FileTransfer fileTransfer);

    boolean finalizeUploadTransaction(FileTransfer fileTransfer);

    boolean finalizeDownloadTransaction(FileTransfer fileTransfer,
                                        OutputStream outputStream);

    FileTransferSegment saveSegment(FileTransfer fileTransfer, byte[] orderData);

    FileTransferSegment loadSegment(FileTransfer fileTransfer);
}
