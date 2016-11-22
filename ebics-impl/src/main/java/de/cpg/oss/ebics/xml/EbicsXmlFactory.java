package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import javaslang.control.Option;
import org.ebics.h004.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

public abstract class EbicsXmlFactory {

    final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    static JAXBElement<ProductElementType> unsecuredProduct(final Product product) {
        return OBJECT_FACTORY.createStaticHeaderBaseTypeProduct(ProductElementType.builder()
                .withLanguage(product.getLanguage())
                .withValue(product.getName())
                .build());
    }

    private static StaticHeaderOrderDetailsType.OrderType orderType(final OrderType orderType) {
        return StaticHeaderOrderDetailsType.OrderType.builder()
                .withValue(orderType.name())
                .build();
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
        return StaticHeaderType.builder()
                .withHostID(session.getHostId())
                .withNonce(nonce)
                .withPartnerID(session.getPartner().getId())
                .withProduct(product(session.getProduct()))
                .withSecurityMedium(session.getUser().getSecurityMedium())
                .withUserID(session.getUser().getUserId())
                .withTimestamp(OffsetDateTime.now())
                .withOrderDetails(orderDetails)
                .withBankPubKeyDigests(bankPubKeyDigests(session.getBank()))
                .build();
    }

    static StaticHeaderType staticHeader(final String hostId, final byte[] transactionId) {
        return StaticHeaderType.builder()
                .withHostID(hostId)
                .withTransactionID(transactionId)
                .build();
    }

    static MutableHeaderType mutableHeader(
            final TransactionPhaseType transactionPhase,
            final int segmentNumber,
            final boolean lastSegment) {
        return mutableHeader(transactionPhase, Option.of(segmentNumber), Option.of(lastSegment));
    }

    static MutableHeaderType mutableHeader(final TransactionPhaseType transactionPhase) {
        return mutableHeader(transactionPhase, Option.none(), Option.none());
    }

    static EbicsRequest.Header header(final MutableHeaderType mutable, final StaticHeaderType xstatic) {
        return EbicsRequest.Header.builder()
                .withAuthenticate(true)
                .withMutable(mutable)
                .withStatic(xstatic)
                .build();
    }

    static EbicsRequest.Body body(final DataTransferRequestType dataTransfer) {
        return EbicsRequest.Body.builder()
                .withDataTransfer(dataTransfer)
                .build();
    }

    static EbicsRequest request(final EbicsConfiguration configuration, final EbicsRequest.Header header) {
        return request(configuration, header, EbicsRequest.Body.builder().build());
    }

    static EbicsRequest request(
            final EbicsConfiguration configuration,
            final EbicsRequest.Header header,
            final EbicsRequest.Body body) {
        return EbicsRequest.builder()
                .withRevision(configuration.getRevision())
                .withVersion(configuration.getVersion().name())
                .withHeader(header)
                .withBody(body)
                .build();
    }

    private static DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest(final EbicsEncryptionKey encryptionKey) {
        return DataEncryptionInfoType.EncryptionPubKeyDigest.builder()
                .withVersion(encryptionKey.getVersion().name())
                .withAlgorithm(XmlUtil.SIGNATURE_METHOD)
                .withValue(encryptionKey.getDigest())
                .build();
    }

    private static DataTransferRequestType.SignatureData signatureData(
            final EbicsUser user,
            final String partnerId,
            final Supplier<byte[]> signatureSupplier,
            final SecretKeySpec keySpec) {
        return DataTransferRequestType.SignatureData.builder()
                .withAuthenticate(true)
                .withValue(CryptoUtil.encrypt(
                        ZipUtil.compress(
                                XmlUtil.prettyPrint(
                                        EbicsSignatureXmlFactory.userSignature(user, partnerId, signatureSupplier))),
                        keySpec))
                .build();
    }

