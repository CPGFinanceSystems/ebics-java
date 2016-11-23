package de.cpg.oss.ebics.io;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransaction;
import de.cpg.oss.ebics.api.FileTransferManager;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public final class DefaultFileTransferManager implements FileTransferManager {

    @NonNull
    private final EbicsSession session;
    @NonNull
    private final UUID transactionId;
    @NonNull
    private final byte[] nonce;

    @Builder
    private DefaultFileTransferManager(final EbicsSession session, final UUID transactionId, final byte[] nonce) {
        this.session = session;
        this.transactionId = transactionId;
        this.nonce = nonce;
    }

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

    public FileTransaction.Builder createUploadTransaction(final InputStream inputStream) throws EbicsException {
        IOUtil.createDirectories(transactionDir());
        return createUploadTransaction(inputStream, transactionId, nonce);
    }

    public FileTransaction.Builder createDownloadTransaction(final int numSegments,
                                                             final byte[] nonce,
                                                             final byte[] transactionId) throws EbicsException {
        IOUtil.createDirectories(transactionDir());
        return createDownloadTransaction(numSegments, this.transactionId, nonce, transactionId);
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
    public boolean finalizeUploadTransaction() {
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
