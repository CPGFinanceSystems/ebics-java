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

package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsBank;
import de.cpg.oss.ebics.api.Messages;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.nio.charset.Charset;


/**
 * A simple HTTP request sender and receiver.
 * The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 */
public abstract class HttpUtil {

    /**
     * Sends the request contained in the <code>ContentFactory</code>.
     * The <code>ContentFactory</code> will deliver the request as
     * an <code>InputStream</code>.
     *
     * @param xmlRequest the ebics request
     */
    public static HttpEntity sendAndReceive(final EbicsBank ebicsBank, final ContentFactory xmlRequest) throws EbicsException {
        try {
            final HttpResponse httpResponse = Request.Post(ebicsBank.getUri().toString())
                    .bodyStream(xmlRequest.getContent(), ContentType.APPLICATION_XML.withCharset(Charset.defaultCharset()))
                    // TODO .socketTimeout()
                    .execute()
                    .returnResponse();
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException(Messages.getString("http.code.error",
                        Constants.APPLICATION_BUNDLE_NAME,
                        httpResponse.getStatusLine().getStatusCode()));
            }
            return httpResponse.getEntity();
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }
}