    static StaticHeaderOrderDetailsType orderDetails(final OrderAttributeType orderAttribute, final OrderType orderType) {
        return orderDetails(
                orderAttribute,
                orderType,
                OBJECT_FACTORY.createStandardOrderParams(StandardOrderParamsType.builder().build()));
    }

    static StaticHeaderOrderDetailsType orderDetails(
            final OrderAttributeType orderAttribute,
            final OrderType orderType,
            final JAXBElement<?> orderParams) {
        return StaticHeaderOrderDetailsType.builder()
                .withOrderAttribute(orderAttribute)
                .withOrderType(EbicsXmlFactory.orderType(orderType))
                .withOrderParams(orderParams)
                .build();
    }

    static DataTransferRequestType dataTransferRequest(
            final EbicsSession session,
            final byte[] message,
            final SecretKeySpec keySpec,
            final byte[] transactionKey) {
        return dataTransferRequest(session, () -> {
            try {
                return CryptoUtil.signMessage(message, session.getUser().getSignatureKey());
            } catch (final IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }, keySpec, transactionKey);
    }

    static DataTransferRequestType dataTransferRequestWithDigest(
            final EbicsSession session,
            final byte[] digest,
            final byte[] nonce) throws EbicsException {
        return dataTransferRequest(session, () -> {
            try {
                return CryptoUtil.signHash(digest, session.getUser().getSignatureKey());
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }, new SecretKeySpec(nonce, "AES"), CryptoUtil.generateTransactionKey(nonce, session.getBankEncryptionKey()));
    }

    static DataTransferRequestType dataTransferRequest(
            final EbicsSession session,
            final Supplier<byte[]> signatureSupplier,
            final SecretKeySpec keySpec,
            final byte[] transactionKey) {
        return DataTransferRequestType.builder()
                .withDataEncryptionInfo(dataEncryptionInfo(session.getBank().getEncryptionKey(), transactionKey))
                .withSignatureData(EbicsXmlFactory.signatureData(
                        session.getUser(),
                        session.getPartner().getId(),
                        signatureSupplier,
                        keySpec))
                .build();
    }

    static Parameter stringParameter(final String name, final String value) {
        return Parameter.builder()
                .withName(name)
                .withValue(Parameter.Value.builder()
                        .withType("String")
                        .withValue(value)
                        .build())
                .build();
    }

    public static HIARequestOrderDataType hiaRequestOrderData(final EbicsSession session) throws EbicsException {
        return HIARequestOrderDataType.builder()
                .withAuthenticationPubKeyInfo(AuthenticationPubKeyInfoType.builder()
                        .withAuthenticationVersion(session.getUser().getAuthenticationKey().getVersion().name())
                        .withPubKeyValue(pubKeyValue(session.getUser().getAuthenticationKey()))
                        .build())
                .withEncryptionPubKeyInfo(EncryptionPubKeyInfoType.builder()
                        .withEncryptionVersion(session.getUser().getEncryptionKey().getVersion().name())
                        .withPubKeyValue(pubKeyValue(session.getUser().getEncryptionKey()))
                        .build())
                .withPartnerID(session.getPartner().getId())
                .withUserID(session.getUser().getId())
                .build();
    }

    public static EbicsUnsecuredRequest ebicsUnsecuredRequest(
            final EbicsSession session,
            final OrderType orderType,
            final byte[] orderData) {
        return EbicsUnsecuredRequest.builder()
                .withHeader(EbicsUnsecuredRequest.Header.builder()
                        .withAuthenticate(true)
                        .withMutable(EmptyMutableHeaderType.builder().build())
                        .withStatic(UnsecuredRequestStaticHeaderType.builder()
                                .withHostID(session.getHostId())
                                .withUserID(session.getUser().getUserId())
                                .withPartnerID(session.getPartner().getId())
                                .withProduct(unsecuredProduct(session.getProduct()))
                                .withOrderDetails(UnsecuredReqOrderDetailsType.builder()
                                        .withOrderAttribute("DZNNN")
                                        .withOrderType(orderType.name())
                                        .build())
                                .withSecurityMedium(OrderType.HIA.equals(orderType)
                                        ? "0000"
                                        : session.getUser().getSecurityMedium())
                                .build())
                        .build())
                .withBody(EbicsUnsecuredRequest.Body.builder()
                        .withDataTransfer(EbicsUnsecuredRequest.Body.DataTransfer.builder()
                                .withOrderData(orderData(orderData))
                                .build())
                        .build())
                .withRevision(session.getConfiguration().getRevision())
                .withVersion(session.getConfiguration().getVersion().name())
                .build();
    }

    private static JAXBElement<StaticHeaderType.Product> product(final Product product) {
        return OBJECT_FACTORY.createStaticHeaderTypeProduct(StaticHeaderType.Product.builder()
                .withLanguage(product.getLanguage())
                .withValue(product.getName())
                .build());
    }

    private static StaticHeaderType.BankPubKeyDigests bankPubKeyDigests(final EbicsBank bank) {
        return StaticHeaderType.BankPubKeyDigests.builder()
                .withAuthentication(authentication(bank.getAuthenticationKey()))
                .withEncryption(encryption(bank.getEncryptionKey()))
                .build();
    }

    private static <T extends Enum> PubKeyValueType pubKeyValue(final EbicsRsaKey<T> ebicsRsaKey) {
        return PubKeyValueType.builder()
                .withRSAKeyValue(XmlSignatureFactory.rsaPublicKey(ebicsRsaKey.getPublicKey()))
                .withTimeStamp(ebicsRsaKey.getCreationTime())
                .build();
    }

    private static StaticHeaderType.BankPubKeyDigests.Authentication authentication(
            final EbicsAuthenticationKey authenticationKey) {
        return StaticHeaderType.BankPubKeyDigests.Authentication.builder()
                .withVersion(authenticationKey.getVersion().name())
                .withAlgorithm(XmlUtil.SIGNATURE_METHOD)
                .withValue(authenticationKey.getDigest())
                .build();
    }

    private static StaticHeaderType.BankPubKeyDigests.Encryption encryption(final EbicsEncryptionKey encryptionKey) {
        return StaticHeaderType.BankPubKeyDigests.Encryption.builder()
                .withVersion(encryptionKey.getVersion().name())
                .withAlgorithm(XmlUtil.SIGNATURE_METHOD)
                .withValue(encryptionKey.getDigest())
                .build();
    }

    private static MutableHeaderType mutableHeader(
            final TransactionPhaseType transactionPhase,
            final Option<Integer> segmentNumber,
            final Option<Boolean> lastSegment) {
        final MutableHeaderType.Builder mutableBuilder = MutableHeaderType.builder()
                .withTransactionPhase(transactionPhase);
        if (segmentNumber.isDefined() && lastSegment.isDefined()) {
            return mutableBuilder.withSegmentNumber(
                    OBJECT_FACTORY.createMutableHeaderTypeSegmentNumber(MutableHeaderType.SegmentNumber.builder()
                            .withValue(BigInteger.valueOf(segmentNumber.get()))
                            .withLastSegment(lastSegment.get())
                            .build()))
                    .build();
        }
        return mutableBuilder.build();
    }

    private static DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo(
            final EbicsEncryptionKey bankEncryptionKey,
            final byte[] transactionKey) {
        return DataTransferRequestType.DataEncryptionInfo.builder()
                .withAuthenticate(true)
                .withEncryptionPubKeyDigest(EbicsXmlFactory.encryptionPubKeyDigest(bankEncryptionKey))
                .withTransactionKey(transactionKey)
                .build();
    }

    private static EbicsUnsecuredRequest.Body.DataTransfer.OrderData orderData(final byte[] orderData) {
        return EbicsUnsecuredRequest.Body.DataTransfer.OrderData.builder()
                .withValue(orderData)
                .build();
    }
}
