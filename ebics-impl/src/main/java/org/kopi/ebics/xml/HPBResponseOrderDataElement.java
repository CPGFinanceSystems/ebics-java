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

import org.ebics.h004.HPBResponseOrderDataType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.utils.KeyUtil;
import org.w3.xmldsig.RSAKeyValue;

import java.security.interfaces.RSAPublicKey;

/**
 * The <code>HPBResponseOrderDataElement</code> contains the public bank
 * keys in encrypted mode. The user should decrypt with his encryption
 * key to have the bank public keys.
 *
 * @author hachani
 */
public class HPBResponseOrderDataElement {

    private final ContentFactory contentFactory;

    private HPBResponseOrderDataType response;

    /**
     * Creates a new <code>HPBResponseOrderDataElement</code> from a given
     * content factory.
     *
     * @param factory the content factory.
     */
    public HPBResponseOrderDataElement(final ContentFactory factory) {
        this.contentFactory = factory;
    }

    public RSAPublicKey getBankX002PublicKeyData() {
        final RSAKeyValue rsaKey = response.getAuthenticationPubKeyInfo().getPubKeyValue().getRSAKeyValue();
        return KeyUtil.getPublicKey(rsaKey.getModulus(), rsaKey.getExponent());
    }

    public RSAPublicKey getBankE002PublicKeyData() {
        final RSAKeyValue rsaKey = response.getEncryptionPubKeyInfo().getPubKeyValue().getRSAKeyValue();
        return KeyUtil.getPublicKey(rsaKey.getModulus(), rsaKey.getExponent());
    }

    public HPBResponseOrderDataType build() throws EbicsException {
        response = XmlUtils.parse(HPBResponseOrderDataType.class, contentFactory.getContent());
        return response;
    }

    public String getName() {
        return "HPBData.xml";
    }
}
