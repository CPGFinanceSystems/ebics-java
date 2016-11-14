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
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.math.BigInteger;


/**
 * The <code>InitializationRequestElement</code> is the root element for
 * ebics uploads and downloads requests. The response of this element is
 * then used either to upload or download files from the ebics server.
 *
 * @author Hachani
 */
@Slf4j
public abstract class InitializationRequestElement extends EbicsRequestElement {

    public InitializationRequestElement(final EbicsSession session) {
        super(session);
    }

    byte[] generateTransactionKey(final byte[] nonce) throws EbicsException {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, session.getBankEncryptionKey());
            final BigInteger data = new BigInteger(nonce);
            log.debug("Data bits: {}", data.bitLength());
            log.debug("Modulus bits: {}", session.getBankEncryptionKey().getModulus().bitLength());
            log.debug("Compare: {}", data.compareTo(session.getBankEncryptionKey().getModulus()));
            return cipher.doFinal(nonce);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
