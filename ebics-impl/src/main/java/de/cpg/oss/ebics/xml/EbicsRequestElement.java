package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import java.util.function.Supplier;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

public interface EbicsRequestElement {

    EbicsRequest createForSigning(EbicsSession session) throws EbicsException;

    default EbicsRequest create(final EbicsSession session) throws EbicsException {
        return sign(createForSigning(session), session.getUser());
    }

    static <R extends EbicsRequestElement> EbicsRequest create(
            final EbicsSession session,
            final Supplier<R> ebicsRequestElementSupplier) throws EbicsException {
        return ebicsRequestElementSupplier.get().create(session);
    }

    static EbicsRequest sign(final EbicsRequest requestToSign, final EbicsUser user) throws EbicsException {
        requestToSign.setAuthSignature(XmlSignatureFactory.signatureType(
                XmlUtil.digest(EbicsRequest.class, requestToSign)));
        requestToSign.getAuthSignature().getSignatureValue().setValue(
                XmlUtil.sign(EbicsRequest.class, requestToSign, user));
        return requestToSign;
    }

    static EbicsRequest createSigned(final EbicsSession session, final OrderType orderType) throws EbicsException {
        return sign(request(session.getConfiguration(),
                header(mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session,
                                CryptoUtil.generateNonce(),
                                orderDetails(OrderAttributeType.DZHNN, orderType)))),
                session.getUser());
    }
}
