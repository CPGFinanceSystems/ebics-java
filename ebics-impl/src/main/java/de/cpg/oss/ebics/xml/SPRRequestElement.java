package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import javax.crypto.spec.SecretKeySpec;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

public class SPRRequestElement implements EbicsRequestElement {

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
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
                        " ".getBytes(),
                        new SecretKeySpec(nonce, "AES"),
                        CryptoUtil.generateTransactionKey(nonce, session.getBankEncryptionKey()))));
    }
}
