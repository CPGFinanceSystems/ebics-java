package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsBank;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsNoPubKeyDigestsRequest;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.h004.HIARequestOrderDataType;
import org.ebics.s001.SignaturePubKeyOrderData;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.ebicsUnsecuredRequest;
import static de.cpg.oss.ebics.xml.EbicsXmlFactory.hiaRequestOrderData;

/**
 * Everything that has to do with key handling.
 * If you have a totally new account use <code>sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
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
        final OrderType orderType = OrderType.INI;
        final EbicsUnsecuredRequest unsecuredRequest = ebicsUnsecuredRequest(
                session,
                orderType,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        SignaturePubKeyOrderData.class,
                        EbicsSignatureXmlFactory.signaturePubKeyOrderData(session))));

        ClientUtil.requestExchange(session, EbicsUnsecuredRequest.class, unsecuredRequest,
                KeyManagementResponseElement::parse, orderType.name());

        return session.getUser().withInitializedINI(true);
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @throws IOException    communication error
     * @throws EbicsException server generated error message
     */
    static EbicsUser sendHIA(final EbicsSession session) throws IOException, EbicsException {
        final OrderType orderType = OrderType.HIA;
        final EbicsUnsecuredRequest unsecuredRequest = ebicsUnsecuredRequest(
                session,
                orderType,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        HIARequestOrderDataType.class,
                        hiaRequestOrderData(session))));

        ClientUtil.requestExchange(session, EbicsUnsecuredRequest.class, unsecuredRequest,
                KeyManagementResponseElement::parse, orderType.name());

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
        final String baseElementName = ebicsNoPubKeyDigestsRequest.getHeader().getStatic().getOrderDetails().getOrderType();
        final KeyManagementResponseElement response = ClientUtil.requestExchange(session,
                EbicsNoPubKeyDigestsRequest.class, ebicsNoPubKeyDigestsRequest, KeyManagementResponseElement::parse,
                baseElementName);
        final HPBResponseOrderDataElement orderData = ClientUtil.orderDataElement(session,
                response, HPBResponseOrderDataElement::parse, baseElementName);

        return session.getBank()
                .withAuthenticationKey(orderData.getBankAuthenticationKey())
                .withEncryptionKey(orderData.getBankEncryptionKey());
    }

    static EbicsBank sendHPD(final EbicsSession session) throws EbicsException, IOException {
        final OrderType orderType = OrderType.HPD;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);
        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HPDResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HPDResponseOrderDataElement::parse, orderType.name());

        if (orderData.isDownloadableOrderDataSupported()) {
            return sendHAA(session.withBank(session.getBank().withName(orderData.getBankName())));
        }

        return session.getBank().withName(orderData.getBankName());
    }

    private static EbicsBank sendHAA(final EbicsSession session) throws EbicsException, IOException {
        final OrderType orderType = OrderType.HAA;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);
        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HAAResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HAAResponseOrderDataElement::parse, orderType.name());

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
        final EbicsRequest ebicsRequest = EbicsRequestElement.create(session, SPRRequestElement::new);

        ClientUtil.requestExchange(session, ebicsRequest);

        return session.getUser();
    }
}
