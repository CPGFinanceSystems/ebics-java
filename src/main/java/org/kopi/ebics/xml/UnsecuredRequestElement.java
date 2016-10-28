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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header;
import org.kopi.ebics.schema.h004.EmptyMutableHeaderType;
import org.kopi.ebics.schema.h004.OrderDetailsType;
import org.kopi.ebics.schema.h004.ProductElementType;
import org.kopi.ebics.schema.h004.UnsecuredRequestStaticHeaderType;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;

/**
 * The <code>UnsecuredRequestElement</code> is the common element
 * used for key management requests.
 *
 * @author hachani
 */
public class UnsecuredRequestElement extends DefaultEbicsRootElement {

    /**
     * Constructs a Unsecured Request Element.
     *
     * @param session   the ebics session.
     * @param orderType the order type (INI | HIA).
     */
    public UnsecuredRequestElement(final EbicsSession session,
                                   final OrderType orderType,
                                   final byte[] orderData) {
        super(session);
        this.orderType = orderType;
        this.orderData = orderData;
    }

    @Override
    public void build() throws EbicsException {
        final Header header;
        final Body body;
        final EmptyMutableHeaderType mutable;
        final UnsecuredRequestStaticHeaderType xstatic;
        final ProductElementType productType;
        final OrderDetailsType orderDetails;
        final DataTransfer dataTransfer;
        final OrderData orderData;
        final EbicsUnsecuredRequest request;

        orderDetails = EbicsXmlFactory.createOrderDetailsType("DZNNN", orderType.name());

        productType = EbicsXmlFactory.creatProductElementType(session.getProduct().getLanguage(),
                session.getProduct().getName());

        xstatic = EbicsXmlFactory.createUnsecuredRequestStaticHeaderType(session.getBankID(),
                session.getUser().getPartner().getPartnerId(),
                session.getUser().getUserId(),
                productType,
                orderDetails,
                session.getUser().getSecurityMedium());
        mutable = EbicsXmlFactory.createEmptyMutableHeaderType();

        header = EbicsXmlFactory.createHeader(true,
                mutable,
                xstatic);

        orderData = EbicsXmlFactory.createOrderData(this.orderData);
        dataTransfer = EbicsXmlFactory.createDataTransfer(orderData);
        body = EbicsXmlFactory.createBody(dataTransfer);
        request = EbicsXmlFactory.createEbicsUnsecuredRequest(header,
                body,
                session.getConfiguration().getRevision(),
                session.getConfiguration().getVersion());

        document = EbicsXmlFactory.createEbicsUnsecuredRequestDocument(request);
    }

    @Override
    public String getName() {
        return "UnsecuredRequest.xml";
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final OrderType orderType;
    private final byte[] orderData;
    private static final long serialVersionUID = -3548730114599886711L;
}
