package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.FileTransaction;
import de.cpg.oss.ebics.api.FileTransferManager;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.UUID;

abstract class AbstractFileTransferManager implements FileTransferManager {

    FileTransaction createUploadTransaction(final OrderType orderType,
                                            final InputStream inputStream,
                                            final UUID transactionId,
                                            final byte[] nonce) throws EbicsException {
        try {
            final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.EBICS_DIGEST_ALGORITHM);
            final InputStream compressedAndEncrypted = CryptoUtil.encryptAES(
                    ZipUtil.compress(CryptoUtil.digest(inputStream, digester)),
                    nonce);

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
                    .orderType(orderType)
                    .numSegments(segmentNumber)
                    .digest(digester.digest())
                    .nonce(nonce)
                    .id(transactionId)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(e);
        }
    }

    FileTransaction createDownloadTransaction(final OrderType orderType,
                                              final int numSegments,
                                              final byte[] nonce,
                                              final UUID transactionId,
                                              final byte[] remoteTransactionId) {
        return FileTransaction.builder()
                .orderType(orderType)
                .numSegments(numSegments)
                .nonce(nonce)
                .id(transactionId)
                .remoteTransactionId(remoteTransactionId)
                .build();
    }

    void writeOutput(final FileTransaction fileTransaction,
                     final OutputStream outputStream) throws EbicsException, IOException {
        for (int i = 0; i < fileTransaction.getNumSegments(); i++) {
            outputStream.write(IOUtil.read(ZipUtil.uncompress(CryptoUtil.decryptAES(
                    readSegment(i + 1),
                    fileTransaction.getNonce()))));
        }
    }
}
