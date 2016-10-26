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

package org.kopi.ebics.security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * A simple SSL socket factory for EBICS client.
 *
 * @author hachani
 */
public class EbicsSocketFactory extends SSLSocketFactory {

    /**
     * Constructs a new <code>EbicsSocketFactory</code> from an SSL context
     *
     * @param context the <code>SSLContext</code>
     */
    public EbicsSocketFactory(final SSLContext context) {
        this.context = context;
    }

    /**
     * Constructs a new <code>EbicsSocketFactory</code> from
     * key store and trust store information
     *
     * @param keystore       the key store
     * @param keystoreType   the key store type
     * @param keystrorePass  the key store password
     * @param truststore     the trust store
     * @param truststoreType the trust store type
     * @param truststorePass the trust store password
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public EbicsSocketFactory(final byte[] keystore,
                              final String keystoreType,
                              final char[] keystrorePass,
                              final byte[] truststore,
                              final String truststoreType,
                              final char[] truststorePass)
            throws IOException, GeneralSecurityException {
        this.context = getSSLContext(keystore,
                keystoreType,
                keystrorePass,
                truststore,
                truststoreType,
                truststorePass);
    }

    /**
     * Returns the <code>SSLContext</code> from key store information.
     *
     * @param keystore       the key store
     * @param keystoreType   the key store type
     * @param keystrorePass  the key store password
     * @param truststore     the trust store
     * @param truststoreType the trust store type
     * @param truststorePass the trust store password
     * @return the <code>SSLContext</code>
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public SSLContext getSSLContext(final byte[] keystore,
                                    final String keystoreType,
                                    final char[] keystrorePass,
                                    final byte[] truststore,
                                    final String truststoreType,
                                    final char[] truststorePass)
            throws IOException, GeneralSecurityException {
        final KeyStore kstore;
        final KeyStore tstore;
        final KeyManagerFactory kmf;
        final TrustManagerFactory tmf;
        final SSLContext context;

        kstore = initKeyStore(keystore, keystrorePass, keystoreType);
        kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(kstore, keystrorePass);

        tstore = initKeyStore(truststore, truststorePass, truststoreType);
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tstore);
        context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return context;
    }

    /**
     * Initializes a key store.
     *
     * @param keystore the key store
     * @param password the key store password
     * @return key store
     * @throws IOException
     */
    protected KeyStore initKeyStore(final byte[] keystore, final char[] password, final String type)
            throws IOException {
        try {
            final KeyStore kstore;

            kstore = KeyStore.getInstance(type);
            kstore.load(new ByteArrayInputStream(keystore), password);
            return kstore;
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IOException("Exception trying to load keystore " + type + ": " + e.toString());
        }
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose)
            throws IOException {
        return context.getSocketFactory().createSocket(s, host, port, autoClose);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return context.getSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return context.getSocketFactory().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return context.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return context.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
            throws IOException {
        return context.getSocketFactory().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort)
            throws IOException {
        return context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    private final SSLContext context;
}
