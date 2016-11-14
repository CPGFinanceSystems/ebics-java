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
import org.ebics.h004.ObjectFactory;

import java.io.IOException;


/**
 * The <code>TransferRequestElement</code> is the common root element
 * for all ebics transfer for the bank server.
 *
 * @author Hachani
 */
public abstract class TransferRequestElement {

    protected static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    protected final EbicsSession session;

    final int segmentNumber;
    final boolean lastSegment;
    final byte[] transactionId;

    /**
     * Constructs a new <code>TransferRequestElement</code> element.
     *
     * @param session       the current ebics session
     * @param segmentNumber the segment number to be sent
     * @param lastSegment   is it the last segment?
     * @param transactionId the transaction ID
     */
    public TransferRequestElement(final EbicsSession session,
                                  final int segmentNumber,
                                  final boolean lastSegment,
                                  final byte[] transactionId) {
        this.session = session;
        this.segmentNumber = segmentNumber;
        this.lastSegment = lastSegment;
        this.transactionId = transactionId;
    }

    public EbicsRequest build() throws EbicsException {
        final EbicsRequest request;
        try {
            request = buildTransfer();
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        final SignedInfoElement signedInfo = new SignedInfoElement(XmlUtils.digest(EbicsRequest.class, request));
        request.setAuthSignature(signedInfo.build());

        final byte[] signature = XmlUtils.sign(EbicsRequest.class, request, session.getUser());
        request.getAuthSignature().getSignatureValue().setValue(signature);

        return request;
    }

    /**
     * Builds the transfer request.
     */
    public abstract EbicsRequest buildTransfer() throws IOException;
}
