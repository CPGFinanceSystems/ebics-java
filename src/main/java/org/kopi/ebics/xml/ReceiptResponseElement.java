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

import org.apache.http.HttpResponse;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.exception.ReturnCode;

/**
 * The <code>ReceiptResponseElement</code> is the response element
 * for ebics receipt request.
 *
 * @author Hachani
 */
public class ReceiptResponseElement extends EbicsResponseElement {

    public ReceiptResponseElement(final HttpResponse httpResponse) {
        super(httpResponse, null);
    }

    @Override
    public void report() throws EbicsException {
        if (!returnCode.equals(ReturnCode.EBICS_DOWNLOAD_POSTPROCESS_DONE)) {
            returnCode.throwException();
        }
    }
}
