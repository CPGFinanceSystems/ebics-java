package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.UUID;

public interface FileTransferManager {

    String CRYPTO_ALGORITHM = "AES";
    int BLOCK_SIZE = 1024 * 1024;

    default FileTransaction.Builder createUploadTransaction(final InputStream inputStream,
                                                            final UUID transactionId,
                                                            final byte[] nonce) throws EbicsException {
        try {
            final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.DIGEST_ALGORITHM);
            final InputStream compressedAndEncrypted = CryptoUtil.encrypt(
                    ZipUtil.compress(CryptoUtil.digest(inputStream, digester)),
                    new SecretKeySpec(nonce, CRYPTO_ALGORITHM));

            final byte[] block = new byte[BLOCK_SIZE];
            int segmentNumber = 0;
            int bytesRead = 0;
            do {
                int blockBytesRead = 0;
                while (blockBytesRead < block.length) {
                    bytesRead = compressedAndEncrypted.read(block, blockBytesRead, block.length - blockBytesRead);
                    if (bytesRead == -1) {
                        break;
                    }
                    blockBytesRead += bytesRead;
                }
                writeSegment(++segmentNumber, block, blockBytesRead);
            } while (bytesRead != -1);

            return FileTransaction.builder()
                    .numSegments(segmentNumber)
                    .digest(digester.digest())
                    .nonce(nonce)
                    .id(transactionId);
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(e);
        }
    }

    default FileTransaction.Builder createDownloadTransaction(final int numSegments,
                                                              final UUID transactionId,
                                                              final byte[] nonce,
                                                              final byte[] remoteTransactionId) throws EbicsException {
        return FileTransaction.builder()
                .numSegments(numSegments)
                .nonce(nonce)
                .id(transactionId)
                .remoteTransactionId(remoteTransactionId);
    }

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @return compressed and encrypted segment
     */
    byte[] readSegment(int segmentNumber) throws IOException;

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @param orderData     compressed and encrypted segment
     * @param orderDataLen  length of segment
     */
    void writeSegment(int segmentNumber, byte[] orderData, int orderDataLen) throws IOException;

    default void writeSegment(final int segmentNumber, final byte[] orderData) throws IOException {
        writeSegment(segmentNumber, orderData, orderData.length);
    }

    boolean finalizeUploadTransaction();

    boolean finalizeDownloadTransaction(FileTransaction fileTransaction,
                                        OutputStream outputStream) throws EbicsException;

    default void writeOutput(final FileTransaction fileTransaction,
                             final OutputStream outputStream) throws EbicsException, IOException {
        for (int i = 0; i < fileTransaction.getNumSegments(); i++) {
            outputStream.write(ZipUtil.uncompress(CryptoUtil.decrypt(
                    readSegment(i + 1),
                    new SecretKeySpec(fileTransaction.getNonce(), CRYPTO_ALGORITHM))));
        }
    }
}
