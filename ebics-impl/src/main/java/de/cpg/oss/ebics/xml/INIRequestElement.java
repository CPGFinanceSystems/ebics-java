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
import de.cpg.oss.ebics.utils.ZipUtil;
import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.s001.SignaturePubKeyOrderData;

/**
 * The INI request XML element. This root element is to be sent
 * to the ebics server to initiate the signature util.
 *
 * @author hachani
 */
public class INIRequestElement {

    private final EbicsSession session;

    /**
     * Constructs a new INI request element.
     *
     * @param session the ebics session.
     */
    public INIRequestElement(final EbicsSession session) {
        this.session = session;
    }

    public EbicsUnsecuredRequest build() throws EbicsException {
        final SignaturePubKeyOrderDataElement signaturePubKey = new SignaturePubKeyOrderDataElement(session);
        final SignaturePubKeyOrderData signaturePubKeyOrderData = signaturePubKey.build();
        final EbicsUnsecuredRequestElement unsecuredRequest = new EbicsUnsecuredRequestElement(session,
                OrderType.INI,
                ZipUtil.compress(XmlUtils.prettyPrint(SignaturePubKeyOrderData.class, signaturePubKeyOrderData)));
        return unsecuredRequest.build();
    }
}
