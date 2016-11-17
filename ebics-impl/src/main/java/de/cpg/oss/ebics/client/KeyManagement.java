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

package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsBank;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ByteArrayContentFactory;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.io.InputStreamContentFactory;
import de.cpg.oss.ebics.utils.*;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.ebics.h004.*;
import org.ebics.s001.SignaturePubKeyOrderData;

import java.io.IOException;
import java.security.GeneralSecurityException;


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
     * Sends the user's signature key to the bank.
     * After successful operation the user is in state "initialized".
     *
     * @throws EbicsException server generated error message
     * @throws IOException    communication error
     */
    static EbicsUser sendINI(final EbicsSession session) throws EbicsException, IOException {

        final EbicsUnsecuredRequest unsecuredRequest = iniRequest(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getTraceManager().trace(EbicsUnsecuredRequest.class, unsecuredRequest, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(
                httpEntity,
                session.getMessageProvider());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getTraceManager().trace(XmlUtil.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "INIResponse", session.getUser());

        return session.getUser().withInitializedINI(true);
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @throws IOException    communication error
     * @throws EbicsException server generated error message
     */
    static EbicsUser sendHIA(final EbicsSession session) throws IOException, EbicsException {
        final EbicsUnsecuredRequest unsecuredRequest = hiaRequest(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsUnsecuredRequest.class, unsecuredRequest);
        session.getTraceManager().trace(EbicsUnsecuredRequest.class, unsecuredRequest, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(
                httpEntity,
                session.getMessageProvider());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getTraceManager().trace(XmlUtil.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HIAResponse", session.getUser());

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
    static EbicsBank sendHPB(final EbicsSession session) throws IOException, GeneralSecurityException, EbicsException {
        final EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest = HPBRequestElement.create(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsNoPubKeyDigestsRequest.class, ebicsNoPubKeyDigestsRequest);
        session.getTraceManager().trace(EbicsNoPubKeyDigestsRequest.class, ebicsNoPubKeyDigestsRequest, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final KeyManagementResponseElement response = new KeyManagementResponseElement(
                httpEntity,
                session.getMessageProvider());
        final EbicsKeyManagementResponse keyManagementResponse = response.build();
        session.getTraceManager().trace(XmlUtil.prettyPrint(EbicsKeyManagementResponse.class, keyManagementResponse), "HBPResponse", session.getUser());
        final ContentFactory factory = new ByteArrayContentFactory(ZipUtil.uncompress(CryptoUtil.decrypt(
                response.getOrderData(),
                response.getTransactionKey(),
                session.getUser().getEncryptionKey().getPrivateKey())));
        final HPBResponseOrderDataElement orderData = HPBResponseOrderDataElement.parse(factory);
        session.getTraceManager().trace(IOUtil.read(factory.getContent()), "HPBResponseOrderData", session.getUser());
        return session.getBank()
                .withAuthenticationKey(orderData.getBankAuthenticationKey())
                .withEncryptionKey(orderData.getBankEncryptionKey());
    }

    static EbicsBank sendHPD(final EbicsSession session) throws EbicsException, IOException {
        final EbicsRequest ebicsRequest = new HPDRequestElement().create(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final EbicsResponseElement responseElement = EbicsResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        session.getTraceManager().trace(XmlUtil.prettyPrint(EbicsResponse.class, responseElement.getResponse()), "HBDResponse", session.getUser());
        responseElement.report(session.getMessageProvider());
        final ContentFactory factory = new ByteArrayContentFactory(ZipUtil.uncompress(CryptoUtil.decrypt(
                responseElement.getOrderData(),
                responseElement.getTransactionKey(),
                session.getUser().getEncryptionKey().getPrivateKey())));
        final HPDResponseOrderDataElement orderData = HPDResponseOrderDataElement.parse(factory);
        session.getTraceManager().trace(IOUtil.read(factory.getContent()), "HPDResponseOrderData", session.getUser());

        if (orderData.isDownloadableOrderDataSupported()) {
            return sendHAA(session.withBank(session.getBank().withName(orderData.getBankName())));
        }

        return session.getBank().withName(orderData.getBankName());
    }

    private static EbicsBank sendHAA(final EbicsSession session) throws EbicsException, IOException {
        final EbicsRequest ebicsRequest = new HAARequestElement().create(session);
        final byte[] xml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        XmlUtil.validate(xml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(xml),
                session.getMessageProvider());
        final EbicsResponseElement responseElement = EbicsResponseElement.parse(InputStreamContentFactory.of(httpEntity));
        session.getTraceManager().trace(XmlUtil.prettyPrint(EbicsResponse.class, responseElement.getResponse()), "HAAResponse", session.getUser());
        responseElement.report(session.getMessageProvider());
        final ContentFactory factory = new ByteArrayContentFactory(ZipUtil.uncompress(CryptoUtil.decrypt(
                responseElement.getOrderData(),
                responseElement.getTransactionKey(),
                session.getUser().getEncryptionKey().getPrivateKey())));
        final HAAResponseOrderDataElement orderData = HAAResponseOrderDataElement.parse(factory);
        session.getTraceManager().trace(IOUtil.read(factory.getContent()), "HAAResponseOrderData", session.getUser());

        return session.getBank().withSupportedOrderTypes(orderData.getSupportedOrderTypes());
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     *
     * @throws IOException    Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    static EbicsUser lockAccess(final EbicsSession session) throws IOException, EbicsException {
        final EbicsRequest ebicsRequest = new SPRRequestElement().create(session);
        final byte[] requestXml = XmlUtil.prettyPrint(EbicsRequest.class, ebicsRequest);
        session.getTraceManager().trace(EbicsRequest.class, ebicsRequest, session.getUser());
        XmlUtil.validate(requestXml);
        final HttpEntity httpEntity = HttpUtil.sendAndReceive(
                session.getBank(),
                new ByteArrayContentFactory(requestXml),
                session.getMessageProvider());
        final EbicsResponseElement responseElement = EbicsResponseElement.parse(new InputStreamContentFactory(httpEntity.getContent()));
        final EbicsResponse ebicsResponse = responseElement.getResponse();
        session.getTraceManager().trace(EbicsResponse.class, ebicsResponse, session.getUser());
        responseElement.report(session.getMessageProvider());
        return session.getUser();
    }

    private static EbicsUnsecuredRequest iniRequest(final EbicsSession session) throws EbicsException {
        return EbicsXmlFactory.ebicsUnsecuredRequest(
                session,
                OrderType.INI,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        SignaturePubKeyOrderData.class,
                        EbicsSignatureXmlFactory.signaturePubKeyOrderData(session))));
    }

    private static EbicsUnsecuredRequest hiaRequest(final EbicsSession session) throws EbicsException {
        return EbicsXmlFactory.ebicsUnsecuredRequest(
                session,
                OrderType.HIA,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        HIARequestOrderDataType.class,
                        EbicsXmlFactory.hiaRequestOrderData(session))));
    }
}
