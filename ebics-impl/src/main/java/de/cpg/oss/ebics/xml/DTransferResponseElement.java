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

import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.apache.http.HttpEntity;
import org.ebics.h004.EbicsResponse;

/**
 * The <code>DTransferResponseElement</code> is the response element
 * for all ebics downloads transfers.
 *
 * @author Hachani
 */
public class DTransferResponseElement extends EbicsResponseElement {

    private byte[] orderData;

    public DTransferResponseElement(final HttpEntity httpEntity,
                                    final OrderType orderType) {
        super(httpEntity, orderType);
    }

    public EbicsResponse build() throws EbicsException {
        final EbicsResponse ebicsResponse = super.build();
        orderData = ebicsResponse.getBody().getDataTransfer().getOrderData().getValue();
        return ebicsResponse;
    }

    /**
     * Returns the order data.
     *
     * @return the order data.
     */
    public byte[] getOrderData() {
        return orderData;
    }
}
