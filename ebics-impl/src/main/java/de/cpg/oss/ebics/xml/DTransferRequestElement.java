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
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

/**
 * The <code>DTransferRequestElement</code> is the common elements
 * for all ebics downloads.
 *
 * @author Hachani
 */
public class DTransferRequestElement extends EbicsRequestElement {

    private final int segmentNumber;
    private final boolean lastSegment;
    private final byte[] transactionId;

    /**
     * Constructs a new <code>DTransferRequestElement</code> element.
     *
     * @param session       the current ebics session
     * @param segmentNumber the segment number
     * @param lastSegment   is it the last segment?
     * @param transactionId the transaction ID
     */
    public DTransferRequestElement(final EbicsSession session,
                                   final int segmentNumber,
                                   final boolean lastSegment,
                                   final byte[] transactionId) {
        super(session);
        this.segmentNumber = segmentNumber;
        this.lastSegment = lastSegment;
        this.transactionId = transactionId;
    }

    @Override
    public EbicsRequest buildEbicsRequest() throws EbicsException {
        return EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.TRANSFER, segmentNumber, lastSegment),
                        EbicsXmlFactory.staticHeader(session.getHostId(), transactionId)));
    }
}
