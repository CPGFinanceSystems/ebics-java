package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.DataTransferRequestType;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@Builder
public class UTransferRequestElement implements EbicsRequestElement {

    private final int segmentNumber;
    private final boolean lastSegment;
    @NonNull
    private final byte[] transactionId;
    @NonNull
    private final byte[] content;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.TRANSFER, segmentNumber, lastSegment),
                        staticHeader(session.getHostId(), transactionId)),
                body(DataTransferRequestType.builder()
                        .withOrderData(DataTransferRequestType.OrderData.builder()
                                .withValue(content)
                                .build())
                        .build()));
    }
}
