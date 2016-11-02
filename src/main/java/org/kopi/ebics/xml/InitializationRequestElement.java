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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h004.EbicsRequestDocument;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.utils.Utils;

import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


/**
 * The <code>InitializationRequestElement</code> is the root element for
 * ebics uploads and downloads requests. The response of this element is
 * then used either to upload or download files from the ebics server.
 *
 * @author Hachani
 */
public abstract class InitializationRequestElement extends DefaultEbicsRootElement {

    /**
     * Construct a new <code>InitializationRequestElement</code> root element.
     *
     * @param session the current ebics session.
     * @param type    the initialization type (UPLOAD, DOWNLOAD).
     * @param name    the element name.
     * @throws EbicsException
     */
    public InitializationRequestElement(final EbicsSession session,
                                        final OrderType type,
                                        final String name)
            throws EbicsException {
        super(session);
        this.type = type;
        this.name = name;
        nonce = Utils.generateNonce();
    }

    @Override
    public void build() throws EbicsException {
        final SignedInfo signedInfo;

        buildInitialization();
        signedInfo = new SignedInfo(session.getUser(), getDigest());
        signedInfo.build();
        ((EbicsRequestDocument) document).getEbicsRequest().setAuthSignature(signedInfo.getSignatureType());
        ((EbicsRequestDocument) document).getEbicsRequest().getAuthSignature().setSignatureValue(EbicsXmlFactory.createSignatureValueType(signedInfo.sign(toByteArray())));
    }

    @Override
    public String getName() {
        return name + ".xml";
    }

    @Override
    public byte[] toByteArray() {
        setSaveSuggestedPrefixes("urn:org:ebics:H004", "");

        return super.toByteArray();
    }

    /**
     * Returns the digest value of the authenticated XML portions.
     *
     * @return the digest value.
     * @throws EbicsException Failed to retrieve the digest value.
     */
    public byte[] getDigest() throws EbicsException {
        addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");

        try {
            return MessageDigest.getInstance("SHA-256", "BC").digest(Utils.canonize(toByteArray()));
        } catch (final NoSuchAlgorithmException e) {
            throw new EbicsException(e.getMessage());
        } catch (final NoSuchProviderException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    /**
     * Returns the element type.
     *
     * @return the element type.
     */
    public String getType() {
        return type.name();
    }

    /**
     * Decodes an hexadecimal input.
     *
     * @param hex the hexadecimal input
     * @return the decoded hexadecimal value
     * @throws EbicsException
     */
    protected byte[] decodeHex(final byte[] hex) throws EbicsException {
        if (hex == null) {
            throw new EbicsException("Bank digest is empty, HPB request must be performed before");
        }
        return hex;
    }

    /**
     * Generates the upload transaction key
     *
     * @return the transaction key
     */
    protected byte[] generateTransactionKey() throws EbicsException {
        try {
            final Cipher cipher;

            cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, session.getBankE002Key());

            return cipher.doFinal(nonce);
        } catch (final Exception e) {
            throw new EbicsException(e.getMessage(), e);
        }
    }

    /**
     * Builds the initialization request according to the
     * element type.
     *
     * @throws EbicsException build fails
     */
    public abstract void buildInitialization() throws EbicsException;

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final String name;
    protected OrderType type;
    protected byte[] nonce;
    private static final long serialVersionUID = 8983807819242699280L;
}
