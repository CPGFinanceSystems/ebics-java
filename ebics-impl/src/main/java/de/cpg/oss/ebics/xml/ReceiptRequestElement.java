package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

/**
 * The <code>ReceiptRequestElement</code> is the element containing the
 * receipt request to tell the server bank that all segments are received.
 *
 * @author Hachani
 */
public class ReceiptRequestElement {

    private final EbicsSession session;
    private final byte[] transactionId;

    /**
     * Construct a new <code>ReceiptRequestElement</code> element.
     *
     * @param session the current ebics session
     */
    public ReceiptRequestElement(final EbicsSession session,
                                 final byte[] transactionId) {
        this.session = session;
        this.transactionId = transactionId;
    }

    public EbicsRequest build() throws EbicsException {
        final EbicsRequest.Body.TransferReceipt transferReceipt = OBJECT_FACTORY.createEbicsRequestBodyTransferReceipt();
        transferReceipt.setAuthenticate(true);
        transferReceipt.setReceiptCode(0);

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setTransferReceipt(transferReceipt);

        final EbicsRequest request = request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.RECEIPT),
                        staticHeader(session.getHostId(), transactionId)),
                body);

        request.setAuthSignature(XmlSignatureFactory.signatureType(XmlUtil.digest(EbicsRequest.class, request)));

        return request;
    }
}
