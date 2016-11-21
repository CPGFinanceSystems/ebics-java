package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransferState;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.Joiner;
import de.cpg.oss.ebics.io.Splitter;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    /**
     * Initiates a file transfer to the bank.
     *
     * @param content   The bytes you want to send.
     * @param orderType As which order type
     */
    static void sendFile(final EbicsSession session, final InputStream content, final OrderType orderType) throws EbicsException {
        final Splitter splitter = new Splitter(content);
        final EbicsRequest request;
        try {
            request = UInitializationRequestElement.builder()
                    .orderType(orderType)
                    .userData(IOUtil.read(content))
                    .splitter(splitter)
                    .build().create(session);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, request);
        FileTransferState state = FileTransferState.builder()
                .numSegments(splitter.getNumSegments())
                .transactionId(responseElement.getTransactionId())
                .build();

        while (state.hasNext()) {
            state = sendFile(session,
                    splitter,
                    state.next());
        }
    }

    /**
     * Sends a segment to the ebics bank server.
     */
    private static FileTransferState sendFile(final EbicsSession session,
                                              final Splitter splitter,
                                              final FileTransferState fileTransferState) throws EbicsException {
        log.info(session.getMessageProvider().getString(
                "upload.segment",
                Constants.APPLICATION_BUNDLE_NAME,
                fileTransferState.getSegmentNumber()));
        final EbicsRequest ebicsRequest = UTransferRequestElement.builder()
                .segmentNumber(fileTransferState.getSegmentNumber())
                .lastSegment(fileTransferState.isLastSegment())
                .transactionId(fileTransferState.getTransactionId())
                .contentFactory(splitter.getContent(fileTransferState.getSegmentNumber()))
                .build().create(session);

        ClientUtil.requestExchange(session, ebicsRequest);

        return fileTransferState;
    }

    /**
     * Fetches a file of the given order type from the bank.
     * You may give an optional start and end date.
     * This type of transfer will run until everything is processed.
     * No transaction recovery is possible.
     *
     * @param orderType type of file to fetch
     * @param start     optional begin of fetch term
     * @param end       optional end of fetch term
     * @param dest      where to put the data
     * @throws EbicsException server generated error
     */
    static void fetchFile(final EbicsSession session,
                          final OrderType orderType,
                          final LocalDate start,
                          final LocalDate end,
                          final OutputStream dest) throws EbicsException {
        final Joiner joiner = new Joiner(session.getUser());
        final EbicsRequest request = DInitializationRequestElement.builder()
                .orderType(orderType)
                .startRange(start)
                .endRange(end)
                .build().create(session);

        final DInitializationResponseElement responseElement = ClientUtil.requestExchange(session, request,
                DInitializationResponseElement::parse);

        FileTransferState state = responseElement.getFileTransferState();
        joiner.append(responseElement.getOrderData());
        while (state.hasNext()) {
            state = fetchFile(session, state.next(), joiner);
        }

        joiner.writeTo(dest, responseElement.getTransactionKey());

        final EbicsRequest ebicsRequest = new ReceiptRequestElement(state.getTransactionId()).create(session);
        ClientUtil.requestExchange(session, ebicsRequest, ReceiptResponseElement::parse);
    }

    /**
     * Fetches a given portion of a file.
     *
     * @param joiner the portions joiner
     * @throws EbicsException server generated error
     */
    private static FileTransferState fetchFile(final EbicsSession session,
                                               final FileTransferState fileTransferState,
                                               final Joiner joiner) throws EbicsException {
        final EbicsRequest ebicsRequest = DTransferRequestElement.builder()
                .segmentNumber(fileTransferState.getSegmentNumber())
                .lastSegment(fileTransferState.isLastSegment())
                .transactionId(fileTransferState.getTransactionId())
                .build().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        joiner.append(responseElement.getOrderData());

        return fileTransferState;
    }
}
