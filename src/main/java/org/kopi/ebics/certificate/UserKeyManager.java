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

import org.kopi.ebics.interfaces.EbicsUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

/**
 * Simple manager for EBICS certificates.
 *
 * @author hacheni
 */
public class UserKeyManager {

    private final EbicsUser user;

    public UserKeyManager(final EbicsUser user) {
        this.user = user;
    }

    /**
     * Creates the RSA key pairs for the user
     */
    public EbicsUser create() throws GeneralSecurityException, IOException {
        user.setA005KeyPair(createRsaKeyPair());
        user.setX002KeyPair(createRsaKeyPair());
        user.setE002KeyPair(createRsaKeyPair());

        return user;
    }

    private static KeyPair createRsaKeyPair() throws GeneralSecurityException, IOException {
        return KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);
    }
}
