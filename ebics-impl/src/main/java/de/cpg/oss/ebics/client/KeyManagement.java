package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import de.cpg.oss.ebics.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h000.EbicsHEVRequest;
import org.ebics.h004.EbicsNoPubKeyDigestsRequest;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.h004.HIARequestOrderDataType;
import org.ebics.s001.SignaturePubKeyOrderData;

import java.io.IOException;

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
     */
    static EbicsUser sendINI(final EbicsSession session) throws EbicsException {
        final OrderType orderType = OrderType.INI;
        final EbicsUnsecuredRequest unsecuredRequest = ebicsUnsecuredRequest(
                session,
                orderType,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        SignaturePubKeyOrderData.class,
                        EbicsSignatureXmlFactory.signaturePubKeyOrderData(session))));

        ClientUtil.requestExchange(session, EbicsUnsecuredRequest.class, unsecuredRequest,
                KeyManagementResponseElement::parse, orderType.name());

        final EbicsUser iniSentUser = session.getUser().withStatus(UserStatus.NEW.equals(session.getUser().getStatus())
                ? UserStatus.PARTLY_INITIALIZED_INI
                : UserStatus.INITIALIZED);
        try {
            session.getPersistenceProvider().save(EbicsUser.class, iniSentUser);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return iniSentUser;
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     *
     * @throws EbicsException server generated error message
     */
    static EbicsUser sendHIA(final EbicsSession session) throws EbicsException {
        final OrderType orderType = OrderType.HIA;
        final EbicsUnsecuredRequest unsecuredRequest = ebicsUnsecuredRequest(
                session,
                orderType,
                ZipUtil.compress(XmlUtil.prettyPrint(
                        HIARequestOrderDataType.class,
                        hiaRequestOrderData(session))));

        ClientUtil.requestExchange(session, EbicsUnsecuredRequest.class, unsecuredRequest,
                KeyManagementResponseElement::parse, orderType.name());

        final EbicsUser hiaSentUser = session.getUser().withStatus(UserStatus.NEW.equals(session.getUser().getStatus())
                ? UserStatus.PARTLY_INITIALIZED_HIA
                : UserStatus.INITIALIZED);

        try {
            session.getPersistenceProvider().save(EbicsUser.class, hiaSentUser);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return hiaSentUser;
    }

    /**
     * Sends encryption and authentication keys to the bank.
     * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
     * The keys will be activated immediately after successful completion of the transfer.
     *
     * @throws EbicsException server generated error message
     */
    static EbicsBank getBankPublicKeys(final EbicsSession session) throws EbicsException {
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

    static EbicsSession collectInformation(final EbicsSession session) throws EbicsException {
        EbicsBank updatedBank = sendHEV(session);

        final OrderType orderType = OrderType.HPD;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HPDResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HPDResponseOrderDataElement::parse, orderType.name());
        updatedBank = updatedBank.withName(orderData.getBankName());

        if (orderData.isDownloadableOrderDataSupported()) {
            updatedBank = sendHAA(session.withBank(updatedBank));
        }

        EbicsPartner updatedPartner = session.getPartner();
        EbicsUser updatedUser = session.getUser();
        if (orderData.isClientDataDownloadSupported()) {
            updatedPartner = sendHKD(session.withBank(updatedBank));
            updatedUser = sendHTD(session.withBank(updatedBank).withPartner(updatedPartner));
        }

        return session.withBank(updatedBank)
                .withPartner(updatedPartner)
                .withUser(updatedUser);
    }

    private static EbicsBank sendHEV(final EbicsSession session) throws EbicsException {
        final EbicsHEVRequest hevRequest = EbicsHEVRequest.builder()
                .withHostID(session.getHostId())
                .build();

        final HEVResponseElement responseElement = ClientUtil.requestExchange(session, EbicsHEVRequest.class,
                hevRequest, HEVResponseElement::parse, OrderType.HEV.name());

        return session.getBank().withSupportedEbicsVersions(responseElement.getSupportedEbicsVersions());
    }

    private static EbicsBank sendHAA(final EbicsSession session) throws EbicsException {
        final OrderType orderType = OrderType.HAA;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);
        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HAAResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HAAResponseOrderDataElement::parse, orderType.name());

        return session.getBank().withSupportedOrderTypes(orderData.getSupportedOrderTypes());
    }

    private static EbicsPartner sendHKD(final EbicsSession session) throws EbicsException {
        final OrderType orderType = OrderType.HKD;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HKDResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HKDResponseOrderDataElement::parse, orderType.name());

        return session.getPartner().withBankAccounts(orderData.getBankAccounts());
    }

    private static EbicsUser sendHTD(final EbicsSession session) throws EbicsException {
        final OrderType orderType = OrderType.HTD;
        final EbicsRequest ebicsRequest = EbicsRequestElement.createSigned(session, orderType);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HTDResponseOrderDataElement orderData = ClientUtil.orderDataElement(session, responseElement,
                HTDResponseOrderDataElement::parse, orderType.name());

        return session.getUser()
                .withStatus(orderData.getUserStatus())
                .withPermittedOrderTypes(orderData.getPermittedUserOrderTypes());
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

        return session.getUser().withStatus(UserStatus.SUSPENDED_BY_CUSTOMER);
    }
}
