/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.FileTransferState;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ByteArrayContentFactory;
import de.cpg.oss.ebics.io.InputStreamContentFactory;
import de.cpg.oss.ebics.io.Joiner;
import de.cpg.oss.ebics.io.Splitter;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.utils.HttpUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.EbicsResponse;

import java.io.IOException;
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
    static void sendFile(final EbicsSession session, final byte[] content, final OrderType orderType) throws IOException, EbicsException {
        final Splitter splitter = new Splitter(content);
        final EbicsRequest request = UInitializationRequestElement.builder()
                .orderType(orderType)
                .userData(content)
                .splitter(splitter)
                .build().create(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, request);
        session.getTraceManager().trace(EbicsRequest.class, request, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final EbicsResponseElement response = EbicsResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        final EbicsResponse ebicsResponse = response.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsResponse, session.getUser());
        response.report(session.getMessageProvider());
        FileTransferState state = FileTransferState.builder()
                .numSegments(splitter.getNumSegments())
                .transactionId(response.getTransactionId())
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
                                              final FileTransferState fileTransferState) throws EbicsException, IOException {
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
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final EbicsResponseElement response = EbicsResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        final EbicsResponse ebicsResponse = response.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsResponse, session.getUser());
        response.report(session.getMessageProvider());

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
     * @throws IOException    communication error
     * @throws EbicsException server generated error
     */
    static void fetchFile(final EbicsSession session,
                          final OrderType orderType,
                          final LocalDate start,
                          final LocalDate end,
                          final OutputStream dest) throws IOException, EbicsException {
        final EbicsRequest request = DInitializationRequestElement.builder()
                .orderType(orderType)
                .startRange(start)
                .endRange(end)
                .build().create(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, request);
        session.getTraceManager().trace(EbicsRequest.class, request, session.getUser());
        XmlUtil.validate(xml);
        HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final DInitializationResponseElement responseElement = DInitializationResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        final EbicsResponse ebicsResponse = responseElement.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsResponse, session.getUser());
        responseElement.report(session.getMessageProvider());
        FileTransferState state = responseElement.getFileTransferState();
        final Joiner joiner = new Joiner(session.getUser());
        joiner.append(responseElement.getOrderData());
        while (state.hasNext()) {
            state = fetchFile(session, state.next(), joiner);
        }

        joiner.writeTo(dest, responseElement.getTransactionKey());
        final ReceiptRequestElement receipt = new ReceiptRequestElement(session, state.getTransactionId());
        final EbicsRequest ebicsRequest = receipt.build();
        final byte[] receiptXml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        XmlUtil.validate(receiptXml);
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(receiptXml),
                session.getMessageProvider());
        final ReceiptResponseElement receiptResponse = ReceiptResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        final EbicsResponse ebicsReceiptResponse = receiptResponse.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsReceiptResponse, session.getUser());
        receiptResponse.report(session.getMessageProvider());
    }

    /**
     * Fetches a given portion of a file.
     *
     * @param joiner the portions joiner
     * @throws IOException    communication error
     * @throws EbicsException server generated error
     */
    private static FileTransferState fetchFile(final EbicsSession session,
                                               final FileTransferState fileTransferState,
                                               final Joiner joiner) throws IOException, EbicsException {
        final EbicsRequest ebicsRequest = DTransferRequestElement.builder()
                .segmentNumber(fileTransferState.getSegmentNumber())
                .lastSegment(fileTransferState.isLastSegment())
                .transactionId(fileTransferState.getTransactionId())
                .build().create(session);
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final EbicsResponseElement responseElement = EbicsResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        final EbicsResponse ebicsResponse = responseElement.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsResponse, session.getUser());
        responseElement.report(session.getMessageProvider());
        joiner.append(responseElement.getOrderData());

        return fileTransferState;
    }
}
