package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileTransferManager {

    int BLOCK_SIZE = 1024 * 1024;

    FileTransaction createUploadTransaction(OrderType orderType, InputStream inputStream) throws EbicsException;

    FileTransaction createDownloadTransaction(OrderType orderType,
                                              int numSegments,
                                              byte[] nonce,
                                              byte[] remoteTransactionId) throws EbicsException;

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @return compressed and encrypted segment
     */
    InputStream readSegment(int segmentNumber) throws IOException;

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @param orderData     compressed and encrypted segment
     * @param orderDataLen  length of segment
     */
    void writeSegment(int segmentNumber, byte[] orderData, int orderDataLen) throws IOException;

    default void writeSegment(final int segmentNumber, final byte[] orderData) throws IOException {
        writeSegment(segmentNumber, orderData, orderData.length);
    }

    boolean finalizeUploadTransaction(FileTransaction fileTransaction) throws EbicsException;

    boolean finalizeDownloadTransaction(FileTransaction fileTransaction,
                                        OutputStream outputStream) throws EbicsException;
}
