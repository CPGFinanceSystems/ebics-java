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
import de.cpg.oss.ebics.api.exception.NoDownloadDataAvailableException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.apache.http.HttpResponse;
import org.ebics.h004.EbicsResponse;
import org.kopi.ebics.session.OrderType;

/**
 * The <code>DInitializationResponseElement</code> is the response element
 * for ebics downloads initializations.
 *
 * @author Hachani
 */
public class DInitializationResponseElement extends EbicsResponseElement {

    private int numSegments;
    private int segmentNumber;
    private boolean lastSegment;
    private byte[] transactionKey;
    private byte[] orderData;

    public DInitializationResponseElement(final HttpResponse httpResponse,
                                          final OrderType orderType) {
        super(httpResponse, orderType);
    }

    @Override
    public EbicsResponse build() throws EbicsException {
        final String bodyRetCode;

        final EbicsResponse response = super.build();
        bodyRetCode = response.getBody().getReturnCode().getValue();
        final ReturnCode returnCode = ReturnCode.toReturnCode(bodyRetCode, "");
        if (returnCode.equals(ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE)) {
            throw new NoDownloadDataAvailableException();
        }
        numSegments = response.getHeader().getStatic().getNumSegments().intValue();
        segmentNumber = response.getHeader().getMutable().getSegmentNumber().getValue().intValue();
        lastSegment = response.getHeader().getMutable().getSegmentNumber().isLastSegment();
        transactionKey = response.getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
        orderData = response.getBody().getDataTransfer().getOrderData().getValue();

        return response;
    }

    /**
     * Returns the total segments number.
     *
     * @return the total segments number.
     */
    public int getSegmentsNumber() {
        return numSegments;
    }

    /**
     * Returns The current segment number.
     *
     * @return the segment number.
     */
    public int getSegmentNumber() {
        return segmentNumber;
    }

    /**
     * Checks if it is the last segment.
     *
     * @return True is it is the last segment.
     */
    public boolean isLastSegment() {
        return lastSegment;
    }

    /**
     * Returns the transaction key.
     *
     * @return the transaction key.
     */
    public byte[] getTransactionKey() {
        return transactionKey;
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