package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import java.util.function.Supplier;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

public interface EbicsRequestElement {

    EbicsRequest createForSigning(EbicsSession session);

    default EbicsRequest create(final EbicsSession session) {
        return sign(createForSigning(session), session.getUser());
    }

    static <R extends EbicsRequestElement> EbicsRequest create(
            final EbicsSession session,
            final Supplier<R> ebicsRequestElementSupplier) {
        return ebicsRequestElementSupplier.get().create(session);
    }

    static EbicsRequest sign(final EbicsRequest requestToSign, final EbicsUser user) {
        requestToSign.setAuthSignature(XmlSignatureFactory.signatureType(
                XmlUtil.digest(EbicsRequest.class, requestToSign)));
        requestToSign.getAuthSignature().getSignatureValue().setValue(
                XmlUtil.sign(EbicsRequest.class, requestToSign, user));
        return requestToSign;
    }

    static EbicsRequest createSigned(final EbicsSession session, final OrderType orderType) {
        return sign(request(session.getConfiguration(),
                header(mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session,
                                CryptoUtil.generateNonce(),
                                orderDetails(orderAttribute(orderType), orderType)))),
                session.getUser());
    }

    static OrderAttributeType orderAttribute(final OrderType orderType) {
        if (OrderType.Transmission.DOWNLOAD.equals(orderType.getTransmission())) {
            return OrderAttributeType.DZHNN;
        } else {
            return OrderAttributeType.UZHNN;
        }
    }

    static String orderType(final EbicsRequest ebicsRequest) {
        return ebicsRequest.getHeader().getStatic().getOrderDetails().getOrderType().getValue();
    }
}
