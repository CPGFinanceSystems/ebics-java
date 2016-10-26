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

package org.kopi.ebics.xml;

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.schema.h004.DataTransferRequestType;
import org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData;
import org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest;
import org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body;
import org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header;
import org.kopi.ebics.schema.h004.MutableHeaderType;
import org.kopi.ebics.schema.h004.MutableHeaderType.SegmentNumber;
import org.kopi.ebics.schema.h004.StaticHeaderType;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;

/**
 * The <code>UTransferRequestElement</code> is the root element
 * for all ebics upload transfers.
 *
 * @author Hachani
 */
public class UTransferRequestElement extends TransferRequestElement {

    /**
     * Constructs a new <code>UTransferRequestElement</code> for ebics upload transfer.
     *
     * @param session       the current ebics session
     * @param orderType     the upload order type
     * @param segmentNumber the segment number
     * @param lastSegment   i it the last segment?
     * @param transactionId the transaction ID
     * @param content       the content factory
     */
    public UTransferRequestElement(final EbicsSession session,
                                   final OrderType orderType,
                                   final int segmentNumber,
                                   final boolean lastSegment,
                                   final byte[] transactionId,
                                   final ContentFactory content) {
        super(session, generateName(orderType), orderType, segmentNumber, lastSegment, transactionId);
        this.content = content;
    }

    @Override
    public void buildTransfer() throws EbicsException {
        final EbicsRequest request;
        final Header header;
        final Body body;
        final MutableHeaderType mutable;
        final SegmentNumber segmentNumber;
        final StaticHeaderType xstatic;
        final OrderData orderData;
        final DataTransferRequestType dataTransfer;

        segmentNumber = EbicsXmlFactory.createSegmentNumber(this.segmentNumber, lastSegment);
        mutable = EbicsXmlFactory.createMutableHeaderType("Transfer", segmentNumber);
        xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
        header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
        orderData = EbicsXmlFactory.createEbicsRequestOrderData(IOUtils.getFactoryContent(content));
        dataTransfer = EbicsXmlFactory.createDataTransferRequestType(orderData);
        body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
        request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
                session.getConfiguration().getVersion(),
                header,
                body);
        document = EbicsXmlFactory.createEbicsRequestDocument(request);
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final ContentFactory content;
    private static final long serialVersionUID = 8465397978597444978L;
}
