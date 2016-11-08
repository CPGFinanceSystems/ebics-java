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

package org.kopi.ebics.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;


/**
 * Things an EBICS user must be able to perform.
 *
 * @author Hachani
 */
public interface EbicsUser extends Savable {

    /**
     * Returns the public part of the signature key.
     *
     * @return the public part of the signature key.
     */
    RSAPublicKey getA005PublicKey();

    /**
     * Returns the public part of the encryption key.
     *
     * @return the public part of the encryption key.
     */
    RSAPublicKey getE002PublicKey();

    /**
     * Return the public part of the transport authentication key.
     *
     * @return the public part of the transport authentication key.
     */
    RSAPublicKey getX002PublicKey();

    /**
     * Sets the signature key pair.
     */
    void setA005KeyPair(KeyPair a005KeyPair);

    /**
     * Sets the authentication key pair.
     */
    void setX002KeyPair(KeyPair x002KeyPair);

    /**
     * Sets the encryption key pair.
     */
    void setE002KeyPair(KeyPair e002KeyPair);

    /**
     * Returns the type to security medium used to store the A005 key.
     *
     * @return the type to security medium used to store the A005 key.
     */
    String getSecurityMedium();

    /**
     * Returns the customer in whose name we operate.
     *
     * @return the customer in whose name we operate.
     */
    EbicsPartner getPartner();

    /**
     * Returns the (bank provided) user id.
     *
     * @return the (bank provided) user id.
     */
    String getUserId();

    /**
     * Returns the user name.
     *
     * @return the user name.
     */
    String getName();

    /**
     * Returns the password callback handler for the current user.
     *
     * @return the password callback handler.
     */
    PasswordCallback getPasswordCallback();

    /**
     * Signs the given digest with the private X002 key.
     *
     * @param digest the given digest
     * @return the signature.
     */
    byte[] authenticate(byte[] digest) throws GeneralSecurityException;

    /**
     * Signs the given digest with the private A005 key.
     *
     * @return the signature
     */
    byte[] sign(byte[] digest) throws IOException, GeneralSecurityException;

    /**
     * Uses the E001 key to decrypt the given secret key.
     *
     * @param encryptedKey   the given secret key
     * @param transactionKey a given transaction key
     * @return the decrypted key;
     */
    byte[] decrypt(byte[] encryptedKey, byte[] transactionKey);

    boolean isInitializedINI();

    void setInitializedINI(boolean value);

    boolean isInitializedHIA();

    void setInitializedHIA(boolean value);
}
