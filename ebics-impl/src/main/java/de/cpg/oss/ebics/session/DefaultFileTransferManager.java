package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import javaslang.collection.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DefaultFileTransferManager implements FileTransferManager {

    private final PersistenceProvider persistenceProvider;

    public DefaultFileTransferManager(final PersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    @Override
    public FileTransfer createUploadTransaction(final OrderType orderType,
                                                final InputStream inputStream) throws EbicsException {
        return save(createUploadTransfer(orderType, inputStream, UUID.randomUUID(), CryptoUtil.generateNonce()));
    }

    @Override
    public FileTransfer createDownloadTransaction(final OrderType orderType,
                                                  final int numSegments,
                                                  final byte[] nonce,
                                                  final byte[] transactionId) throws EbicsException {
        return save(FileTransfer.builder()
                .orderType(orderType)
                .segmentNumber(1)
                .segmentIds(Stream.range(0, numSegments).map(i -> UUID.randomUUID()).toJavaList())
                .nonce(nonce)
                .transferId(UUID.randomUUID())
                .transactionId(transactionId)
                .build());
    }

    @Override
    public boolean finalizeUploadTransaction(final FileTransfer fileTransfer) throws EbicsException {
        return delete(fileTransfer);
    }

    @Override
    public boolean finalizeDownloadTransaction(final FileTransfer fileTransfer,
                                               final OutputStream outputStream) throws EbicsException {
        try {
            writeOutput(fileTransfer, outputStream);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
        return delete(fileTransfer);
    }

    @Override
    public FileTransfer save(final FileTransfer fileTransfer) throws EbicsException {
        try {
            return persistenceProvider.save(FileTransfer.class, fileTransfer);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    @Override
    public FileTransferSegment saveSegment(final FileTransfer fileTransfer, final byte[] content) throws EbicsException {
        try {
            return persistenceProvider.save(
                    FileTransferSegment.class,
                    FileTransferSegment.valueOf(
                            fileTransfer.getSegmentIds().get(fileTransfer.getSegmentNumber() - 1),
                            content));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    @Override
    public FileTransferSegment loadSegment(final FileTransfer fileTransfer) throws EbicsException {
        try {
            return persistenceProvider.load(FileTransferSegment.class, fileTransfer.getSegmentIds().get(fileTransfer.getSegmentNumber() - 1).toString());
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    private FileTransfer createUploadTransfer(final OrderType orderType,
                                              final InputStream inputStream,
                                              final UUID transferId,
                                              final byte[] nonce) throws EbicsException {
        try {
            final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.EBICS_DIGEST_ALGORITHM);
            final InputStream compressedAndEncrypted = CryptoUtil.encryptAES(
                    ZipUtil.compress(CryptoUtil.digest(inputStream, digester)),
                    nonce);
            final List<UUID> segmentIds = new ArrayList<>();

            final byte[] block = new byte[BLOCK_SIZE];
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
                segmentIds.add(persistenceProvider.save(
                        FileTransferSegment.class,
                        FileTransferSegment.create(block, blockBytesRead))
                        .getSegmentId());
            } while (bytesRead != -1);

            return FileTransfer.builder()
                    .orderType(orderType)
                    .segmentIds(segmentIds)
                    .digest(digester.digest())
                    .nonce(nonce)
                    .transferId(transferId)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(e);
        }
    }

    private void writeOutput(final FileTransfer fileTransfer,
                             final OutputStream outputStream) throws EbicsException, IOException {
        for (final UUID segmentId : fileTransfer.getSegmentIds()) {
            outputStream.write(IOUtil.read(ZipUtil.uncompress(CryptoUtil.decryptAES(
                    IOUtil.wrap(persistenceProvider.load(FileTransferSegment.class, segmentId.toString()).getContent()),
                    fileTransfer.getNonce()))));
        }
    }

    private boolean delete(final FileTransfer fileTransfer) throws EbicsException {
        try {
            if (persistenceProvider.delete(fileTransfer)) {
                for (final UUID segmentId : fileTransfer.getSegmentIds()) {
                    persistenceProvider.delete(FileTransferSegment.class, segmentId.toString());
                }
                return true;
            }
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
        return false;
    }
}
