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

import de.cpg.oss.ebics.api.EbicsSignatureKey;
import de.cpg.oss.ebics.api.exception.EbicsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;


/**
 * Some utilities for EBICS request creation and reception
 *
 * @author hachani
 */
@Slf4j
public abstract class CryptoUtil {

    /**
     * Generates a random nonce.
     * <p>
     * <p>EBICS Specification 2.4.2 - 11.6 Generation of the transaction IDs:
     * <p>
     * <p>Transaction IDs are cryptographically-strong random numbers with a length of 128 bits. This
     * means that the likelihood of any two bank systems using the same transaction ID at the
     * same time is sufficiently small.
     * <p>
     * <p>Transaction IDs are generated by cryptographic pseudo-random number generators (PRNG)
     * that have been initialized with a real random number (seed). The entropy of the seed should
     * be at least 100 bits.
     *
     * @return a random nonce.
     */
    public static byte[] generateNonce() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").generateSeed(16);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypts an input with a given key spec.
     * <p>
     * <p>EBICS Specification 2.4.2 - 15.1 Workflows at the sender’s end:
     * <p>
     * <p><b>Preparation for DEK encryption</b>
     * <p>The 128 bit DEK that is interpreted as a natural number is filled out with null bits to 768 bits in
     * front of the highest-value bit. The result is called PDEK.
     * <p>
     * <p><b>Encryption of the secret DES key</b>
     * <p>PDEK is then encrypted with the recipient’s public key of the RSA key system and is then
     * expanded with leading null bits to 1024 bits.
     * <p>The result is called EDEK. It must be ensured that EDEK is not equal to DEK.
     * <p>
     * <p><b>Encryption of the messages</b>
     * <p><U>Padding of the message:</U>
     * <p>The method Padding with Octets in accordance with ANSI X9.23 is used for padding the
     * message, i.e. in all cases, data is appended to the message that is to be encrypted.
     * <p>
     * <p><U>Application of the encryption algorithm:</U>
     * <p>The message is encrypted in CBC mode in accordance with ANSI X3.106 with the secret key
     * DEK according to the 2-key triple DES process as specified in ANSI X3.92-1981.
     * <p>In doing this, the following initialization value “ICV” is used: X ‘00 00 00 00 00 00 00 00’.
     *
     * @param input   the input to encrypt
     * @param keySpec the key spec
     * @return the encrypted input
     */
    public static byte[] encrypt(final byte[] input, final SecretKeySpec keySpec) {
        return encryptOrDecrypt(Cipher.ENCRYPT_MODE, input, keySpec);
    }

    /**
     * Decrypts the given input according to key spec.
     *
     * @param input   the input to decrypt
     * @param keySpec the key spec
     * @return the decrypted input
     */
    public static byte[] decrypt(final byte[] input, final SecretKeySpec keySpec)
            throws EbicsException {
        return encryptOrDecrypt(Cipher.DECRYPT_MODE, input, keySpec);
    }

