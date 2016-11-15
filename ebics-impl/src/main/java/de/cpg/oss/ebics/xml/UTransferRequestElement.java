package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.IOUtil;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.DataTransferRequestType;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.TransactionPhaseType;

import java.io.IOException;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@Builder
public class UTransferRequestElement implements EbicsRequestElement {

    private final int segmentNumber;
    private final boolean lastSegment;
    private final byte[] transactionId;
    @NonNull
    private final ContentFactory contentFactory;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        final DataTransferRequestType.OrderData orderData = OBJECT_FACTORY.createDataTransferRequestTypeOrderData();
        try {
            orderData.setValue(IOUtil.read(contentFactory.getContent()));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        final DataTransferRequestType dataTransfer = OBJECT_FACTORY.createDataTransferRequestType();
        dataTransfer.setOrderData(orderData);

        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.TRANSFER, segmentNumber, lastSegment),
                        staticHeader(session.getHostId(), transactionId)),
                body(dataTransfer));
    }
}
