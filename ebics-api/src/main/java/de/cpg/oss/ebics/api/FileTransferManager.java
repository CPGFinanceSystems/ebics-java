package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public interface FileTransferManager {

    int BLOCK_SIZE = 1024 * 1024;

    FileTransfer createUploadTransaction(EbicsUser user,
                                         OrderType orderType,
                                         InputStream inputStream) throws EbicsException;

    FileTransfer createDownloadTransaction(EbicsUser user,
                                           OrderType orderType,
                                           int numSegments,
                                           byte[] nonce,
                                           byte[] remoteTransactionId) throws EbicsException;

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @return compressed and encrypted segment
     */
    InputStream readSegment(EbicsUser user,
                            UUID transactionId,
                            int segmentNumber) throws IOException;

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @param orderData     compressed and encrypted segment
     * @param orderDataLen  length of segment
     */
    void writeSegment(EbicsUser user,
                      UUID transactionId,
                      int segmentNumber,
                      byte[] orderData,
                      int orderDataLen) throws IOException;

    default void writeSegment(final EbicsUser user,
                              final UUID transactionId,
                              final int segmentNumber,
                              final byte[] orderData) throws IOException {
        writeSegment(user, transactionId, segmentNumber, orderData, orderData.length);
    }

    FileTransfer save(FileTransfer fileTransfer) throws EbicsException;

    boolean finalizeUploadTransaction(EbicsUser user, FileTransfer fileTransfer) throws EbicsException;

    boolean finalizeDownloadTransaction(EbicsUser user,
                                        FileTransfer fileTransfer,
                                        OutputStream outputStream) throws EbicsException;
}
