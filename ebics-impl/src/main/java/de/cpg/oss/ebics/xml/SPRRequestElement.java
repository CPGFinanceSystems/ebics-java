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
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import javax.crypto.spec.SecretKeySpec;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

/**
 * The <code>SPRRequestElement</code> is the request element
 * for revoking a subscriber
 *
 * @author Hachani
 */
public class SPRRequestElement extends InitializationRequestElement {

    private final SecretKeySpec keySpec;
    private final byte[] nonce;

    /**
     * Constructs a new SPR request element.
     *
     * @param session the current ebic session.
     */
    public SPRRequestElement(final EbicsSession session) throws EbicsException {
        super(session);
        this.nonce = CryptoUtil.generateNonce();
        this.keySpec = new SecretKeySpec(nonce, "AES");
    }

    @Override
    public EbicsRequest buildEbicsRequest() throws EbicsException {
        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(
                                session,
                                nonce,
                                0,
                                orderDetails(
                                        OrderAttributeType.UZHNN,
                                        OrderType.SPR))),
                body(dataTransferRequest(
                        session,
                        " ".getBytes(),
                        keySpec,
                        generateTransactionKey(nonce))));
    }
}
