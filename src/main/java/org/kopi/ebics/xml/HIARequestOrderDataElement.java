/* Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
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

import org.ebics.h004.AuthenticationPubKeyInfoType;
import org.ebics.h004.EncryptionPubKeyInfoType;
import org.ebics.h004.HIARequestOrderDataType;
import org.ebics.h004.ObjectFactory;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.session.EbicsSession;
import org.w3.xmldsig.RSAKeyValue;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;


/**
 * The <code>HIARequestOrderDataElement</code> is the element that contains
 * X002 and E002 keys information needed for a HIA request in order to send
 * the authentication and encryption user keys to the bank server.
 *
 * @author hachani
 */
public class HIARequestOrderDataElement {

    private final EbicsSession session;

    private static final org.w3.xmldsig.ObjectFactory W3C_OBJECT_FACTORY = new org.w3.xmldsig.ObjectFactory();
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Constructs a new HIA Request Order Data element
     *
     * @param session the current ebics session
     */
    public HIARequestOrderDataElement(final EbicsSession session) {
        this.session = session;
    }

    public HIARequestOrderDataType build() throws EbicsException {
        final org.ebics.h004.PubKeyValueType encryptionPubKeyValue = OBJECT_FACTORY.createPubKeyValueType();
        encryptionPubKeyValue.setRSAKeyValue(rsaKeyValue(session.getUser().getE002Key().getPublic()));
        encryptionPubKeyValue.setTimeStamp(LocalDateTime.now()); //TODO: date time of key creation

        final EncryptionPubKeyInfoType encryptionPubKeyInfo = OBJECT_FACTORY.createEncryptionPubKeyInfoType();
        encryptionPubKeyInfo.setEncryptionVersion(session.getConfiguration().getEncryptionVersion());
        encryptionPubKeyInfo.setPubKeyValue(encryptionPubKeyValue);

        final org.ebics.h004.PubKeyValueType authPubKeyValue = OBJECT_FACTORY.createPubKeyValueType();
        authPubKeyValue.setRSAKeyValue(rsaKeyValue(session.getUser().getX002Key().getPublic()));
        authPubKeyValue.setTimeStamp(LocalDateTime.now()); //TODO: date time of key creation

        final AuthenticationPubKeyInfoType authenticationPubKeyInfo = OBJECT_FACTORY.createAuthenticationPubKeyInfoType();
        authenticationPubKeyInfo.setAuthenticationVersion(session.getConfiguration().getAuthenticationVersion());
        authenticationPubKeyInfo.setPubKeyValue(authPubKeyValue);

        final HIARequestOrderDataType request = OBJECT_FACTORY.createHIARequestOrderDataType();
        request.setAuthenticationPubKeyInfo(authenticationPubKeyInfo);
        request.setEncryptionPubKeyInfo(encryptionPubKeyInfo);
        request.setPartnerID(session.getUser().getPartner().getPartnerId());
        request.setUserID(session.getUser().getUserId());

        return request;
    }

    public String getName() {
        return "HIARequestOrderData.xml";
    }

    static RSAKeyValue rsaKeyValue(final PublicKey publicKey) {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        final RSAKeyValue rsaKeyValue = W3C_OBJECT_FACTORY.createRSAKeyValue();

        rsaKeyValue.setExponent(rsaPublicKey.getPublicExponent().toByteArray());
        rsaKeyValue.setModulus(rsaPublicKey.getModulus().toByteArray());

        return rsaKeyValue;
    }
}
