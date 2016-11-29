package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileTransferManager {

    int BLOCK_SIZE = 1024 * 1024;

    FileTransfer createUploadTransaction(OrderType orderType,
                                         InputStream inputStream) throws EbicsException;

    FileTransfer createDownloadTransaction(OrderType orderType,
                                           int numSegments,
                                           byte[] nonce,
                                           byte[] remoteTransactionId) throws EbicsException;

    FileTransfer save(FileTransfer fileTransfer) throws EbicsException;

    boolean finalizeUploadTransaction(FileTransfer fileTransfer) throws EbicsException;

    boolean finalizeDownloadTransaction(FileTransfer fileTransfer,
                                        OutputStream outputStream) throws EbicsException;

    FileTransferSegment saveSegment(FileTransfer fileTransfer, byte[] orderData) throws EbicsException;

    FileTransferSegment loadSegment(FileTransfer fileTransfer) throws EbicsException;
}
