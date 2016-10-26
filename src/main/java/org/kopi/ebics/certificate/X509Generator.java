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

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * An X509 certificate generator for EBICS protocol.
 * Generated certificates are self signed certificates.
 *
 * @author hachani
 */
@SuppressWarnings("deprecation")
public class X509Generator {

    /**
     * Generates the signature certificate for the EBICS protocol
     *
     * @param keypair   the key pair
     * @param issuer    the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter  the end validity date
     * @return the signature certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public X509Certificate generateA005Certificate(final KeyPair keypair,
                                                   final String issuer,
                                                   final Date notBefore,
                                                   final Date notAfter)
            throws GeneralSecurityException, IOException {
        return generate(keypair,
                issuer,
                notBefore,
                notAfter,
                X509Constants.SIGNATURE_KEY_USAGE);
    }

    /**
     * Generates the authentication certificate for the EBICS protocol
     *
     * @param keypair   the key pair
     * @param issuer    the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter  the end validity date
     * @return the authentication certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public X509Certificate generateX002Certificate(final KeyPair keypair,
                                                   final String issuer,
                                                   final Date notBefore,
                                                   final Date notAfter)
            throws GeneralSecurityException, IOException {
        return generate(keypair,
                issuer,
                notBefore,
                notAfter,
                X509Constants.AUTHENTICATION_KEY_USAGE);
    }

    /**
     * Generates the encryption certificate for the EBICS protocol
     *
     * @param keypair   the key pair
     * @param issuer    the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter  the end validity date
     * @return the encryption certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public X509Certificate generateE002Certificate(final KeyPair keypair,
                                                   final String issuer,
                                                   final Date notBefore,
                                                   final Date notAfter)
            throws GeneralSecurityException, IOException {
        return generate(keypair,
                issuer,
                notBefore,
                notAfter,
                X509Constants.ENCRYPTION_KEY_USAGE);
    }

    /**
     * Returns an <code>X509Certificate</code> from a given
     * <code>KeyPair</code> and limit dates validations
     *
     * @param keypair   the given key pair
     * @param issuer    the certificate issuer
     * @param notBefore the begin validity date
     * @param notAfter  the end validity date
     * @param keyusage  the certificate key usage
     * @return the X509 certificate
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public X509Certificate generate(final KeyPair keypair,
                                    final String issuer,
                                    final Date notBefore,
                                    final Date notAfter,
                                    final int keyusage)
            throws GeneralSecurityException, IOException {
        final X509V3CertificateGenerator generator;
        final BigInteger serial;
        final X509Certificate certificate;
        final ASN1EncodableVector vector;

        serial = BigInteger.valueOf(generateSerial());
        generator = new X509V3CertificateGenerator();
        generator.setSerialNumber(serial);
        generator.setIssuerDN(new X509Principal(issuer));
        generator.setNotBefore(notBefore);
        generator.setNotAfter(notAfter);
        generator.setSubjectDN(new X509Principal(issuer));
        generator.setPublicKey(keypair.getPublic());
        generator.setSignatureAlgorithm(X509Constants.SIGNATURE_ALGORITHM);
        generator.addExtension(X509Extensions.BasicConstraints,
                false,
                new BasicConstraints(true));
        generator.addExtension(X509Extensions.SubjectKeyIdentifier,
                false,
                getSubjectKeyIdentifier(keypair.getPublic()));
        generator.addExtension(X509Extensions.AuthorityKeyIdentifier,
                false,
                getAuthorityKeyIdentifier(keypair.
                                getPublic(),
                        issuer,
                        serial));
        vector = new ASN1EncodableVector();
        vector.add(KeyPurposeId.id_kp_emailProtection);

        generator.addExtension(X509Extensions.ExtendedKeyUsage, false, new ExtendedKeyUsage(new DERSequence(vector)));

        switch (keyusage) {
            case X509Constants.SIGNATURE_KEY_USAGE:
                generator.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.nonRepudiation));
                break;
            case X509Constants.AUTHENTICATION_KEY_USAGE:
                generator.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.digitalSignature));
                break;
            case X509Constants.ENCRYPTION_KEY_USAGE:
                generator.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.keyAgreement));
                break;
            default:
                generator.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature));
                break;
        }

        certificate = generator.generate(keypair.getPrivate(), "BC", new SecureRandom());
        certificate.checkValidity(new Date());
        certificate.verify(keypair.getPublic());

        return certificate;
    }

    /**
     * Returns the <code>AuthorityKeyIdentifier</code> corresponding
     * to a given <code>PublicKey</code>
     *
     * @param publicKey the given public key
     * @param issuer    the certificate issuer
     * @param serial    the certificate serial number
     * @return the authority key identifier of the public key
     * @throws IOException
     */
    private AuthorityKeyIdentifier getAuthorityKeyIdentifier(final PublicKey publicKey,
                                                             final String issuer,
                                                             final BigInteger serial)
            throws IOException {
        final InputStream input;
        final SubjectPublicKeyInfo keyInfo;
        final ASN1EncodableVector vector;

        input = new ByteArrayInputStream(publicKey.getEncoded());
        keyInfo = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(input).readObject());
        vector = new ASN1EncodableVector();
        vector.add(new GeneralName(new X509Name(issuer)));

        return new AuthorityKeyIdentifier(keyInfo, new GeneralNames(new DERSequence(vector)), serial);
    }

    /**
     * Returns the <code>SubjectKeyIdentifier</code> corresponding
     * to a given <code>PublicKey</code>
     *
     * @param publicKey the given public key
     * @return the subject key identifier
     * @throws IOException
     */
    private SubjectKeyIdentifier getSubjectKeyIdentifier(final PublicKey publicKey)
            throws IOException {
        final InputStream input;
        final SubjectPublicKeyInfo keyInfo;

        input = new ByteArrayInputStream(publicKey.getEncoded());
        keyInfo = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(input).readObject());

        return new SubjectKeyIdentifier(keyInfo);
    }

    /**
     * Generates a random serial number
     *
     * @return the serial number
     */
    private long generateSerial() {
        final Date now;

        now = new Date();
        final String sNow = sdfSerial.format(now);

        return Long.valueOf(sNow).longValue();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private static final SimpleDateFormat sdfSerial;

    static {
        sdfSerial = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        sdfSerial.setTimeZone(tz);
    }
}
