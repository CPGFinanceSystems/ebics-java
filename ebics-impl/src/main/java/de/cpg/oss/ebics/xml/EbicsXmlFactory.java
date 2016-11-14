package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.*;
import org.ebics.h004.*;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

abstract class EbicsXmlFactory {

    private final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    static JAXBElement<ProductElementType> unsecuredProduct(final Product product) {
        final ProductElementType productType = OBJECT_FACTORY.createProductElementType();
        productType.setLanguage(product.getLanguage());
        productType.setValue(product.getName());
        return OBJECT_FACTORY.createStaticHeaderBaseTypeProduct(productType);
    }

    static StaticHeaderOrderDetailsType.OrderType orderType(final OrderType orderType) {
        final StaticHeaderOrderDetailsType.OrderType orderTypeXml = OBJECT_FACTORY.createStaticHeaderOrderDetailsTypeOrderType();
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
        staticHeader.setTimestamp(LocalDateTime.now());
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

    private static JAXBElement<StaticHeaderType.Product> product(final Product product) {
        final StaticHeaderType.Product productXml = OBJECT_FACTORY.createStaticHeaderTypeProduct();
        productXml.setLanguage(product.getLanguage());
        productXml.setValue(product.getName());
        return OBJECT_FACTORY.createStaticHeaderTypeProduct(productXml);
    }

    private static StaticHeaderType.BankPubKeyDigests bankPubKeyDigests(final EbicsBank bank) {
        final StaticHeaderType.BankPubKeyDigests bankPubKeyDigests = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigests();
        bankPubKeyDigests.setAuthentication(authentication(bank.getAuthenticationKey()));
        bankPubKeyDigests.setEncryption(encryption(bank.getEncryptionKey()));
        return bankPubKeyDigests;
    }

    private static StaticHeaderType.BankPubKeyDigests.Authentication authentication(final EbicsAuthenticationKey authenticationKey) {
        final StaticHeaderType.BankPubKeyDigests.Authentication authentication = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsAuthentication();
        authentication.setVersion(authenticationKey.getVersion().name());
        authentication.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        authentication.setValue(authenticationKey.getDigest());
        return authentication;
    }

    private static StaticHeaderType.BankPubKeyDigests.Encryption encryption(final EbicsEncryptionKey encryptionKey) {
        final StaticHeaderType.BankPubKeyDigests.Encryption encryption = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsEncryption();
        encryption.setVersion(encryptionKey.getVersion().name());
        encryption.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
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
}
