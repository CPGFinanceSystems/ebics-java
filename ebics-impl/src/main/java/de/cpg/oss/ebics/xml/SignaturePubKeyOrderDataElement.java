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
import org.ebics.s001.ObjectFactory;
import org.ebics.s001.PubKeyValueType;
import org.ebics.s001.SignaturePubKeyInfo;
import org.ebics.s001.SignaturePubKeyOrderData;


/**
 * The <code>SignaturePubKeyOrderDataElement</code> is the order data
 * component for the INI request.
 *
 * @author hachani
 */
public class SignaturePubKeyOrderDataElement {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final EbicsSession session;

    /**
     * Creates a new Signature Public Key Order Data element.
     *
     * @param session the current ebics session
     */
    public SignaturePubKeyOrderDataElement(final EbicsSession session) {
        this.session = session;
    }

    public SignaturePubKeyOrderData build() throws EbicsException {
        final PubKeyValueType pubKeyValue = OBJECT_FACTORY.createPubKeyValueType();
        pubKeyValue.setRSAKeyValue(HIARequestOrderDataElement.rsaKeyValue(session.getUser().getSignatureKey().getPublicKey()));
        pubKeyValue.setTimeStamp(session.getUser().getSignatureKey().getCreationTime());

        final SignaturePubKeyInfo signaturePubKeyInfo = OBJECT_FACTORY.createSignaturePubKeyInfo();
        signaturePubKeyInfo.setPubKeyValue(pubKeyValue);
        signaturePubKeyInfo.setSignatureVersion(session.getConfiguration().getSignatureVersion().name());

        final SignaturePubKeyOrderData signaturePubKeyOrderData = OBJECT_FACTORY.createSignaturePubKeyOrderData();
        signaturePubKeyOrderData.setSignaturePubKeyInfo(signaturePubKeyInfo);
        signaturePubKeyOrderData.setPartnerID(session.getPartner().getId());
        signaturePubKeyOrderData.setUserID(session.getUser().getId());

        return signaturePubKeyOrderData;
    }

    public String getName() {
        return "SignaturePubKeyOrderData.xml";
    }
}
