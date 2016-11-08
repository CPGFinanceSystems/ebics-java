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

import org.ebics.h004.*;
import org.kopi.ebics.certificate.KeyStoreManager;
import org.kopi.ebics.certificate.KeyUtil;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.interfaces.EbicsUser;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.Utils;
import org.kopi.ebics.xml.*;

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

    private final EbicsSession session;

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

        final HttpRequestSender sender = new HttpRequestSender(session);
        final INIRequestElement request = new INIRequestElement(session);
        final EbicsUnsecuredRequest unsecuredRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final int httpCode = sender.send(new ByteArrayContentFactory(xml));
        Utils.checkHttpCode(httpCode);
        final KeyManagementResponseElement response = new KeyManagementResponseElement(sender.getResponseBody());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "INIResponse");

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
        final HttpRequestSender sender = new HttpRequestSender(session);
        final HIARequestElement request = new HIARequestElement(session);
        final EbicsUnsecuredRequest unsecuredRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final int httpCode = sender.send(new ByteArrayContentFactory(xml));
        Utils.checkHttpCode(httpCode);
        final KeyManagementResponseElement response = new KeyManagementResponseElement(sender.getResponseBody());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HIAResponse");

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
        final HttpRequestSender sender = new HttpRequestSender(session);
        final HPBRequestElement request = new HPBRequestElement(session);
        final EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsNoPubKeyDigestsRequest.class, ebicsNoPubKeyDigestsRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final int httpCode = sender.send(new ByteArrayContentFactory(xml));
        Utils.checkHttpCode(httpCode);
        final KeyManagementResponseElement response = new KeyManagementResponseElement(sender.getResponseBody());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HBPResponse");
        final ContentFactory factory = new ByteArrayContentFactory(Utils.unzip(session.getUser().decrypt(response.getOrderData(), response.getTransactionKey())));
        final HPBResponseOrderDataElement orderData = new HPBResponseOrderDataElement(factory);
        final HPBResponseOrderDataType orderDataResponse = orderData.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(HPBResponseOrderDataType.class, orderDataResponse), orderData.getName());
        final KeyStoreManager keystoreManager = new KeyStoreManager();
        final String path = session.getConfiguration().getKeystoreDirectory(session.getUser());
        keystoreManager.load("", session.getUser().getPasswordCallback().getPassword());
        final RSAPublicKey e002PubKey = orderData.getBankE002PublicKeyData();
        final RSAPublicKey x002PubKey = orderData.getBankX002PublicKeyData();
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
        final SPRResponseElement response;
        final int httpCode;

        final HttpRequestSender sender = new HttpRequestSender(session);
        final SPRRequestElement request = new SPRRequestElement(session);
        final EbicsRequest ebicsRequest = request.build();
        final byte[] requestXml = XmlUtils.prettyPrint(EbicsRequest.class, ebicsRequest);
        session.getConfiguration().getTraceManager().trace(requestXml, request.getName());
        XmlUtils.validate(requestXml);
        httpCode = sender.send(new ByteArrayContentFactory(requestXml));
        Utils.checkHttpCode(httpCode);
        response = new SPRResponseElement(sender.getResponseBody());
        final EbicsResponse ebicsResponse = response.build();
        session.getConfiguration().getTraceManager().trace(EbicsResponse.class, ebicsResponse);
        response.report();
    }
}
