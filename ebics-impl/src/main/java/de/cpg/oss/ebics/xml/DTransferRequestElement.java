package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

@Builder
public class DTransferRequestElement implements EbicsRequestElement {

    private final int segmentNumber;
    private final boolean lastSegment;
    @NonNull
    private final byte[] transactionId;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        return EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.TRANSFER, segmentNumber, lastSegment),
                        EbicsXmlFactory.staticHeader(session.getHostId(), transactionId)));
    }
}
