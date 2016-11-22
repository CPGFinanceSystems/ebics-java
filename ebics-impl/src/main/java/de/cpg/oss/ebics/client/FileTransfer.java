package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransaction;
import de.cpg.oss.ebics.api.FileTransferManager;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.DefaultFileTransferManager;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;

import java.io.*;
import java.time.LocalDate;


/**
 * Handling of file transfers.
 * Files can be transferred to and fetched from the bank.
 * Every transfer may be performed in a recoverable way.
 * For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call.
 * To use the recoverable transfer mode, you may set a working
 * directory for temporarily created files.
 * <p>
 * <p> EBICS specification 2.4.2 - 6.2 Encryption at application level
 * <p>
 * <p>In the event of an upload transaction, a random symmetrical key is generated in the
 * customer system that is used exclusively within the framework of this transaction both for
 * encryption of the ES’s and for encryption of the order data. This key is encrypted
 * asymmetrically with the financial institution’s public encryption key and is transmitted by the
 * customer system to the bank system during the initialization phase of the transaction.
 * <p>
 * <p>Analogously, in the case of a download transaction a random symmetrical key is generated
 * in the bank system that is used for encryption of the order data that is to be downloaded and
 * for encryption of the bank-technical signature that has been provided by the financial
 * institution. This key is asymmetrically encrypted and is transmitted by the bank system to the
 * customer system during the initialization phase of the transaction. The asymmetrical
 * encryption takes place with the technical subscriber’s public encryption key if the
 * transaction’s EBICS messages are sent by a technical subscriber. Otherwise the
 * asymmetrical encryption takes place with the public encryption key of the non-technical
 * subscriber, i.e. the submitter of the order.
 *
 * @author Hachani
 */
@Slf4j
abstract class FileTransfer {

    static FileTransaction createFileUploadTransaction(
            final EbicsSession session,
            final File inputFile,
            final OrderType orderType) throws FileNotFoundException, EbicsException {
        try {
            return DefaultFileTransferManager.create(session)
                    .createUploadTransaction(new FileInputStream(inputFile))
                    .withOrderType(orderType);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    static FileTransaction uploadFile(final EbicsSession session,
                                      final FileTransaction transaction) throws EbicsException {
        final FileTransferManager transferManager = DefaultFileTransferManager.of(session, transaction);

        FileTransaction current;
        if (null == transaction.getRemoteTransactionId() || 1 == transaction.getSegmentNumber()) {
            current = uploadInitRequest(session, transaction);
        } else {
            current = transaction;
        }

        while (current.hasNext()) {
            current = uploadSegment(session, current.next(), transferManager);
        }

        transferManager.finalizeUploadTransaction();

        return current;
    }

    static FileTransaction createFileDownloadTransaction(
            final EbicsSession session,
            final OrderType orderType,
            final LocalDate start,
            final LocalDate end) throws FileNotFoundException, EbicsException {
        final EbicsRequest request = DInitializationRequestElement.builder()
                .orderType(orderType)
                .startRange(start)
                .endRange(end)
                .build().create(session);

        final DInitializationResponseElement responseElement = ClientUtil.requestExchange(session, request,
                DInitializationResponseElement::parse);

        final FileTransferManager transferManager = DefaultFileTransferManager.create(session);
        final FileTransaction fileTransaction = transferManager.createDownloadTransaction(
                responseElement.getNumSegments(),
                responseElement.getTransactionKey(),
                responseElement.getTransactionId());

        try {
            transferManager.writeSegment(fileTransaction.getSegmentNumber(), responseElement.getOrderData());
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return fileTransaction.withOrderType(orderType);
    }

    static FileTransaction downloadFile(final EbicsSession session,
                                        final FileTransaction transaction,
                                        final File outputFile) throws EbicsException, FileNotFoundException {
        final FileTransferManager transferManager = DefaultFileTransferManager.of(session, transaction);
        FileTransaction current = transaction;
        while (current.hasNext()) {
            current = downloadSegment(session, current.next(), transferManager);
        }

        final EbicsRequest ebicsRequest = new ReceiptRequestElement(current.getRemoteTransactionId())
                .create(session);
        ClientUtil.requestExchange(session, ebicsRequest, ReceiptResponseElement::parse);

        try {
            transferManager.finalizeDownloadTransaction(transaction, new FileOutputStream(outputFile));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return current;
    }

    private static FileTransaction uploadInitRequest(final EbicsSession session,
                                                     final FileTransaction transaction) throws EbicsException {
        final EbicsRequest request = UInitializationRequestElement.builder()
                .orderType(transaction.getOrderType())
                .digest(transaction.getDigest())
                .numSegments(transaction.getNumSegments())
                .build().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, request);
        return transaction.withRemoteTransactionId(responseElement.getTransactionId());
    }

    private static FileTransaction uploadSegment(final EbicsSession session,
                                                 final FileTransaction fileTransaction,
                                                 final FileTransferManager transferManager) throws EbicsException {
        log.info(session.getMessageProvider().getString(
                "upload.segment",
                Constants.APPLICATION_BUNDLE_NAME,
                fileTransaction.getSegmentNumber()));
        final EbicsRequest ebicsRequest;
        try {
            ebicsRequest = UTransferRequestElement.builder()
                    .segmentNumber(fileTransaction.getSegmentNumber())
                    .lastSegment(fileTransaction.isLastSegment())
                    .transactionId(fileTransaction.getRemoteTransactionId())
                    .content(transferManager.readSegment(fileTransaction.getSegmentNumber()))
                    .build().create(session);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        ClientUtil.requestExchange(session, ebicsRequest);

        return fileTransaction;
    }

    private static FileTransaction downloadSegment(final EbicsSession session,
                                                   final FileTransaction fileTransaction,
                                                   final FileTransferManager transferManager) throws EbicsException {
        final EbicsRequest ebicsRequest = DTransferRequestElement.builder()
                .segmentNumber(fileTransaction.getSegmentNumber())
                .lastSegment(fileTransaction.isLastSegment())
                .transactionId(fileTransaction.getRemoteTransactionId())
                .build().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        try {
            transferManager.writeSegment(fileTransaction.getSegmentNumber(), responseElement.getOrderData());
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return fileTransaction;
    }
}
