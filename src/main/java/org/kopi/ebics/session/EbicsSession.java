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

package org.kopi.ebics.session;

import org.kopi.ebics.client.EbicsUser;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsConfiguration;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;


/**
 * Communication hub for EBICS.
 *
 * @author Hachani
 */
public class EbicsSession {

    private final EbicsUser user;
    private final EbicsConfiguration configuration;
    private final Product product;
    private final Map<String, String> parameters;

    /**
     * Constructs a new ebics session
     *
     * @param user          ebics user
     * @param configuration ebics client configuration
     */
    public EbicsSession(final EbicsUser user, final EbicsConfiguration configuration, final Product product) {
        this.user = user;
        this.configuration = configuration;
        this.product = product;
        this.parameters = new HashMap<>();
    }

    /**
     * Returns the banks encryption key.
     * The key will be fetched automatically form the bank if needed.
     *
     * @return the banks encryption key.
     * @throws IOException    Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    public RSAPublicKey getBankE002Key() throws IOException, EbicsException {
        return (RSAPublicKey) user.getPartner().getBank().getE002Key();
    }

    /**
     * Returns the banks authentication key.
     * The key will be fetched automatically form the bank if needed.
     *
     * @return the banks authentication key.
     * @throws IOException    Communication error during key retrieval.
     * @throws EbicsException Server error message generated during key retrieval.
     */
    public RSAPublicKey getBankX002Key() throws IOException, EbicsException {
        return (RSAPublicKey) user.getPartner().getBank().getX002Key();
    }

    /**
     * Returns the bank id.
     *
     * @return the bank id.
     * @throws EbicsException
     */
    public String getBankID() throws EbicsException {
        return user.getPartner().getBank().getHostId();
    }

    /**
     * Return the session user.
     *
     * @return the session user.
     */
    public EbicsUser getUser() {
        return user;
    }

    /**
     * Returns the client application configuration.
     *
     * @return the client application configuration.
     */
    public EbicsConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Adds a session parameter to use it in the transfer process.
     *
     * @param key   the parameter key
     * @param value the parameter value
     */
    public void addSessionParam(final String key, final String value) {
        parameters.put(key, value);
    }

    /**
     * Retrieves a session parameter using its key.
     *
     * @param key the parameter key
     * @return the session parameter
     */
    public String getSessionParam(final String key) {
        if (key == null) {
            return null;
        }

        return parameters.get(key);
    }
}
