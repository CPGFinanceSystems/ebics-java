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
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.io.InputStreamContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.apache.http.HttpEntity;
import org.ebics.h004.EbicsResponse;

import java.io.IOException;

/**
 * The <code>InitializationResponseElement</code> is the common
 * element for transfer initialization responses.
 *
 * @author Hachani
 */
public class EbicsResponseElement {

    ReturnCode returnCode;
    final MessageProvider messageProvider;
    private byte[] transactionId;
    private final ContentFactory contentFactory;

    public EbicsResponseElement(final HttpEntity httpEntity,
                                final MessageProvider messageProvider) {
        try {
            this.contentFactory = new InputStreamContentFactory(httpEntity.getContent());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.messageProvider = messageProvider;
    }

    public EbicsResponse build() throws EbicsException {
        final String code;
        final String text;

        final EbicsResponse response = XmlUtil.parse(EbicsResponse.class, contentFactory.getContent());
        code = response.getHeader().getMutable().getReturnCode();
        text = response.getHeader().getMutable().getReportText();
        returnCode = ReturnCode.toReturnCode(code, text);
        transactionId = response.getHeader().getStatic().getTransactionID();

        return response;
    }

    public void report() throws EbicsException {
        if (!returnCode.isOk()) {
            returnCode.throwException(messageProvider);
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
}
