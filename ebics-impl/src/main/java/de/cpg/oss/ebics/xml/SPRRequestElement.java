package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.IOUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

public class SPRRequestElement implements EbicsRequestElement {

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) {
        final byte[] nonce = CryptoUtil.generateNonce();

        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(
                                session,
                                nonce,
                                0,
                                orderDetails(
                                        OrderAttributeType.UZHNN,
                                        OrderType.SPR))),
                body(dataTransferRequest(
                        session,
                        IOUtil.wrap(" ".getBytes()),
                        nonce)));
    }
}
