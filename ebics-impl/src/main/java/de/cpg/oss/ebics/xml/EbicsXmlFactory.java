package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import org.ebics.h004.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Optional;

public abstract class EbicsXmlFactory {

    final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    static JAXBElement<ProductElementType> unsecuredProduct(final Product product) {
        final ProductElementType productType = OBJECT_FACTORY.createProductElementType();
        productType.setLanguage(product.getLanguage());
        productType.setValue(product.getName());
        return OBJECT_FACTORY.createStaticHeaderBaseTypeProduct(productType);
    }

    private static StaticHeaderOrderDetailsType.OrderType orderType(final OrderType orderType) {
        final StaticHeaderOrderDetailsType.OrderType orderTypeXml =
                OBJECT_FACTORY.createStaticHeaderOrderDetailsTypeOrderType();
        orderTypeXml.setValue(orderType.name());
        return orderTypeXml;
    }

    static StaticHeaderType staticHeader(
            final EbicsSession session,
            final byte[] nonce,
            final int numSegments,
            final StaticHeaderOrderDetailsType orderDetails) {
        final StaticHeaderType staticHeader = staticHeader(session, nonce, orderDetails);
        staticHeader.setNumSegments(BigInteger.valueOf(numSegments));
        return staticHeader;
    }

    static StaticHeaderType staticHeader(
            final EbicsSession session,
            final byte[] nonce,
            final StaticHeaderOrderDetailsType orderDetails) {
        final StaticHeaderType staticHeader = OBJECT_FACTORY.createStaticHeaderType();
        staticHeader.setHostID(session.getHostId());
        staticHeader.setNonce(nonce);
        staticHeader.setPartnerID(session.getPartner().getId());
        staticHeader.setProduct(product(session.getProduct()));
        staticHeader.setSecurityMedium(session.getUser().getSecurityMedium());
        staticHeader.setUserID(session.getUser().getUserId());
        staticHeader.setTimestamp(OffsetDateTime.now());
        staticHeader.setOrderDetails(orderDetails);
        staticHeader.setBankPubKeyDigests(bankPubKeyDigests(session.getBank()));
        return staticHeader;
    }

    static StaticHeaderType staticHeader(final String hostId, final byte[] transactionId) {
        final StaticHeaderType staticHeader = OBJECT_FACTORY.createStaticHeaderType();
        staticHeader.setHostID(hostId);
        staticHeader.setTransactionID(transactionId);
        return staticHeader;
    }

    static MutableHeaderType mutableHeader(
            final TransactionPhaseType transactionPhase,
            final int segmentNumber,
            final boolean lastSegment) {
        return mutableHeader(transactionPhase, Optional.of(segmentNumber), Optional.of(lastSegment));
    }

    static MutableHeaderType mutableHeader(final TransactionPhaseType transactionPhase) {
        return mutableHeader(transactionPhase, Optional.empty(), Optional.empty());
    }

