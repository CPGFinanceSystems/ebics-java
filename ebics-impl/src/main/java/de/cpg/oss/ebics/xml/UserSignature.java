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

import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import org.ebics.s001.ObjectFactory;
import org.ebics.s001.OrderSignatureData;
import org.ebics.s001.UserSignatureDataSigBookType;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * A root EBICS element representing the user signature
 * element. The user data is signed with the user signature
 * key sent in the INI request to the EBICS bank server
 *
 * @author hachani
 */
public class UserSignature {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final EbicsUser user;
    private final String signatureVersion;
    private final byte[] toSign;
    private final String name;

    /**
     * Constructs a new <code>UserSignature</code> element for
     * an Ebics user and a data to sign
     *
     * @param user             the ebics user
     * @param signatureVersion the signature version
     * @param toSign           the data to be signed
     */
    public UserSignature(final EbicsUser user,
                         final String name,
                         final String signatureVersion,
                         final byte[] toSign) {
        this.user = user;
        this.toSign = toSign;
        this.name = name;
        this.signatureVersion = signatureVersion;
    }

    public JAXBElement<UserSignatureDataSigBookType> build() throws EbicsException {
        final byte[] signature;

        try {
            signature = CryptoUtil.sign(toSign, user.getA005Key().getPrivate());
        } catch (final IOException | GeneralSecurityException e) {
            throw new EbicsException(e.getMessage(), e);
        }

        final OrderSignatureData orderSignatureData = OBJECT_FACTORY.createOrderSignatureData();
        orderSignatureData.setSignatureVersion(signatureVersion);
        orderSignatureData.setPartnerID(user.getPartner().getPartnerId());
        orderSignatureData.setUserID(user.getUserId());
        orderSignatureData.setSignatureValue(signature);

        final UserSignatureDataSigBookType userSignatureData = OBJECT_FACTORY.createUserSignatureDataSigBookType();
        userSignatureData.getOrderSignaturesAndOrderSignatureDatas().add(orderSignatureData);

        return OBJECT_FACTORY.createUserSignatureData(userSignatureData);
    }

    public String getName() {
        return name + ".xml";
    }
}
