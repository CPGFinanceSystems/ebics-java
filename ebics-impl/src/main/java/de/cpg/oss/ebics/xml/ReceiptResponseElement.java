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

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.apache.http.HttpEntity;

/**
 * The <code>ReceiptResponseElement</code> is the response element
 * for ebics receipt request.
 *
 * @author Hachani
 */
public class ReceiptResponseElement extends EbicsResponseElement {

    public ReceiptResponseElement(final HttpEntity httpEntity, final MessageProvider messageProvider) {
        super(httpEntity, messageProvider);
    }

    @Override
    public void report() throws EbicsException {
        if (!returnCode.equals(ReturnCode.EBICS_DOWNLOAD_POSTPROCESS_DONE)) {
            returnCode.throwException(messageProvider);
        }
    }
}
