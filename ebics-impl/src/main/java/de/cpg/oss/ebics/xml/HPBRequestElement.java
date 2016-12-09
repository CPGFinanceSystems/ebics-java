package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsConfiguration;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.*;

import java.time.OffsetDateTime;

public abstract class HPBRequestElement {

    public static EbicsNoPubKeyDigestsRequest create(final EbicsSession session) {
        final EbicsNoPubKeyDigestsRequest request = ebicsNoPubKeyDigestsRequest(session);

        request.setAuthSignature(XmlSignatureFactory.signatureType(
                XmlUtil.digest(EbicsNoPubKeyDigestsRequest.class, request)));
        request.getAuthSignature().getSignatureValue().setValue(
                XmlUtil.sign(EbicsNoPubKeyDigestsRequest.class, request, session.getUser()));

        return request;
    }

    private static EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest(final EbicsSession session) {
        return request(
                session.getConfiguration(),
                header(staticHeader(session, orderDetails())));
    }

    private static EbicsNoPubKeyDigestsRequest request(final EbicsConfiguration configuration,
                                                       final EbicsNoPubKeyDigestsRequest.Header header) {
        return EbicsNoPubKeyDigestsRequest.builder()
                .withRevision(configuration.getRevision())
                .withVersion(configuration.getVersion().name())
                .withHeader(header)
                .withBody(EbicsNoPubKeyDigestsRequest.Body.builder().build())
                .build();
    }

    private static EbicsNoPubKeyDigestsRequest.Header header(final NoPubKeyDigestsRequestStaticHeaderType staticHeader) {
        return EbicsNoPubKeyDigestsRequest.Header.builder()
                .withAuthenticate(true)
                .withMutable(EmptyMutableHeaderType.builder().build())
                .withStatic(staticHeader)
                .build();
    }

    private static NoPubKeyDigestsRequestStaticHeaderType staticHeader(final EbicsSession session,
                                                                       final OrderDetailsType orderDetails) {
        return NoPubKeyDigestsRequestStaticHeaderType.builder()
                .withHostID(session.getHostId())
                .withNonce(CryptoUtil.generateNonce())
                .withTimestamp(OffsetDateTime.now())
                .withPartnerID(session.getPartner().getPartnerId())
                .withUserID(session.getUser().getUserId())
                .withProduct(session.getProduct().map(EbicsXmlFactory::unsecuredProduct).orElse(null))
                .withOrderDetails(orderDetails)
                .withSecurityMedium(session.getUser().getSecurityMedium())
                .build();
    }

    private static OrderDetailsType orderDetails() {
        return NoPubKeyDigestsReqOrderDetailsType.builder()
                .withOrderAttribute(OrderAttributeType.DZHNN.name())
                .withOrderType(OrderType.HPB.name())
                .build();
    }
}
