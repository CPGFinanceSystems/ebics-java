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

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.ebics.h004.*;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.ContentFactory;
import org.kopi.ebics.io.ByteArrayContentFactory;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.utils.CryptoUtil;
import org.kopi.ebics.utils.HttpUtil;
import org.kopi.ebics.utils.KeyUtil;
import org.kopi.ebics.utils.ZipUtil;
import org.kopi.ebics.xml.*;

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
@Slf4j
abstract class KeyManagement {

    /**
     * Sends the user's signature key (A005) to the bank.
     * After successful operation the user is in state "initialized".
     *
     * @throws EbicsException server generated error message
     * @throws IOException    communication error
     */
    static EbicsUser sendINI(final EbicsSession session) throws EbicsException, IOException {

        final INIRequestElement request = new INIRequestElement(session);
        final EbicsUnsecuredRequest unsecuredRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final HttpResponse httpResponse = HttpUtil.sendAndReceive(session.getUser().getPartner().getBank(), new ByteArrayContentFactory(xml));
        HttpUtil.checkHttpCode(httpResponse.getStatusLine().getStatusCode());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(httpResponse);
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "INIResponse");

        return session.getUser().withInitializedINI(true);
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @throws IOException    communication error
     * @throws EbicsException server generated error message
     */
    static EbicsUser sendHIA(final EbicsSession session) throws IOException, EbicsException {
        final HIARequestElement request = new HIARequestElement(session);
        final EbicsUnsecuredRequest unsecuredRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final HttpResponse httpResponse = HttpUtil.sendAndReceive(session.getUser().getPartner().getBank(), new ByteArrayContentFactory(xml));
        HttpUtil.checkHttpCode(httpResponse.getStatusLine().getStatusCode());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(httpResponse);
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HIAResponse");

        return session.getUser().withInitializedHIA(true);
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
    static EbicsUser sendHPB(final EbicsSession session) throws IOException, GeneralSecurityException, EbicsException {
        final HPBRequestElement request = new HPBRequestElement(session);
        final EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest = request.build();
        final byte[] xml = XmlUtils.prettyPrint(EbicsNoPubKeyDigestsRequest.class, ebicsNoPubKeyDigestsRequest);
        session.getConfiguration().getTraceManager().trace(xml, request.getName());
        XmlUtils.validate(xml);
        final HttpResponse httpResponse = HttpUtil.sendAndReceive(session.getUser().getPartner().getBank(), new ByteArrayContentFactory(xml));
        HttpUtil.checkHttpCode(httpResponse.getStatusLine().getStatusCode());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(httpResponse);
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HBPResponse");
        final ContentFactory factory = new ByteArrayContentFactory(ZipUtil.uncompress(CryptoUtil.decrypt(
                response.getOrderData(),
                response.getTransactionKey(),
                session.getUser().getE002Key().getPrivate())));
        final HPBResponseOrderDataElement orderData = new HPBResponseOrderDataElement(factory);
        final HPBResponseOrderDataType orderDataResponse = orderData.build();
        session.getConfiguration().getTraceManager().trace(XmlUtils.prettyPrint(HPBResponseOrderDataType.class, orderDataResponse), orderData.getName());
        final RSAPublicKey e002PubKey = orderData.getBankE002PublicKeyData();
        final RSAPublicKey x002PubKey = orderData.getBankX002PublicKeyData();
        final EbicsBank bankWithKeys = session.getUser().getPartner().getBank()
                .withE002Key(e002PubKey)
                .withX002Key(x002PubKey)
                .withE002Digest(KeyUtil.getKeyDigest(e002PubKey))
                .withX002Digest(KeyUtil.getKeyDigest(x002PubKey));
        log.debug("Updated bank: {}", bankWithKeys);
        final EbicsPartner updatedPartner = session.getUser().getPartner()
                .withBank(bankWithKeys);
        log.debug("Updated partner: {}", bankWithKeys);
        return session.getUser().withPartner(updatedPartner);
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     *
     * @throws IOException    Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    static EbicsUser lockAccess(final EbicsSession session) throws IOException, EbicsException {
        final SPRRequestElement request = new SPRRequestElement(session);
        final EbicsRequest ebicsRequest = request.build();
        final byte[] requestXml = XmlUtils.prettyPrint(EbicsRequest.class, ebicsRequest);
        session.getConfiguration().getTraceManager().trace(requestXml, request.getName());
        XmlUtils.validate(requestXml);
        final HttpResponse httpResponse = HttpUtil.sendAndReceive(session.getUser().getPartner().getBank(), new ByteArrayContentFactory(requestXml));
        HttpUtil.checkHttpCode(httpResponse.getStatusLine().getStatusCode());
        final SPRResponseElement response = new SPRResponseElement(httpResponse);
        final EbicsResponse ebicsResponse = response.build();
        session.getConfiguration().getTraceManager().trace(EbicsResponse.class, ebicsResponse);
        response.report();
        return session.getUser();
    }
}
