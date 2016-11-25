package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public final class DefaultFileTransferManager extends AbstractFileTransferManager {

    @NonNull
    private final EbicsConfiguration configuration;
    @NonNull
    private final SerializationManager serializationManager;

    @Override
    public FileTransfer createUploadTransaction(final EbicsUser user,
                                                final OrderType orderType,
                                                final InputStream inputStream) throws EbicsException {
        final UUID transferId = UUID.randomUUID();
        IOUtil.createDirectories(transactionDir(user, transferId));
        return save(createUploadTransfer(user, orderType, inputStream, transferId, CryptoUtil.generateNonce()));
    }

    @Override
    public FileTransfer createDownloadTransaction(final EbicsUser user,
                                                  final OrderType orderType,
                                                  final int numSegments,
                                                  final byte[] nonce,
                                                  final byte[] transactionId) throws EbicsException {
        final UUID transferId = UUID.randomUUID();
        IOUtil.createDirectories(transactionDir(user, transferId));
        return save(FileTransfer.builder()
                .orderType(orderType)
                .numSegments(numSegments)
                .nonce(nonce)
                .transferId(transferId)
                .transactionId(transactionId)
                .build());
    }

    @Override
    public InputStream readSegment(final EbicsUser user,
                                   final UUID transferId,
                                   final int segmentNumber) throws IOException {
        log.debug("Read segment {}", segmentNumber);
        return new FileInputStream(segmentFile(user, transferId, segmentNumber));
    }

    @Override
    public void writeSegment(final EbicsUser user,
                             final UUID transferId,
                             final int segmentNumber,
                             final byte[] orderData,
                             final int orderDataLen) throws IOException {
        log.debug("Write segment {}", segmentNumber);
        try (final OutputStream outputStream = new FileOutputStream(segmentFile(user, transferId, segmentNumber))) {
            outputStream.write(orderData, 0, orderDataLen);
        }
    }

    @Override
    public boolean finalizeUploadTransaction(final EbicsUser user,
                                             final FileTransfer fileTransfer) throws EbicsException {
        try {
            serializationManager.delete(fileTransfer);
            return cleanupTransactionDir(user, fileTransfer.getTransferId());
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    @Override
    public boolean finalizeDownloadTransaction(final EbicsUser user,
                                               final FileTransfer fileTransfer,
                                               final OutputStream outputStream) throws EbicsException {
        try {
            writeOutput(user, fileTransfer, outputStream);
            serializationManager.delete(fileTransfer);
            return cleanupTransactionDir(user, fileTransfer.getTransferId());
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    @Override
    public FileTransfer save(final FileTransfer fileTransfer) throws EbicsException {
        try {
            serializationManager.serialize(FileTransfer.class, fileTransfer);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
        return fileTransfer;
    }

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @return file location of segment file
     */
    private File segmentFile(final EbicsUser user, final UUID transferId, final int segmentNumber) {
        return new File(transactionDir(user, transferId), String.format("%04d.bin", segmentNumber - 1));
    }

    private File transactionDir(final EbicsUser user, final UUID transferId) {
        return new File(configuration.getTransferFilesDirectory(user), transferId.toString());
    }

    private boolean cleanupTransactionDir(final EbicsUser user, final UUID transferId) {
        Optional.ofNullable(transactionDir(user, transferId).listFiles())
                .ifPresent(files -> Stream.of(files).forEach(File::delete));
        return transactionDir(user, transferId).delete();
    }
}
