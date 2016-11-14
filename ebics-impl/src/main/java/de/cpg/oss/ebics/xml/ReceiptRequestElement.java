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
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.ObjectFactory;
import org.ebics.h004.TransactionPhaseType;


/**
 * The <code>ReceiptRequestElement</code> is the element containing the
 * receipt request to tell the server bank that all segments are received.
 *
 * @author Hachani
 */
public class ReceiptRequestElement {

    private final byte[] transactionId;

    protected final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    protected final EbicsSession session;


    /**
     * Construct a new <code>ReceiptRequestElement</code> element.
     *
     * @param session the current ebics session
     */
    public ReceiptRequestElement(final EbicsSession session,
                                 final byte[] transactionId) {
        this.session = session;
        this.transactionId = transactionId;
    }

    public EbicsRequest build() throws EbicsException {
        final SignedInfoElement signedInfo;

        final EbicsRequest.Body.TransferReceipt transferReceipt = OBJECT_FACTORY.createEbicsRequestBodyTransferReceipt();
        transferReceipt.setAuthenticate(true);
        transferReceipt.setReceiptCode(0);

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setTransferReceipt(transferReceipt);

        final EbicsRequest request = EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.RECEIPT),
                        EbicsXmlFactory.staticHeader(session.getHostId(), transactionId)),
                body);

        signedInfo = new SignedInfoElement(XmlUtil.digest(EbicsRequest.class, request));
        request.setAuthSignature(signedInfo.build());

        return request;
    }
}
