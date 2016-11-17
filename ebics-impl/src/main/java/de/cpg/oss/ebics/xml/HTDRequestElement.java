package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

public class HTDRequestElement implements EbicsRequestElement {

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(
                                session,
                                CryptoUtil.generateNonce(),
                                orderDetails(
                                        OrderAttributeType.DZHNN,
                                        OrderType.HTD))));
    }
}
