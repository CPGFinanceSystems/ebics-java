package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransaction;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultFileTransferManager extends AbstractFileTransferManager {

    @NonNull
    private final EbicsSession session;
    @NonNull
    private final UUID transactionId;
    @NonNull
    private final byte[] nonce;

    public static DefaultFileTransferManager create(final EbicsSession session) {
        return DefaultFileTransferManager.builder()
                .session(session)
                .transactionId(UUID.randomUUID())
                .nonce(CryptoUtil.generateNonce())
                .build();
    }

    public static DefaultFileTransferManager of(final EbicsSession session, final FileTransaction fileTransaction) {
        return DefaultFileTransferManager.builder()
                .session(session)
                .transactionId(fileTransaction.getId())
                .nonce(fileTransaction.getNonce())
                .build();
    }

    @Override
    public FileTransaction createUploadTransaction(final OrderType orderType,
                                                   final InputStream inputStream) throws EbicsException {
        IOUtil.createDirectories(transactionDir());
        return createUploadTransaction(orderType, inputStream, transactionId, nonce);
    }

    @Override
    public FileTransaction createDownloadTransaction(final OrderType orderType,
                                                     final int numSegments,
                                                     final byte[] nonce,
                                                     final byte[] transactionId) throws EbicsException {
        IOUtil.createDirectories(transactionDir());
        return createDownloadTransaction(orderType, numSegments, nonce, this.transactionId, transactionId);
    }

    @Override
    public InputStream readSegment(final int segmentNumber) throws IOException {
        log.debug("Read segment {}", segmentNumber);
        return new FileInputStream(segmentFile(segmentNumber));
    }

    @Override
    public void writeSegment(final int segmentNumber, final byte[] orderData, final int orderDataLen) throws IOException {
        log.debug("Write segment {}", segmentNumber);
        try (final OutputStream outputStream = new FileOutputStream(segmentFile(segmentNumber))) {
            outputStream.write(orderData, 0, orderDataLen);
        }
    }

    @Override
    public boolean finalizeUploadTransaction(final FileTransaction fileTransaction) {
        return cleanupTransactionDir();
    }

    @Override
    public boolean finalizeDownloadTransaction(final FileTransaction fileTransaction,
                                               final OutputStream outputStream) throws EbicsException {
        try {
            writeOutput(fileTransaction, outputStream);
            return cleanupTransactionDir();
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    /**
     * @param segmentNumber segment number starting at <code>1</code>
     * @return file location of segment file
     */
    private File segmentFile(final int segmentNumber) {
        return new File(transactionDir(), String.format("%04d.bin", segmentNumber - 1));
    }

    private File transactionDir() {
        return new File(session.getConfiguration().getTransferFilesDirectory(session.getUser()),
                transactionId.toString());
    }

    private boolean cleanupTransactionDir() {
        Optional.ofNullable(transactionDir().listFiles())
                .ifPresent(files -> Stream.of(files).forEach(File::delete));
        return transactionDir().delete();
    }
}
