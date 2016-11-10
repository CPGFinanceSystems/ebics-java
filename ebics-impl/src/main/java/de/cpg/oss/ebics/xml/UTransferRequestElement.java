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

package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.io.IOUtils;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import org.ebics.h004.*;

import java.math.BigInteger;

import static de.cpg.oss.ebics.xml.DefaultEbicsRootElement.generateName;

/**
 * The <code>UTransferRequestElement</code> is the root element
 * for all ebics upload transfers.
 *
 * @author Hachani
 */
public class UTransferRequestElement extends TransferRequestElement {

    private final ContentFactory contentFactory;

    /**
     * Constructs a new <code>UTransferRequestElement</code> for ebics upload transfer.
     *
     * @param session        the current ebics session
     * @param orderType      the upload order type
     * @param segmentNumber  the segment number
     * @param lastSegment    i it the last segment?
     * @param transactionId  the transaction ID
     * @param contentFactory the contentFactory factory
     */
    public UTransferRequestElement(final EbicsSession session,
                                   final OrderType orderType,
                                   final int segmentNumber,
                                   final boolean lastSegment,
                                   final byte[] transactionId,
                                   final ContentFactory contentFactory) {
        super(session, generateName(orderType), orderType, segmentNumber, lastSegment, transactionId);
        this.contentFactory = contentFactory;
    }

    @Override
    public EbicsRequest buildTransfer() throws EbicsException {
        final MutableHeaderType.SegmentNumber segmentNumber = OBJECT_FACTORY.createMutableHeaderTypeSegmentNumber();
        segmentNumber.setValue(BigInteger.valueOf(this.segmentNumber));
        segmentNumber.setLastSegment(lastSegment);

        final MutableHeaderType mutable = OBJECT_FACTORY.createMutableHeaderType();
        mutable.setTransactionPhase(TransactionPhaseType.TRANSFER);
        mutable.setSegmentNumber(OBJECT_FACTORY.createMutableHeaderTypeSegmentNumber(segmentNumber));

        final StaticHeaderType xstatic = OBJECT_FACTORY.createStaticHeaderType();
        xstatic.setHostID(session.getHostId());
        xstatic.setTransactionID(transactionId);

        final EbicsRequest.Header header = OBJECT_FACTORY.createEbicsRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);

        final DataTransferRequestType.OrderData orderData = OBJECT_FACTORY.createDataTransferRequestTypeOrderData();
        orderData.setValue(IOUtils.read(contentFactory.getContent()));

        final DataTransferRequestType dataTransfer = OBJECT_FACTORY.createDataTransferRequestType();
        dataTransfer.setOrderData(orderData);

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setDataTransfer(dataTransfer);

        final EbicsRequest request = OBJECT_FACTORY.createEbicsRequest();
        request.setRevision(session.getConfiguration().getRevision());
        request.setVersion(session.getConfiguration().getVersion());
        request.setHeader(header);
        request.setBody(body);

        return request;
    }
}
