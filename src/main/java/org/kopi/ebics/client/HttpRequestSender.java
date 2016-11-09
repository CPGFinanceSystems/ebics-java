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

package org.kopi.ebics.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.InputStreamContentFactory;
import org.kopi.ebics.session.EbicsSession;

import java.io.IOException;


/**
 * A simple HTTP request sender and receiver.
 * The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 */
class HttpRequestSender {

    private final EbicsSession session;

    private ContentFactory response;

    /**
     * Constructs a new <code>HttpRequestSender</code> with a
     * given ebics session.
     *
     * @param session the ebics session
     */
    HttpRequestSender(final EbicsSession session) {
        this.session = session;
    }

    /**
     * Sends the request contained in the <code>ContentFactory</code>.
     * The <code>ContentFactory</code> will deliver the request as
     * an <code>InputStream</code>.
     *
     * @param request the ebics request
     * @return the HTTP return code
     */
    int send(final ContentFactory request) throws EbicsException {
        try {
            final HttpResponse httpResponse = Request
                    .Post(session.getUser().getPartner().getBank().getURL().toString())
                    .bodyStream(request.getContent(), ContentType.APPLICATION_XML)
                    // TODO .socketTimeout()
                    .execute()
                    .returnResponse();
            response = new InputStreamContentFactory(httpResponse.getEntity().getContent());
            return httpResponse.getStatusLine().getStatusCode();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the content factory of the response body
     *
     * @return the content factory of the response.
     */
    ContentFactory getResponseBody() {
        return response;
    }
}
