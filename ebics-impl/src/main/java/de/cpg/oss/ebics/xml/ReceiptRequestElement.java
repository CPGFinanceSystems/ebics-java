package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AllArgsConstructor;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

/**
 * The <code>ReceiptRequestElement</code> is the element containing the
 * receipt request to tell the server bank that all segments are received.
 */
@AllArgsConstructor
public class ReceiptRequestElement {

    private final byte[] transactionId;

    public EbicsRequest create(final EbicsSession session) {
        final EbicsRequest request = request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.RECEIPT),
                        staticHeader(session.getHostId(), transactionId)),
                EbicsRequest.Body.builder()
                        .withTransferReceipt(EbicsRequest.Body.TransferReceipt.builder()
                                .withAuthenticate(true)
                                .withReceiptCode(0)
                                .build())
                        .build());

        request.setAuthSignature(XmlSignatureFactory.signatureType(XmlUtil.digest(EbicsRequest.class, request)));

        return request;
    }
}