    static EbicsRequest.Header header(final MutableHeaderType mutable, final StaticHeaderType xstatic) {
        final EbicsRequest.Header header = OBJECT_FACTORY.createEbicsRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);
        return header;
    }

    static EbicsRequest.Body body(final DataTransferRequestType dataTransfer) {
        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setDataTransfer(dataTransfer);
        return body;
    }

    static EbicsRequest request(final EbicsConfiguration configuration, final EbicsRequest.Header header) {
        return request(configuration, header, OBJECT_FACTORY.createEbicsRequestBody());
    }

    static EbicsRequest request(
            final EbicsConfiguration configuration,
            final EbicsRequest.Header header,
            final EbicsRequest.Body body) {
        final EbicsRequest request = OBJECT_FACTORY.createEbicsRequest();
        request.setRevision(configuration.getRevision());
        request.setVersion(configuration.getVersion().name());
        request.setHeader(header);
        request.setBody(body);
        return request;
    }

    private static DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest(final EbicsEncryptionKey encryptionKey) {
        final DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest =
                OBJECT_FACTORY.createDataEncryptionInfoTypeEncryptionPubKeyDigest();
        encryptionPubKeyDigest.setVersion(encryptionKey.getVersion().name());
        encryptionPubKeyDigest.setAlgorithm(XmlUtil.SIGNATURE_METHOD);
        encryptionPubKeyDigest.setValue(encryptionKey.getDigest());
        return encryptionPubKeyDigest;
    }

    private static DataTransferRequestType.SignatureData signatureData(
            final EbicsUser user,
            final String partnerId,
            final byte[] toSign,
            final SecretKeySpec keySpec) {
        final DataTransferRequestType.SignatureData signatureData = OBJECT_FACTORY.createDataTransferRequestTypeSignatureData();
        signatureData.setAuthenticate(true);
        signatureData.setValue(CryptoUtil.encrypt(
                ZipUtil.compress(
                        XmlUtil.prettyPrint(
                                EbicsSignatureXmlFactory.userSignature(user, partnerId, toSign))),
                keySpec));
        return signatureData;
    }

    static StaticHeaderOrderDetailsType orderDetails(final OrderAttributeType orderAttribute, final OrderType orderType) {
        return orderDetails(
                orderAttribute,
                orderType,
                OBJECT_FACTORY.createStandardOrderParams(OBJECT_FACTORY.createStandardOrderParamsType()));
    }

    static StaticHeaderOrderDetailsType orderDetails(
            final OrderAttributeType orderAttribute,
            final OrderType orderType,
            final JAXBElement<?> orderParams) {
        final StaticHeaderOrderDetailsType orderDetails = OBJECT_FACTORY.createStaticHeaderOrderDetailsType();
        orderDetails.setOrderAttribute(orderAttribute);
        orderDetails.setOrderType(EbicsXmlFactory.orderType(orderType));
        orderDetails.setOrderParams(orderParams);
        return orderDetails;
    }

    static DataTransferRequestType dataTransferRequest(
            final EbicsSession session,
            final byte[] toSign,
            final SecretKeySpec keySpec,
            final byte[] transactionKey) {
        final DataTransferRequestType dataTransfer = OBJECT_FACTORY.createDataTransferRequestType();
        dataTransfer.setDataEncryptionInfo(dataEncryptionInfo(session.getBank().getEncryptionKey(), transactionKey));
        dataTransfer.setSignatureData(EbicsXmlFactory.signatureData(
                session.getUser(),
                session.getPartner().getId(),
                toSign,
                keySpec));
        return dataTransfer;
    }

    public static HIARequestOrderDataType hiaRequestOrderData(final EbicsSession session) throws EbicsException {
        final EncryptionPubKeyInfoType encryptionPubKeyInfo = OBJECT_FACTORY.createEncryptionPubKeyInfoType();
        encryptionPubKeyInfo.setEncryptionVersion(session.getUser().getEncryptionKey().getVersion().name());
        encryptionPubKeyInfo.setPubKeyValue(pubKeyValue(session.getUser().getEncryptionKey()));

        final AuthenticationPubKeyInfoType authenticationPubKeyInfo = OBJECT_FACTORY.createAuthenticationPubKeyInfoType();
        authenticationPubKeyInfo.setAuthenticationVersion(session.getUser().getAuthenticationKey().getVersion().name());
        authenticationPubKeyInfo.setPubKeyValue(pubKeyValue(session.getUser().getAuthenticationKey()));

        final HIARequestOrderDataType request = OBJECT_FACTORY.createHIARequestOrderDataType();
        request.setAuthenticationPubKeyInfo(authenticationPubKeyInfo);
        request.setEncryptionPubKeyInfo(encryptionPubKeyInfo);
        request.setPartnerID(session.getPartner().getId());
        request.setUserID(session.getUser().getId());

        return request;
    }

    public static EbicsUnsecuredRequest ebicsUnsecuredRequest(
            final EbicsSession session,
            final OrderType orderType,
            final byte[] orderData) {
        final UnsecuredReqOrderDetailsType orderDetails = OBJECT_FACTORY.createUnsecuredReqOrderDetailsType();
        orderDetails.setOrderAttribute("DZNNN");
        orderDetails.setOrderType(orderType.name());

        final UnsecuredRequestStaticHeaderType xstatic = OBJECT_FACTORY.createUnsecuredRequestStaticHeaderType();
        xstatic.setHostID(session.getHostId());
        xstatic.setUserID(session.getUser().getUserId());
        xstatic.setPartnerID(session.getPartner().getId());
        xstatic.setProduct(unsecuredProduct(session.getProduct()));
        xstatic.setOrderDetails(orderDetails);
        xstatic.setSecurityMedium(OrderType.HIA.equals(orderType) ? "0000" : session.getUser().getSecurityMedium());

        final EmptyMutableHeaderType mutable = OBJECT_FACTORY.createEmptyMutableHeaderType();

        final EbicsUnsecuredRequest.Header header = OBJECT_FACTORY.createEbicsUnsecuredRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);

        final EbicsUnsecuredRequest.Body.DataTransfer dataTransfer =
                OBJECT_FACTORY.createEbicsUnsecuredRequestBodyDataTransfer();
        dataTransfer.setOrderData(orderData(orderData));

        final EbicsUnsecuredRequest.Body body = OBJECT_FACTORY.createEbicsUnsecuredRequestBody();
        body.setDataTransfer(dataTransfer);

        final EbicsUnsecuredRequest unsecuredRequest = OBJECT_FACTORY.createEbicsUnsecuredRequest();
        unsecuredRequest.setHeader(header);
        unsecuredRequest.setBody(body);
        unsecuredRequest.setRevision(session.getConfiguration().getRevision());
        unsecuredRequest.setVersion(session.getConfiguration().getVersion().name());

        return unsecuredRequest;
    }

    private static JAXBElement<StaticHeaderType.Product> product(final Product product) {
        final StaticHeaderType.Product productXml = OBJECT_FACTORY.createStaticHeaderTypeProduct();
        productXml.setLanguage(product.getLanguage());
        productXml.setValue(product.getName());
        return OBJECT_FACTORY.createStaticHeaderTypeProduct(productXml);
    }

    private static StaticHeaderType.BankPubKeyDigests bankPubKeyDigests(final EbicsBank bank) {
        final StaticHeaderType.BankPubKeyDigests bankPubKeyDigests =
                OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigests();
        bankPubKeyDigests.setAuthentication(authentication(bank.getAuthenticationKey()));
        bankPubKeyDigests.setEncryption(encryption(bank.getEncryptionKey()));
        return bankPubKeyDigests;
    }

    private static <T extends Enum> PubKeyValueType pubKeyValue(final EbicsRsaKey<T> ebicsRsaKey) {
        final PubKeyValueType pubKeyValue = OBJECT_FACTORY.createPubKeyValueType();
        pubKeyValue.setRSAKeyValue(XmlSignatureFactory.rsaPublicKey(ebicsRsaKey.getPublicKey()));
        pubKeyValue.setTimeStamp(ebicsRsaKey.getCreationTime());
        return pubKeyValue;
    }

    private static StaticHeaderType.BankPubKeyDigests.Authentication authentication(
            final EbicsAuthenticationKey authenticationKey) {
        final StaticHeaderType.BankPubKeyDigests.Authentication authentication =
                OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsAuthentication();
        authentication.setVersion(authenticationKey.getVersion().name());
        authentication.setAlgorithm(XmlUtil.SIGNATURE_METHOD);
        authentication.setValue(authenticationKey.getDigest());
        return authentication;
    }

    private static StaticHeaderType.BankPubKeyDigests.Encryption encryption(final EbicsEncryptionKey encryptionKey) {
        final StaticHeaderType.BankPubKeyDigests.Encryption encryption =
                OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsEncryption();
        encryption.setVersion(encryptionKey.getVersion().name());
        encryption.setAlgorithm(XmlUtil.SIGNATURE_METHOD);
        encryption.setValue(encryptionKey.getDigest());
        return encryption;
    }

    private static MutableHeaderType mutableHeader(
            final TransactionPhaseType transactionPhase,
            final Optional<Integer> segmentNumber,
            final Optional<Boolean> lastSegment) {
        final MutableHeaderType mutable = OBJECT_FACTORY.createMutableHeaderType();
        mutable.setTransactionPhase(transactionPhase);
        if (segmentNumber.isPresent() && lastSegment.isPresent()) {
            final MutableHeaderType.SegmentNumber segmentNumberXml = OBJECT_FACTORY.createMutableHeaderTypeSegmentNumber();
            segmentNumberXml.setValue(BigInteger.valueOf(segmentNumber.get()));
            segmentNumberXml.setLastSegment(lastSegment.get());
            mutable.setSegmentNumber(OBJECT_FACTORY.createMutableHeaderTypeSegmentNumber(segmentNumberXml));
        }
        return mutable;
    }

    private static DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo(
            final EbicsEncryptionKey bankEncryptionKey,
            final byte[] transactionKey) {
        final DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo =
                OBJECT_FACTORY.createDataTransferRequestTypeDataEncryptionInfo();
        dataEncryptionInfo.setAuthenticate(true);
        dataEncryptionInfo.setEncryptionPubKeyDigest(
                EbicsXmlFactory.encryptionPubKeyDigest(bankEncryptionKey));
        dataEncryptionInfo.setTransactionKey(transactionKey);
        return dataEncryptionInfo;
    }

    private static EbicsUnsecuredRequest.Body.DataTransfer.OrderData orderData(final byte[] orderData) {
        final EbicsUnsecuredRequest.Body.DataTransfer.OrderData orderDataXml =
                OBJECT_FACTORY.createEbicsUnsecuredRequestBodyDataTransferOrderData();
        orderDataXml.setValue(orderData);
        return orderDataXml;
    }
}
