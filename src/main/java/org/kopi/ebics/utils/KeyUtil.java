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

package org.kopi.ebics.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.kopi.ebics.exception.EbicsException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Some key utilities
 *
 * @author hachani
 */
public class KeyUtil {

    /**
     * EBICS key size
     */
    public static final int EBICS_KEY_SIZE = 2048;

    /**
     * Generates a <code>KeyPair</code> in RSA format.
     *
     * @param keyLen - key size
     * @return KeyPair the key pair
     */
    public static KeyPair createRsaKeyPair(final int keyLen) throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen;

        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keyLen, new SecureRandom());

        return keyGen.generateKeyPair();
    }

    /**
     * Generates a random password
     *
     * @return the password
     */
    public static String generatePassword() {
        final SecureRandom random;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            final String pwd = Base64.encodeBase64String(random.generateSeed(5));

            return pwd.substring(0, pwd.length() - 2);
        } catch (final NoSuchAlgorithmException e) {
            return "changeit";
        }
    }

    /**
     * Returns the digest value of a given public key.
     * <p>
     * <p>
     * <p>In Version “H003” of the EBICS protocol the ES of the financial:
     * <p>
     * <p>The SHA-256 hash values of the financial institution's public keys for X002 and E002 are
     * composed by concatenating the exponent with a blank character and the modulus in hexadecimal
     * representation (using lower case letters) without leading zero (as to the hexadecimal
     * representation). The resulting string has to be converted into a byte array based on US ASCII
     * code.
     *
     * @param publicKey the public key
     * @return the digest value
     */
    public static byte[] getKeyDigest(final RSAPublicKey publicKey) throws EbicsException {
        final String exponent = Hex.encodeHexString(publicKey.getPublicExponent().toByteArray()).replaceFirst("^0+", "");
        final String modulus = Hex.encodeHexString(publicKey.getModulus().toByteArray()).replaceFirst("^0+", "");
        final String hash = exponent.concat(" ").concat(modulus).toLowerCase();

        try {
            return MessageDigest.getInstance("SHA-256").digest(hash.getBytes("US-ASCII"));
        } catch (final GeneralSecurityException | UnsupportedEncodingException e) {
            throw new EbicsException(e.getMessage(), e);
        }
    }

    public static RSAPublicKey getPublicKey(final byte[] modulus, final byte[] exponent) {
        final RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        try {
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
