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

import org.kopi.ebics.certificate.KeyStoreManager;
import org.kopi.ebics.certificate.KeyUtil;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;


/**
 * Everything that has to do with key handling.
 * If you have a totally new account use <code>sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 * @author Hachani
 */
class KeyManagement {

    /**
     * Constructs a new <code>KeyManagement</code> instance
     * with a given ebics session
     *
     * @param session the ebics session
     */
    public KeyManagement(final EbicsSession session) {
        this.session = session;
    }

    /**
     * Sends the user's signature key (A005) to the bank.
     * After successful operation the user is in state "initialized".
     *
     * @throws EbicsException server generated error message
     * @throws IOException    communication error
     */
    public EbicsUser sendINI() throws EbicsException, IOException {
        final INIRequestElement request;
        final KeyManagementResponseElement response;
        final HttpRequestSender sender;
        final int httpCode;

        sender = new HttpRequestSender(session);
        request = new INIRequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        response = new KeyManagementResponseElement(sender.getResponseBody(), "INIResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();

        session.getUser().setInitializedINI(true);
        return session.getUser();
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @throws IOException    communication error
     * @throws EbicsException server generated error message
     */
    public EbicsUser sendHIA() throws IOException, EbicsException {
        final HIARequestElement request;
        final KeyManagementResponseElement response;
        final HttpRequestSender sender;
        final int httpCode;

        sender = new HttpRequestSender(session);
        request = new HIARequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        response = new KeyManagementResponseElement(sender.getResponseBody(), "HIAResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();

        session.getUser().setInitializedHIA(true);
        return session.getUser();
    }

    /**
     * Sends encryption and authentication keys to the bank.
     * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
     * The keys will be activated immediately after successful completion of the transfer.
     *
     * @throws IOException              communication error
     * @throws GeneralSecurityException data decryption error
     * @throws EbicsException           server generated error message
     */
    public EbicsUser sendHPB() throws IOException, GeneralSecurityException, EbicsException {
        final HPBRequestElement request;
        final KeyManagementResponseElement response;
        final HttpRequestSender sender;
        final HPBResponseOrderDataElement orderData;
        final ContentFactory factory;
        final KeyStoreManager keystoreManager;
        final String path;
        final RSAPublicKey e002PubKey;
        final RSAPublicKey x002PubKey;
        final int httpCode;

        sender = new HttpRequestSender(session);
        request = new HPBRequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        response = new KeyManagementResponseElement(sender.getResponseBody(), "HBPResponse");
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
        factory = new ByteArrayContentFactory(Utils.unzip(session.getUser().decrypt(response.getOrderData(), response.getTransactionKey())));
        orderData = new HPBResponseOrderDataElement(factory);
        orderData.build();
        session.getConfiguration().getTraceManager().trace(orderData);
        keystoreManager = new KeyStoreManager();
        path = session.getConfiguration().getKeystoreDirectory(session.getUser());
        keystoreManager.load("", session.getUser().getPasswordCallback().getPassword());
        e002PubKey = orderData.getBankE002Certificate().map(ByteArrayInputStream::new).map(keystoreManager::getPublicKey)
                .orElse(orderData.getBankE002PublicKeyData()
                        .map(publicKeyData -> keystoreManager.getPublicKey(publicKeyData.getModulus(), publicKeyData.getExponent()))
                        .orElseThrow(() -> new EbicsException("Neither X.509 certificate data nor public key data supplied in HBP response for E002 key")));
        x002PubKey = orderData.getBankX002Certificate().map(ByteArrayInputStream::new).map(keystoreManager::getPublicKey)
                .orElse(orderData.getBankX002PublicKeyData()
                        .map(publicKeyData -> keystoreManager.getPublicKey(publicKeyData.getModulus(), publicKeyData.getExponent()))
                        .orElseThrow(() -> new EbicsException("Neither X.509 certificate data nor public key data supplied in HBP response for X002 key")));
        session.getUser().getPartner().getBank().setBankKeys(e002PubKey, x002PubKey);
        session.getUser().getPartner().getBank().setDigests(KeyUtil.getKeyDigest(e002PubKey), KeyUtil.getKeyDigest(x002PubKey));
        /* FIXME: Not possible to store public key entries in key stores - only private keys and trusted certificates are allowed
        keystoreManager.setPublicKeyEntry(session.getBankID() + "-E002", e002PubKey);
        keystoreManager.setPublicKeyEntry(session.getBankID() + "-X002", x002PubKey);
         */
        keystoreManager.save(new FileOutputStream(path + File.separator + session.getBankID() + ".p12"));
        return session.getUser();
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     *
     * @throws IOException    Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    public void lockAccess() throws IOException, EbicsException {
        final HttpRequestSender sender;
        final SPRRequestElement request;
        final SPRResponseElement response;
        final int httpCode;

        sender = new HttpRequestSender(session);
        request = new SPRRequestElement(session);
        request.build();
        request.validate();
        session.getConfiguration().getTraceManager().trace(request);
        httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
        Utils.checkHttpCode(httpCode);
        response = new SPRResponseElement(sender.getResponseBody());
        response.build();
        session.getConfiguration().getTraceManager().trace(response);
        response.report();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final EbicsSession session;
}
