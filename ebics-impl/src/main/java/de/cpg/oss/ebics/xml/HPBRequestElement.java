package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsConfiguration;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsNoPubKeyDigestsRequest;
import org.ebics.h004.NoPubKeyDigestsRequestStaticHeaderType;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.OrderDetailsType;

import java.time.LocalDateTime;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.OBJECT_FACTORY;

public abstract class HPBRequestElement {

    public static EbicsNoPubKeyDigestsRequest create(final EbicsSession session) throws EbicsException {
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
        final EbicsNoPubKeyDigestsRequest request = OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequest();
        request.setRevision(configuration.getRevision());
        request.setVersion(configuration.getVersion().name());
        request.setHeader(header);
        request.setBody(OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequestBody());
        return request;
    }

    private static EbicsNoPubKeyDigestsRequest.Header header(final NoPubKeyDigestsRequestStaticHeaderType staticHeader) {
        final EbicsNoPubKeyDigestsRequest.Header header = OBJECT_FACTORY.createEbicsNoPubKeyDigestsRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(OBJECT_FACTORY.createEmptyMutableHeaderType());
        header.setStatic(staticHeader);
        return header;
    }

    private static NoPubKeyDigestsRequestStaticHeaderType staticHeader(final EbicsSession session,
                                                                       final OrderDetailsType orderDetails) {
        final NoPubKeyDigestsRequestStaticHeaderType staticHeader = OBJECT_FACTORY.createNoPubKeyDigestsRequestStaticHeaderType();
        staticHeader.setHostID(session.getHostId());
        staticHeader.setNonce(CryptoUtil.generateNonce());
        staticHeader.setTimestamp(LocalDateTime.now());
        staticHeader.setPartnerID(session.getPartner().getPartnerId());
        staticHeader.setUserID(session.getUser().getUserId());
        staticHeader.setProduct(EbicsXmlFactory.unsecuredProduct(session.getProduct()));
        staticHeader.setOrderDetails(orderDetails);
        staticHeader.setSecurityMedium(session.getUser().getSecurityMedium());
        return staticHeader;
    }

    private static OrderDetailsType orderDetails() {
        final OrderDetailsType orderDetails = OBJECT_FACTORY.createNoPubKeyDigestsReqOrderDetailsType();
        orderDetails.setOrderAttribute(OrderAttributeType.DZHNN.name());
        orderDetails.setOrderType(OrderType.HPB.name());
        return orderDetails;
    }
}
