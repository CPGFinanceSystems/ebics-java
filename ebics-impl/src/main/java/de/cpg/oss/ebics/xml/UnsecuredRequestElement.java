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
import org.ebics.h004.*;

/**
 * The <code>UnsecuredRequestElement</code> is the common element
 * used for key management requests.
 *
 * @author hachani
 */
public class UnsecuredRequestElement {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final EbicsSession session;
    private final OrderType orderType;
    private final byte[] orderData;

    /**
     * Constructs a Unsecured Request Element.
     *
     * @param session   the ebics session.
     * @param orderType the order type (INI | HIA).
     */
    public UnsecuredRequestElement(final EbicsSession session,
                                   final OrderType orderType,
                                   final byte[] orderData) {
        this.session = session;
        this.orderType = orderType;
        this.orderData = orderData;
    }

    public EbicsUnsecuredRequest build() throws EbicsException {

        final UnsecuredReqOrderDetailsType orderDetails = OBJECT_FACTORY.createUnsecuredReqOrderDetailsType();
        orderDetails.setOrderAttribute("DZNNN");
        orderDetails.setOrderType(orderType.name());

        final ProductElementType productType = OBJECT_FACTORY.createProductElementType();
        productType.setLanguage(session.getProduct().getLanguage());
        productType.setValue(session.getProduct().getName());

        final UnsecuredRequestStaticHeaderType xstatic = OBJECT_FACTORY.createUnsecuredRequestStaticHeaderType();
        xstatic.setHostID(session.getHostId());
        xstatic.setUserID(session.getUser().getUserId());
        xstatic.setPartnerID(session.getPartner().getId());
        xstatic.setProduct(OBJECT_FACTORY.createStaticHeaderBaseTypeProduct(productType));
        xstatic.setOrderDetails(orderDetails);
        xstatic.setSecurityMedium(OrderType.HIA.equals(orderType) ? "0000" : session.getUser().getSecurityMedium());

        final EmptyMutableHeaderType mutable = OBJECT_FACTORY.createEmptyMutableHeaderType();

        final EbicsUnsecuredRequest.Header header = OBJECT_FACTORY.createEbicsUnsecuredRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);

        final EbicsUnsecuredRequest.Body.DataTransfer.OrderData orderData = OBJECT_FACTORY.createEbicsUnsecuredRequestBodyDataTransferOrderData();
        orderData.setValue(this.orderData);

        final EbicsUnsecuredRequest.Body.DataTransfer dataTransfer = OBJECT_FACTORY.createEbicsUnsecuredRequestBodyDataTransfer();
        dataTransfer.setOrderData(orderData);

        final EbicsUnsecuredRequest.Body body = OBJECT_FACTORY.createEbicsUnsecuredRequestBody();
        body.setDataTransfer(dataTransfer);

        final EbicsUnsecuredRequest unsecuredRequest = OBJECT_FACTORY.createEbicsUnsecuredRequest();
        unsecuredRequest.setHeader(header);
        unsecuredRequest.setBody(body);
        unsecuredRequest.setRevision(session.getConfiguration().getRevision());
        unsecuredRequest.setVersion(session.getConfiguration().getVersion());

        return unsecuredRequest;
    }

    public String getName() {
        return "UnsecuredRequest.xml";
    }
}
