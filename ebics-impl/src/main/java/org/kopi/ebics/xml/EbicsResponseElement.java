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

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.apache.http.HttpResponse;
import org.ebics.h004.EbicsResponse;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.InputStreamContentFactory;
import org.kopi.ebics.session.OrderType;

import java.io.IOException;

/**
 * The <code>InitializationResponseElement</code> is the common
 * element for transfer initialization responses.
 *
 * @author Hachani
 */
public class EbicsResponseElement {

    private byte[] transactionId;
    protected ReturnCode returnCode;
    private final OrderType orderType;
    private final ContentFactory contentFactory;

    public EbicsResponseElement(final HttpResponse httpResponse,
                                final OrderType orderType) {
        try {
            this.contentFactory = new InputStreamContentFactory(httpResponse.getEntity().getContent());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.orderType = orderType;
    }

    public EbicsResponse build() throws EbicsException {
        final String code;
        final String text;

        final EbicsResponse response = XmlUtils.parse(EbicsResponse.class, contentFactory.getContent());
        code = response.getHeader().getMutable().getReturnCode();
        text = response.getHeader().getMutable().getReportText();
        returnCode = ReturnCode.toReturnCode(code, text);
        transactionId = response.getHeader().getStatic().getTransactionID();

        return response;
    }

    public void report() throws EbicsException {
        if (!returnCode.isOk()) {
            returnCode.throwException();
        }
    }

    /**
     * Returns the transaction ID.
     *
     * @return the transaction ID.
     */
    public byte[] getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the order type.
     *
     * @return the order type.
     */
    public OrderType getOrderType() {
        return orderType;
    }
}
