package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import javax.crypto.spec.SecretKeySpec;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.UUID;

public interface FileTransferManager {

    String CRYPTO_ALGORITHM = "AES";
    int BLOCK_SIZE = 1024 * 1024;

    default FileTransaction createUploadTransaction(final InputStream inputStream) throws EbicsException {
        try {
            final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.DIGEST_ALGORITHM);
            final InputStream inputStreamListenerForDigest = new FilterInputStream(inputStream) {
                @Override
                public int read() throws IOException {
                    final int read = inputStream.read();
                    digester.update((byte) read);
                    return read;
                }

                @Override
                public int read(final byte[] b) throws IOException {
                    final int count = super.read(b);
                    digester.update(b);
                    return count;
                }

                @Override
                public int read(final byte[] b, final int off, final int len) throws IOException {
                    final int count = super.read(b, off, len);
                    digester.update(b, off, len);
                    return count;
                }
            };

            final byte[] nonce = CryptoUtil.generateNonce();
            final InputStream compressedAndEncrypted = CryptoUtil.encrypt(
                    ZipUtil.compress(inputStreamListenerForDigest),
                    new SecretKeySpec(nonce, CRYPTO_ALGORITHM));

            final byte[] block = new byte[BLOCK_SIZE];
            int segmentIndex = 0;
            int bytesRead;

            while ((bytesRead = compressedAndEncrypted.read(block, segmentIndex * BLOCK_SIZE, BLOCK_SIZE)) != -1) {
                writeSegment(segmentIndex + 1, block, bytesRead);
                segmentIndex++;
            }
            return FileTransaction.builder()
                    .segmentNumber(1)
                    .numSegments(segmentIndex + 1)
                    .digest(digester.digest())
                    .nonce(nonce)
                    .id(UUID.randomUUID())
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(e);
        }
    }

    default FileTransaction createDownloadTransaction(final int numSegments,
                                                      final byte[] nonce,
                                                      final byte[] transactionId) throws EbicsException {
        return FileTransaction.builder()
                .segmentNumber(1)
                .numSegments(numSegments)
                .nonce(nonce)
                .id(UUID.randomUUID())
                .remoteTransactionId(transactionId)
                .build();
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
