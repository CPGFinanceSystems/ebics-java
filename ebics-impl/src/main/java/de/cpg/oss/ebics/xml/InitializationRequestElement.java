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
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.ObjectFactory;

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
public abstract class InitializationRequestElement {

    protected static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    protected final EbicsSession session;
    protected final OrderType type;
    final byte[] nonce;

    /**
     * Construct a new <code>InitializationRequestElement</code> root element.
     *
     * @param session the current ebics session.
     * @param type    the initialization type (UPLOAD, DOWNLOAD).
     */
    public InitializationRequestElement(final EbicsSession session,
                                        final OrderType type) {
        this.session = session;
        this.type = type;
        this.nonce = CryptoUtil.generateNonce();
    }

    public EbicsRequest build() throws EbicsException {
        final EbicsRequest ebicsRequest = buildInitialization();

        final SignedInfoElement signedInfo = new SignedInfoElement(XmlUtils.digest(EbicsRequest.class, ebicsRequest));
        ebicsRequest.setAuthSignature(signedInfo.build());

        final byte[] signature = XmlUtils.sign(EbicsRequest.class, ebicsRequest, session.getUser());
        ebicsRequest.getAuthSignature().getSignatureValue().setValue(signature);

        return ebicsRequest;
    }

    /**
     * Generates the upload transaction key
     *
     * @return the transaction key
     */
    byte[] generateTransactionKey() throws EbicsException {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, session.getBankEncryptionKey());
            final BigInteger data = new BigInteger(nonce);
            log.info("Data bits: {}", data.bitLength());
            log.info("Modulus bits: {}", session.getBankEncryptionKey().getModulus().bitLength());
            log.info("Compare: {}", data.compareTo(session.getBankEncryptionKey().getModulus()));
            return cipher.doFinal(nonce);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the initialization request according to the
     * element type.
     *
     * @throws EbicsException build fails
     */
    protected abstract EbicsRequest buildInitialization() throws EbicsException;
}
