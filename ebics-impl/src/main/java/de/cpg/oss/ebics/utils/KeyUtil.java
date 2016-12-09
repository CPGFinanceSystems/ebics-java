package de.cpg.oss.ebics.utils;

import org.apache.commons.codec.binary.Hex;

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
public abstract class KeyUtil {

    /**
     * EBICS key size
     */
    public static final int EBICS_KEY_SIZE = 2048;
    public static final String EBICS_ENCRYPTION_KEY_ALGORITHM = "RSA";

    /**
     * Generates a <code>KeyPair</code> in RSA format.
     *
     * @param keyLen - key size
     * @return KeyPair the key pair
     */
    public static KeyPair createRsaKeyPair(final int keyLen) throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen;

        keyGen = KeyPairGenerator.getInstance(EBICS_ENCRYPTION_KEY_ALGORITHM);
        keyGen.initialize(keyLen, new SecureRandom());

        return keyGen.generateKeyPair();
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
    public static byte[] getKeyDigest(final PublicKey publicKey) {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        final String exponent = Hex.encodeHexString(rsaPublicKey.getPublicExponent().toByteArray()).replaceFirst("^0+", "");
        final String modulus = Hex.encodeHexString(rsaPublicKey.getModulus().toByteArray()).replaceFirst("^0+", "");
        final String hash = exponent.concat(" ").concat(modulus).toLowerCase();

        try {
            return MessageDigest.getInstance(CryptoUtil.EBICS_DIGEST_ALGORITHM).digest(hash.getBytes("US-ASCII"));
        } catch (final GeneralSecurityException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPublicKey getPublicKey(final byte[] modulus, final byte[] exponent) {
        final RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        try {
            final KeyFactory factory = KeyFactory.getInstance(EBICS_ENCRYPTION_KEY_ALGORITHM);
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
