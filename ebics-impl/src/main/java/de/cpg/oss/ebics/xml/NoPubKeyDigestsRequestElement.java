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
import org.ebics.h004.*;

import java.time.LocalDateTime;

/**
 * The <code>NoPubKeyDigestsRequestElement</code> is the root element
 * for a HPB ebics server request.
 *
 * @author hachani
 */
public class NoPubKeyDigestsRequestElement {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final EbicsSession session;

    /**
     * Construct a new No Public Key Digests Request element.
     *
     * @param session the current ebics session.
     */
    public NoPubKeyDigestsRequestElement(final EbicsSession session) {
        this.session = session;
    }

    public EbicsNoPubKeyDigestsRequest build() throws EbicsException {
        final OrderDetailsType orderDetails = OBJECT_FACTORY.createNoPubKeyDigestsReqOrderDetailsType();
        orderDetails.setOrderAttribute(OrderAttributeType.DZHNN.name());
        orderDetails.setOrderType(OrderType.HPB.name());

        final NoPubKeyDigestsRequestStaticHeaderType xstatic = OBJECT_FACTORY.createNoPubKeyDigestsRequestStaticHeaderType();
        xstatic.setHostID(session.getHostId());
        xstatic.setNonce(CryptoUtil.generateNonce());
        xstatic.setTimestamp(LocalDateTime.now());
        xstatic.setPartnerID(session.getPartner().getPartnerId());
        xstatic.setUserID(session.getUser().getUserId());
        xstatic.setProduct(EbicsXmlFactory.unsecuredProduct(session.getProduct()));
        xstatic.setOrderDetails(orderDetails);
        xstatic.setSecurityMedium(session.getUser().getSecurityMedium());

        final EmptyMutableHeaderType mutable = OBJECT_FACTORY.createEmptyMutableHeaderType();

        final EbicsNoPubKeyDigestsRequest.Header header = OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);

        final EbicsNoPubKeyDigestsRequest.Body body = OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequestBody();

        final EbicsNoPubKeyDigestsRequest request = OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequest();
        request.setRevision(session.getConfiguration().getRevision());
        request.setVersion(session.getConfiguration().getVersion().name());
        request.setHeader(header);
        request.setBody(body);

        return request;
    }

    public String getName() {
        return "NoPubKeyDigestsRequest.xml";
    }
}
