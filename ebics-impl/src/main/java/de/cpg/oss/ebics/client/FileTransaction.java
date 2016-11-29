package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransfer;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;

import java.io.*;
import java.text.MessageFormat;
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
abstract class FileTransaction {

    static FileTransfer createFileUploadTransaction(
            final EbicsSession session,
            final File inputFile,
            final OrderType orderType) throws FileNotFoundException, EbicsException {
        try {
            return session.getFileTransferManager().createUploadTransaction(
                    orderType,
                    new FileInputStream(inputFile));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    static FileTransfer uploadFile(final EbicsSession session,
                                   final FileTransfer transaction) throws EbicsException {
        FileTransfer current = transaction;
        try {
            if (null == transaction.getTransactionId() || 0 == transaction.getSegmentNumber()) {
                current = uploadInitRequest(session, transaction);
            }
            while (current.hasNext()) {
                current = uploadSegment(session, current.next());
            }
        } catch (final Exception e) {
            log.error(
                    MessageFormat.format("Error uploading segment {} of {} for file transfer with ID {}",
                            current.getSegmentNumber(),
                            current.getNumSegments(),
                            current.getTransferId()),
                    e);
            return current;
        }

        session.getFileTransferManager().finalizeUploadTransaction(current);

        return current;
    }

    static FileTransfer createFileDownloadTransaction(
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

        final FileTransfer fileTransfer = session.getFileTransferManager().createDownloadTransaction(
                orderType,
                responseElement.getNumSegments(),
                CryptoUtil.decryptRSA(responseElement.getTransactionKey(), session.getUserEncryptionKey()),
                responseElement.getTransactionId());

        session.getFileTransferManager().saveSegment(fileTransfer, responseElement.getOrderData());

        return fileTransfer;
    }

    static FileTransfer downloadFile(final EbicsSession session,
                                     final FileTransfer transaction,
                                     final File outputFile) throws EbicsException, FileNotFoundException {
        FileTransfer current = transaction;
        try {
            while (current.hasNext()) {
                current = downloadSegment(session, current.next());
            }
        } catch (final Exception e) {
            log.error(
                    MessageFormat.format("Error downloading segment {} of {} for file transfer with ID {}",
                            current.getSegmentNumber(),
                            current.getNumSegments(),
                            current.getTransferId()),
                    e);
            return current;
        }

        final EbicsRequest ebicsRequest = new ReceiptRequestElement(current.getTransactionId())
                .create(session);
        ClientUtil.requestExchange(session, ebicsRequest, ReceiptResponseElement::parse);

        try {
            session.getFileTransferManager().finalizeDownloadTransaction(
                    transaction,
                    new FileOutputStream(outputFile));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return current;
    }

    private static FileTransfer uploadInitRequest(final EbicsSession session,
                                                  final FileTransfer transaction) throws EbicsException {
        final EbicsRequest request = UInitializationRequestElement.builder()
                .orderType(transaction.getOrderType())
                .digest(transaction.getDigest())
                .numSegments(transaction.getNumSegments())
                .nonce(transaction.getNonce())
                .build().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, request);
        return transaction.withTransactionId(responseElement.getTransactionId());
    }

    private static FileTransfer uploadSegment(final EbicsSession session,
                                              final FileTransfer fileTransfer) throws EbicsException {
        log.debug("Upload segment number {} of {}", fileTransfer.getSegmentNumber(), fileTransfer.getNumSegments());

        final EbicsRequest ebicsRequest;
        ebicsRequest = UTransferRequestElement.builder()
                .segmentNumber(fileTransfer.getSegmentNumber())
                .lastSegment(fileTransfer.isLastSegment())
                .transactionId(fileTransfer.getTransactionId())
                .content(session.getFileTransferManager().loadSegment(fileTransfer).getContent())
                .build().create(session);

        ClientUtil.requestExchange(session, ebicsRequest);

        return fileTransfer;
    }

    private static FileTransfer downloadSegment(final EbicsSession session,
                                                final FileTransfer fileTransfer) throws EbicsException {
        log.debug("Download segment number {} of {}", fileTransfer.getSegmentNumber(), fileTransfer.getNumSegments());

        final EbicsRequest ebicsRequest = DTransferRequestElement.builder()
                .segmentNumber(fileTransfer.getSegmentNumber())
                .lastSegment(fileTransfer.isLastSegment())
                .transactionId(fileTransfer.getTransactionId())
                .build().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        session.getFileTransferManager().saveSegment(fileTransfer, responseElement.getOrderData());

        return fileTransfer;
    }
}
