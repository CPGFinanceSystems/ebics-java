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

import org.kopi.ebics.certificate.UserKeyManager;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsPartner;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.interfaces.PasswordCallback;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.UserSignature;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;

/**
 * Simple implementation of an EBICS user.
 * This object is not serializable, but it should be persisted every time it needs to be saved.
 * Persistence is achieved via <code>save(ObjectOutputStream)</code> and the matching constructor.
 *
 * @author Hachani
 */
class User implements EbicsUser {

    private static final long serialVersionUID = 1L;

    /**
     * First time constructor. Use this constructor,
     * if you want to prepare the first communication with the bank. For further communications you should recover persisted objects.
     * All required signature keys will be generated now.
     *
     * @param partner          customer in whose name we operate.
     * @param userId           UserId as obtained from the bank.
     * @param name             the user name,
     * @param passwordCallback a callback-handler that supplies us with the password.
     *                         This parameter can be null, in this case no password is used.
     */
    public User(final EbicsPartner partner,
                final String userId,
                final String name,
                final PasswordCallback passwordCallback)
            throws GeneralSecurityException, IOException {
        this.partner = partner;
        this.userId = userId;
        this.name = name;
        this.passwordCallback = passwordCallback;
        createUserKeyPairs();
        needSave = true;
    }

    /**
     * Reconstructs a persisted EBICS user.
     *
     * @param partner          the customer in whose name we operate.
     * @param ois              the object stream
     * @param passwordCallback a callback-handler that supplies us with the password.
     * @throws GeneralSecurityException if the supplies password is wrong.
     */
    public User(final EbicsPartner partner,
                final ObjectInputStream ois,
                final PasswordCallback passwordCallback)
            throws IOException, GeneralSecurityException, ClassNotFoundException {
        this.partner = partner;
        this.passwordCallback = passwordCallback;
        this.userId = ois.readUTF();
        this.name = ois.readUTF();
        this.isInitializedINI = ois.readBoolean();
        this.isInitializedHIA = ois.readBoolean();
        this.a005KeyPair = (KeyPair) ois.readObject();
        this.e002KeyPair = (KeyPair) ois.readObject();
        this.x002KeyPair = (KeyPair) ois.readObject();
        ois.close();
    }

    /**
     * Creates new certificates for a user.
     */
    private void createUserKeyPairs() throws GeneralSecurityException, IOException {
        final UserKeyManager manager = new UserKeyManager(this);
        manager.create();
    }

    @Override
    public void save(final ObjectOutputStream oos) throws IOException {
        oos.writeUTF(userId);
        oos.writeUTF(name);
        oos.writeBoolean(isInitializedINI);
        oos.writeBoolean(isInitializedHIA);
        oos.writeObject(a005KeyPair);
        oos.writeObject(e002KeyPair);
        oos.writeObject(x002KeyPair);
        oos.flush();
        oos.close();
        needSave = false;
    }

    /**
     * Has the users signature key been sent to the bank?
     *
     * @return True if the users signature key been sent to the bank
     */
    public boolean isInitializedINI() {
        return isInitializedINI;
    }

    /**
     * The users signature key has been sent to the bank.
     *
     * @param initializedINI transfer successful?
     */
    public void setInitializedINI(final boolean initializedINI) {
        this.isInitializedINI = initializedINI;
        needSave = true;
    }

    /**
     * Have the users authentication and encryption keys been sent to the bank?
     *
     * @return True if the users authentication and encryption keys been sent to the bank.
     */
    public boolean isInitializedHIA() {
        return isInitializedHIA;
    }

    /**
     * The users authentication and encryption keys have been sent to the bank.
     *
     * @param isInitializedHIA transfer successful?
     */
    public void setInitializedHIA(final boolean isInitializedHIA) {
        this.isInitializedHIA = isInitializedHIA;
        needSave = true;
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
     * Did any persistable attribute change since last load/save operation.
     *
     * @return True if the object needs to be saved.
     */
    public boolean needsSave() {
        return needSave;
    }

    @Override
    public void setA005KeyPair(final KeyPair a005KeyPair) {
        this.a005KeyPair = a005KeyPair;
        needSave = true;
    }

    @Override
    public void setE002KeyPair(final KeyPair e002KeyPair) {
        this.e002KeyPair = e002KeyPair;
        needSave = true;
    }

    @Override
    public void setX002KeyPair(final KeyPair x002KeyPair) {
        this.x002KeyPair = x002KeyPair;
        needSave = true;
    }

    @Override
    public RSAPublicKey getA005PublicKey() {
        return (RSAPublicKey) a005KeyPair.getPublic();
    }

    @Override
    public RSAPublicKey getE002PublicKey() {
        return (RSAPublicKey) e002KeyPair.getPublic();
    }

    @Override
    public RSAPublicKey getX002PublicKey() {
        return (RSAPublicKey) x002KeyPair.getPublic();
    }

    @Override
    public String getSecurityMedium() {
        return "0100";
    }

    @Override
    public EbicsPartner getPartner() {
        return partner;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasswordCallback getPasswordCallback() {
        return passwordCallback;
    }

    @Override
    public String getSaveName() {
        return userId + ".cer";
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
    @Override
    public byte[] authenticate(final byte[] digest) throws GeneralSecurityException {
        final Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(x002KeyPair.getPrivate());
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
     * algorithm. This signature is then put in a {@link UserSignature} XML object that
     * will be sent to the EBICS server.
     */
    @Override
    public byte[] sign(final byte[] digest) throws IOException, GeneralSecurityException {
        final Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(a005KeyPair.getPrivate());
        signature.update(removeOSSpecificChars(digest));
        return signature.sign();
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
    @Override
    public byte[] decrypt(final byte[] encryptedData, final byte[] transactionKey) {

        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, e002KeyPair.getPrivate());
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
    private byte[] decryptData(final byte[] input, final byte[] key)
            throws EbicsException {
        return Utils.decrypt(input, new SecretKeySpec(key, "AES"));
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final EbicsPartner partner;
    private final String userId;
    private final String name;
    private boolean isInitializedHIA;
    private boolean isInitializedINI;
    private final PasswordCallback passwordCallback;
    private transient boolean needSave;

    private KeyPair a005KeyPair;
    private KeyPair e002KeyPair;
    private KeyPair x002KeyPair;
}
