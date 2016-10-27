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

import lombok.Value;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.schema.h004.HPBResponseOrderDataDocument;
import org.kopi.ebics.schema.h004.HPBResponseOrderDataType;
import org.kopi.ebics.schema.h004.PubKeyValueType;

import java.io.Serializable;
import java.util.Optional;

/**
 * The <code>HPBResponseOrderDataElement</code> contains the public bank
 * keys in encrypted mode. The user should decrypt with his encryption
 * key to have the bank public keys.
 *
 * @author hachani
 */
public class HPBResponseOrderDataElement extends DefaultResponseElement {

    @Value
    public static class PublicKeyData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final byte[] modulus;
        private final byte[] exponent;
    }

    /**
     * Creates a new <code>HPBResponseOrderDataElement</code> from a given
     * content factory.
     *
     * @param factory the content factory.
     */
    public HPBResponseOrderDataElement(final ContentFactory factory) {
        super(factory, "HPBData");
    }

    /**
     * Returns the authentication bank certificate.
     *
     * @return the authentication bank certificate.
     */
    public Optional<byte[]> getBankX002Certificate() {
        return Optional.ofNullable(response.getAuthenticationPubKeyInfo().getX509Data())
                .map(data -> data.getX509CertificateArray(0));
    }

    public Optional<PublicKeyData> getBankX002PublicKeyData() {
        return Optional.ofNullable(response.getAuthenticationPubKeyInfo().getPubKeyValue())
                .map(PubKeyValueType::getRSAKeyValue)
                .map(keyValue -> new PublicKeyData(keyValue.getModulus(), keyValue.getExponent()));
    }

    /**
     * Returns the encryption bank certificate.
     *
     * @return the encryption bank certificate.
     */
    public Optional<byte[]> getBankE002Certificate() {
        return Optional.ofNullable(response.getEncryptionPubKeyInfo().getX509Data())
                .map(data -> data.getX509CertificateArray(0));
    }

    public Optional<PublicKeyData> getBankE002PublicKeyData() {
        return Optional.ofNullable(response.getEncryptionPubKeyInfo().getPubKeyValue())
                .map(PubKeyValueType::getRSAKeyValue)
                .map(keyValue -> new PublicKeyData(keyValue.getModulus(), keyValue.getExponent()));
    }

    @Override
    public void build() throws EbicsException {
        parse(factory);
        response = ((HPBResponseOrderDataDocument) document).getHPBResponseOrderData();
    }

    @Override
    public String getName() {
        return "HPBData.xml";
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private HPBResponseOrderDataType response;
    private static final long serialVersionUID = -1305363936881364049L;
}
