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

package org.kopi.ebics.certificate;

import org.bouncycastle.openssl.PEMParser;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Key store loader. This class loads a key store from
 * a given path and allow to get private keys and certificates
 * for a given alias.
 * The PKCS12 key store type is recommended to be used
 *
 * @author hachani
 */
public class KeyStoreManager {

    /**
     * Loads a certificate for a given alias
     *
     * @param alias the certificate alias
     * @return the certificate
     * @throws KeyStoreException
     */
    public final X509Certificate getCertificate(final String alias) throws KeyStoreException {
        final X509Certificate cert;

        cert = (X509Certificate) keyStore.getCertificate(alias);

        if (cert == null) {
            throw new IllegalArgumentException("alias " + alias + " not found in the KeyStore");
        }

        return cert;
    }

    /**
     * Loads a private key for a given alias
     *
     * @param alias the certificate alias
     * @return the private key
     * @throws GeneralSecurityException
     */
    public final PrivateKey getPrivateKey(final String alias) throws GeneralSecurityException {
        final PrivateKey key;

        key = (PrivateKey) keyStore.getKey(alias, password);
        if (key == null) {
            throw new IllegalArgumentException("private key not found for alias " + alias);
        }

        return key;
    }

    /**
     * Loads a key store from a given path and password
     *
     * @param path     the key store path
     * @param password the key store password
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void load(final String path, final char[] password)
            throws GeneralSecurityException, IOException {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        this.password = password;
        load(path);
    }

    /**
     * Loads a key store and cache the loaded one
     *
     * @param path the key store path.
     */
    private void load(final String path) throws GeneralSecurityException, IOException {
        if (path.equals("")) {
            this.keyStore.load(null, null);
        } else {
            this.keyStore.load(new FileInputStream(path), password);
            this.certs = read(this.keyStore);
        }
    }

    /**
     * Reads a certificate from an input stream for a given provider
     *
     * @param x509CertificateData the input stream
     * @param provider            the certificate provider
     * @return the certificate
     */
    public X509Certificate read(final InputStream x509CertificateData, final Provider provider) {
        X509Certificate certificate;

        try {
            certificate = (X509Certificate) CertificateFactory.getInstance("X.509", provider).generateCertificate(x509CertificateData);

            if (certificate == null) {
                certificate = (X509Certificate) (new PEMParser(new InputStreamReader(x509CertificateData))).readObject();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return certificate;
    }

    /**
     * Returns the public key of a given certificate.
     *
     * @param x509CertificateData the given certificate
     * @return The RSA public key of the given certificate
     */
    public RSAPublicKey getPublicKey(final InputStream x509CertificateData) {
        final X509Certificate cert;

        cert = read(x509CertificateData, keyStore.getProvider());
        return (RSAPublicKey) cert.getPublicKey();
    }

    public RSAPublicKey getPublicKey(final byte[] modulus, final byte[] exponent) {
        final RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        try {
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the given certificate into the key store.
     *
     * @param alias the certificate alias
     */
    public void setPublicKeyEntry(final String alias, final RSAPublicKey publicKey) {
        try {
            keyStore.setKeyEntry(alias, publicKey, password, new Certificate[0]);
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the key store to a given output stream.
     *
     * @param output the output stream.
     */
    public void save(final OutputStream output)
            throws GeneralSecurityException, IOException {
        keyStore.store(output, password);
    }

    /**
     * Returns the certificates contained in the key store.
     *
     * @return the certificates contained in the key store.
     */
    public Map<String, X509Certificate> getCertificates() {
        return certs;
    }

    /**
     * Reads all certificate existing in a given key store
     *
     * @param keyStore the key store
     * @return A <code>Map</code> of certificate,
     * the key of the map is the certificate alias
     * @throws KeyStoreException
     */
    public Map<String, X509Certificate> read(final KeyStore keyStore)
            throws KeyStoreException {
        final Map<String, X509Certificate> certificates;
        final Enumeration<String> enumeration;

        certificates = new HashMap<String, X509Certificate>();
        enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            final String alias;

            alias = enumeration.nextElement();
            certificates.put(alias, (X509Certificate) keyStore.getCertificate(alias));
        }

        return certificates;
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private KeyStore keyStore;
    private char[] password;
    private Map<String, X509Certificate> certs;
}
