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

import de.cpg.oss.ebics.api.EbicsUser;
import org.w3.xmldsig.*;


/**
 * A representation of the SignedInfo element
 * performing signature for signed ebics requests
 *
 * @author hachani
 */
public class SignedInfoElement {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final byte[] digest;
    private final EbicsUser user;

    /**
     * Constructs a new <code>SignedInfo</code> element
     *
     * @param digest the digest value
     */
    public SignedInfoElement(final EbicsUser user, final byte[] digest) {
        if (digest == null) {
            throw new IllegalArgumentException("digest value must not be null");
        }

        this.user = user;
        this.digest = digest;
    }

    public SignatureType build() {
        final Transform transform = OBJECT_FACTORY.createTransform();
        transform.setAlgorithm(XmlUtils.CANONICALIZAION_METHOD);

        final DigestMethod digestMethod = OBJECT_FACTORY.createDigestMethod();
        digestMethod.setAlgorithm(XmlUtils.DIGEST_METHOD);

        final Transforms transforms = OBJECT_FACTORY.createTransforms();
        transforms.getTransforms().add(transform);

        final Reference reference = OBJECT_FACTORY.createReference();
        reference.setURI("#xpointer(" + XmlUtils.XPATH_SELECTOR + ")");
        reference.setTransforms(transforms);
        reference.setDigestMethod(digestMethod);
        reference.setDigestValue(digest);

        final SignatureMethod signatureMethod = OBJECT_FACTORY.createSignatureMethod();
        signatureMethod.setAlgorithm(XmlUtils.SIGNATURE_METHOD);

        final CanonicalizationMethod canonicalizationMethod = OBJECT_FACTORY.createCanonicalizationMethod();
        canonicalizationMethod.setAlgorithm(XmlUtils.CANONICALIZAION_METHOD);

        final SignedInfo signedInfo = OBJECT_FACTORY.createSignedInfo();
        signedInfo.setCanonicalizationMethod(canonicalizationMethod);
        signedInfo.setSignatureMethod(signatureMethod);
        signedInfo.getReferences().add(reference);

        final SignatureType signatureType = OBJECT_FACTORY.createSignatureType();
        signatureType.setSignedInfo(signedInfo);
        signatureType.setSignatureValue(OBJECT_FACTORY.createSignatureValue());

        return signatureType;
    }

    public String getName() {
        return "SignedInfo.xml";
    }
}
