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

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.InputStreamContentFactory;
import org.kopi.ebics.session.EbicsSession;

import java.io.IOException;
import java.io.InputStream;


/**
 * A simple HTTP request sender and receiver.
 * The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 */
class HttpRequestSender {

    /**
     * Constructs a new <code>HttpRequestSender</code> with a
     * given ebics session.
     *
     * @param session the ebics session
     */
    public HttpRequestSender(final EbicsSession session) {
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
    public final int send(final ContentFactory request) throws EbicsException {
        final HttpClient httpClient;
        final String proxyConfiguration;
        final PostMethod method;
        final RequestEntity requestEntity;
        final InputStream input;
        int retCode;

        httpClient = new HttpClient();
        proxyConfiguration = session.getConfiguration().getProperty("http.proxy.host");

        if (proxyConfiguration != null && !proxyConfiguration.equals("")) {
            final HostConfiguration hostConfig;
            final String proxyHost;
            final int proxyPort;

            hostConfig = httpClient.getHostConfiguration();
            proxyHost = session.getConfiguration().getProperty("http.proxy.host").trim();
            proxyPort = Integer.parseInt(session.getConfiguration().getProperty("http.proxy.port").trim());
            hostConfig.setProxy(proxyHost, proxyPort);
            if (!session.getConfiguration().getProperty("http.proxy.user").equals("")) {
                final String user;
                final String pwd;
                final UsernamePasswordCredentials credentials;
                final AuthScope authscope;

                user = session.getConfiguration().getProperty("http.proxy.user").trim();
                pwd = session.getConfiguration().getProperty("http.proxy.password").trim();
                credentials = new UsernamePasswordCredentials(user, pwd);
                authscope = new AuthScope(proxyHost, proxyPort);
                httpClient.getState().setProxyCredentials(authscope, credentials);
            }
        }

        try {
            input = request.getContent();
            method = new PostMethod(session.getUser().getPartner().getBank().getURL().toString());
            method.getParams().setSoTimeout(30000);
            requestEntity = new InputStreamRequestEntity(input);
            method.setRequestEntity(requestEntity);
            method.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
            retCode = httpClient.executeMethod(method);
            response = new InputStreamContentFactory(method.getResponseBodyAsStream());
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage(), e);
        }

        return retCode;
    }

    /**
     * Returns the content factory of the response body
     *
     * @return the content factory of the response.
     */
    public ContentFactory getResponseBody() {
        return response;
    }

    //////////////////////////////////////////////////////////////////
    // DATA MEMBERS
    //////////////////////////////////////////////////////////////////

    private final EbicsSession session;
    private ContentFactory response;
}
