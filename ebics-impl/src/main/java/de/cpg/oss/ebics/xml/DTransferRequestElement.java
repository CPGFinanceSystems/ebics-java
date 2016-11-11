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

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.MutableHeaderType;
import org.ebics.h004.StaticHeaderType;
import org.ebics.h004.TransactionPhaseType;

import java.io.IOException;
import java.math.BigInteger;

import static de.cpg.oss.ebics.xml.DefaultEbicsRootElement.generateName;

/**
 * The <code>DTransferRequestElement</code> is the common elements
 * for all ebics downloads.
 *
 * @author Hachani
 */
public class DTransferRequestElement extends TransferRequestElement {

    /**
     * Constructs a new <code>DTransferRequestElement</code> element.
     *
     * @param session       the current ebics session
     * @param type          the order type
     * @param segmentNumber the segment number
     * @param lastSegment   is it the last segment?
     * @param transactionId the transaction ID
     */
    public DTransferRequestElement(final EbicsSession session,
                                   final OrderType type,
                                   final int segmentNumber,
                                   final boolean lastSegment,
                                   final byte[] transactionId) {
        super(session, generateName(type), type, segmentNumber, lastSegment, transactionId);
    }

    @Override
    public EbicsRequest buildTransfer() throws IOException {
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

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();

        final EbicsRequest request = OBJECT_FACTORY.createEbicsRequest();
        request.setRevision(session.getConfiguration().getRevision());
        request.setVersion(session.getConfiguration().getVersion().name());
        request.setHeader(header);
        request.setBody(body);

        return request;
    }
}