    /**
     * Encrypts or decrypts the given input according to key spec.
     *
     * @param mode    the encryption-decryption mode.
     * @param input   the input to encrypt or decrypt.
     * @param keySpec the key spec.
     * @return the encrypted or decrypted data.
     */
    private static byte[] encryptOrDecrypt(final int mode, final byte[] input, final SecretKeySpec keySpec) {
        final IvParameterSpec iv;
        final Cipher cipher;

        iv = new IvParameterSpec(new byte[16]);
        try {
            cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
            cipher.init(mode, keySpec, iv);
            return cipher.doFinal(input);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
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
     * EBICS Specification 2.4.2 - 11.1.1 Process:
     * <p>
     * <p>Identification and authentication signatures are based on the RSA signature process.
     * The following parameters determine the identification and authentication signature process:
     * <p>
     * <ol>
     * <li> Length of the (secret) RSA key
     * <li> Hash algorithm
     * <li> Padding process
     * <li> Canonisation process.
     * </ol>
     * <p>
     * <p>For the identification and authentication process, EBICS defines the process “X002” with
     * the following parameters:
     * <ol>
     * <li>Key length in Kbit >=1Kbit (1024 bit) and lesser than 16Kbit</li>
     * <li>Hash algorithm SHA-256</li>
     * <li>Padding process: PKCS#1</li>
     * <li>Canonisation process: http://www.w3.org/TR/2001/REC-xml-c14n-20010315
     * </ol>
     * <p>
     * <p>From EBICS 2.4 on, the customer system must use the hash value of the public bank key
     * X002 in a request.
     * <p>
     * <p>Notes:
     * <ol>
     * <li> The key length is defined else where.
     * <li> The padding is performed by the {@link Signature} class.
     * <li> The digest must be already canonized
     * </ol>
     */
    public static byte[] authenticate(final byte[] digest, final PrivateKey x002Key) throws GeneralSecurityException {
        final Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(x002Key);
        signature.update(digest);
        return signature.sign();
    }

    /**
     * EBICS Specification 2.4.2 - 14.1 Version A005/A006 of the electronic signature:
     * <p>
     * <p>For the signature processes A005 an interval of 1536 bit (minimum)
     * and 4096 bit (maximum) is defined for the key length.
     * <p>
     * <p>The digital signature mechanisms A005 is both based on the industry standard
     * [PKCS1] using the hash algorithm SHA-256. They are both signature mechanisms without
     * message recovery.
     * <p>
     * <p>A hash algorithm maps bit sequences of arbitrary length (input bit sequences) to byte
     * sequences of a fixed length, determined by the Hash algorithm. The result of the execution of
     * a Hash algorithm to a bit sequence is defined as hash value.
     * <p>
     * <p>The hash algorithm SHA-256 is specified in [FIPS H2]. SHA-256 maps input bit sequences of
     * arbitrary length to byte sequences of 32 byte length. The padding of input bit sequences to a
     * length being a multiple of 64 byte is part of the hash algorithm. The padding even is applied if
     * the input bit sequence already has a length that is a multiple of 64 byte.
     * <p>
     * <p>SHA-256 processes the input bit sequences in blocks of 64 byte length.
     * The hash value of a bit sequence x under the hash algorithm SHA-256 is referred to as
     * follows: SHA-256(x).
     * <p>
     * <p>The digital signature mechanism A005 is identical to EMSA-PKCS1-v1_5 using the hash
     * algorithm SHA-256. The byte length H of the hash value is 32.
     * <p>
     * According [PKCS1] (using the method EMSA-PKCS1-v1_5) the following steps shall be
     * performed for the computation of a signature for message M with bit length m.
     * <ol>
     * <li> The hash value HASH(M) of the byte length H shall be computed. In the case of A005
     * SHA-256(M) with a length of 32 bytes.</li>
     * <li> The DSI for the signature algorithm shall be generated.</li>
     * <li> A signature shall be computed using the DSI with the standard algorithm for the
     * signature generation described in section 14.1.3.1 of the EBICS specification (V 2.4.2).
     * </ol>
     * <p>
     * <p>The {@link Signature} is a digital signature scheme with
     * appendix (SSA) combining the RSA algorithm with the EMSA-PKCS1-v1_5 encoding
     * method.
     * <p>
     * <p> The {@code digest} will be signed with the RSA user signature key using the
     * {@link Signature} that will be instantiated with the <b>SHA-256</b>
     * algorithm. This signature is then put in a UserSignature XML object that will be sent to the EBICS server.
     */
    public static byte[] sign(final byte[] digest, final EbicsSignatureKey signatureKey) throws IOException, GeneralSecurityException {
        final Signature signature;
        switch (signatureKey.getVersion()) {
            case A005:
                signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(signatureKey.getPrivateKey());
                signature.update(removeOSSpecificChars(digest));
                return signature.sign();

            default:
            case A006:
                signature = Signature.getInstance("SHA256withRSAandMGF1", BouncyCastleProvider.PROVIDER_NAME);
                signature.initSign(signatureKey.getPrivateKey());
                final MessageDigest digester = MessageDigest.getInstance("SHA-256");
                signature.update(digester.digest(removeOSSpecificChars(digest)));
                return signature.sign();
        }
    }

    /**
     * EBICS Specification 2.4.2 - 7.1 Process description:
     * <p>
     * <p>In particular, so-called “white-space characters” such as spaces, tabs, carriage
     * returns and line feeds (“CR/LF”) are not permitted.
     * <p>
     * <p> All white-space characters should be removed from entry buffer {@code buf}.
     *
     * @param buf the given byte buffer
     * @return The byte buffer portion corresponding to the given length and offset
     */
    private static byte[] removeOSSpecificChars(final byte[] buf) {
        final ByteArrayOutputStream output;

        output = new ByteArrayOutputStream();
        for (final byte aBuf : buf) {
            switch (aBuf) {
                case '\r':
                case '\n':
                case 0x1A: // CTRL-Z / EOF
                    // ignore this character
                    break;

                default:
                    output.write(aBuf);
            }
        }

        return output.toByteArray();
    }

    /**
     * EBICS IG CFONB VF 2.1.4 2012 02 24 - 2.1.3.2 Calcul de la signature:
     * <p>
     * <p>Il convient d’utiliser PKCS1 V1.5 pour chiffrer la clé de chiffrement.
     * <p>
     * <p>EBICS Specification 2.4.2 - 15.2 Workflows at the recipient’s end:
     * <p>
     * <p><b>Decryption of the DES key</b>
     * <p>The leading 256 null bits of the EDEK are removed and the remaining 768 bits are decrypted
     * with the recipient’s secret key of the RSA key system. PDEK is then present. The secret DES
     * key DEK is obtained from the lowest-value 128 bits of PDEK, this is split into the individual
     * keys DEK<SUB>left</SUB> and DEK<SUB>right</SUB>.
     */
    public static byte[] decrypt(final byte[] encryptedData, final byte[] transactionKey, final PrivateKey e002Key) {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, e002Key);
            return decryptData(encryptedData, cipher.doFinal(transactionKey));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts the <code>encryptedData</code> using the decoded transaction key.
     * <p>
     * <p>EBICS Specification 2.4.2 - 15.2 Workflows at the recipient’s end:
     * <p>
     * <p><b>Decryption of the message</b>
     * <p>The encrypted original message is decrypted in CBC mode in accordance with the 2-key
     * triple DES process via the secret DES key (comprising DEK<SUB>left</SUB> and DEK<SUP>right<SUB>).
     * In doing this, the following initialization value ICV is again used.
     * <p>
     * <p><b>Removal of the padding information</b>
     * <p>The method “Padding with Octets” according to ANSI X9.23 is used to remove the padding
     * information from the decrypted message. The original message is then available in decrypted
     * form.
     *
     * @param input The encrypted data
     * @param key   The secret key.
     * @return The decrypted data sent from the EBICS bank.
     */
    private static byte[] decryptData(final byte[] input, final byte[] key)
            throws EbicsException {
        return decrypt(input, new SecretKeySpec(key, "AES"));
    }

    public static byte[] generateTransactionKey(final byte[] nonce, final RSAPublicKey encryptionKey) throws EbicsException {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            final BigInteger data = new BigInteger(nonce);
            log.debug("Data bits: {}", data.bitLength());
            log.debug("Modulus bits: {}", encryptionKey.getModulus().bitLength());
            log.debug("Compare: {}", data.compareTo(encryptionKey.getModulus()));
            return cipher.doFinal(nonce);
        } catch (final Exception e) {
            throw new EbicsException(e);
        }
    }
}
