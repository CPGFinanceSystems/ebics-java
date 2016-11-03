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

import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.s001.SignaturePubKeyOrderData;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.utils.Utils;

/**
 * The INI request XML element. This root element is to be sent
 * to the ebics server to initiate the signature certificate.
 *
 * @author hachani
 */
public class INIRequestElement {

    private final EbicsSession session;

    /**
     * Constructs a new INI request element.
     *
     * @param session the ebics session.
     */
    public INIRequestElement(final EbicsSession session) {
        this.session = session;
    }

    public String getName() {
        return "INIRequest.xml";
    }

    public EbicsUnsecuredRequest build() throws EbicsException {
        final SignaturePubKeyOrderDataElement signaturePubKey = new SignaturePubKeyOrderDataElement(session);
        final SignaturePubKeyOrderData signaturePubKeyOrderData = signaturePubKey.build();
        final UnsecuredRequestElement unsecuredRequest = new UnsecuredRequestElement(session,
                OrderType.INI,
                Utils.zip(XmlUtils.prettyPrint(SignaturePubKeyOrderData.class, signaturePubKeyOrderData)));
        return unsecuredRequest.build();
    }

    /*
    public byte[] toByteArray() {
        setSaveSuggestedPrefixes("urn:org:ebics:H004", "");

        return unsecuredRequest.toByteArray();
    }

    public void validate() throws EbicsException {
        unsecuredRequest.validate();
    }
    */
}
