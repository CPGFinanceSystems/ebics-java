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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h004.AuthenticationPubKeyInfoType;
import org.kopi.ebics.schema.h004.EncryptionPubKeyInfoType;
import org.kopi.ebics.schema.h004.HIARequestOrderDataType;
import org.kopi.ebics.schema.h004.PubKeyValueType;
import org.kopi.ebics.schema.xmldsig.RSAKeyValueType;
import org.kopi.ebics.session.EbicsSession;

import java.util.Calendar;


/**
 * The <code>HIARequestOrderDataElement</code> is the element that contains
 * X002 and E002 keys information needed for a HIA request in order to send
 * the authentication and encryption user keys to the bank server.
 *
 * @author hachani
 */
public class HIARequestOrderDataElement extends DefaultEbicsRootElement {

    /**
     * Constructs a new HIA Request Order Data element
     *
     * @param session the current ebics session
     */
    public HIARequestOrderDataElement(final EbicsSession session) {
        super(session);
    }

    @Override
    public void build() throws EbicsException {
        final HIARequestOrderDataType request;
        final AuthenticationPubKeyInfoType authenticationPubKeyInfo;
        final EncryptionPubKeyInfoType encryptionPubKeyInfo;
        final PubKeyValueType encryptionPubKeyValue;
        final RSAKeyValueType encryptionRsaKeyValue;
        final PubKeyValueType authPubKeyValue;
        final RSAKeyValueType AuthRsaKeyValue;

        encryptionRsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(session.getUser().getE002PublicKey().getPublicExponent().toByteArray(),
                session.getUser().getE002PublicKey().getModulus().toByteArray());
        encryptionPubKeyValue = EbicsXmlFactory.createH004PubKeyValueType(encryptionRsaKeyValue, Calendar.getInstance());
        encryptionPubKeyInfo = EbicsXmlFactory.createEncryptionPubKeyInfoType(session.getConfiguration().getEncryptionVersion(),
                encryptionPubKeyValue);
        AuthRsaKeyValue = EbicsXmlFactory.createRSAKeyValueType(session.getUser().getX002PublicKey().getPublicExponent().toByteArray(),
                session.getUser().getX002PublicKey().getModulus().toByteArray());
        authPubKeyValue = EbicsXmlFactory.createH004PubKeyValueType(AuthRsaKeyValue, Calendar.getInstance());
        authenticationPubKeyInfo = EbicsXmlFactory.createAuthenticationPubKeyInfoType(session.getConfiguration().getAuthenticationVersion(),
                authPubKeyValue);
        request = EbicsXmlFactory.createHIARequestOrderDataType(authenticationPubKeyInfo,
                encryptionPubKeyInfo,
                session.getUser().getPartner().getPartnerId(),
                session.getUser().getUserId());
        document = EbicsXmlFactory.createHIARequestOrderDataDocument(request);
    }

    @Override
    public String getName() {
        return "HIARequestOrderData.xml";
    }

    @Override
    public byte[] toByteArray() {
        addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
        setSaveSuggestedPrefixes("http://www.ebics.org/S001", "");

        return super.toByteArray();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private static final long serialVersionUID = -7333250823464659004L;
}
