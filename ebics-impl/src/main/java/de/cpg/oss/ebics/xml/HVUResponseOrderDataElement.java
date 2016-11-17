package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HVUResponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HVUResponseOrderDataElement implements ResponseOrderDataElement {

    private final HVUResponseOrderDataType responseOrderData;

    public static HVUResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HVUResponseOrderDataElement(XmlUtil.parse(HVUResponseOrderDataType.class, orderDataXml));
    }

    public Collection<VEUOrderDetails> getVEUOrderDetails() {
        return responseOrderData.getOrderDetails().stream()
                .map(orderDetails -> VEUOrderDetails.builder()
                        .type(orderDetails.getOrderType())
                        .id(orderDetails.getOrderID())
                        .dataSize(orderDetails.getOrderDataSize().intValue())
                        .readyToBeSigned(orderDetails.getSigningInfo().isReadyToBeSigned())
                        .requiredNumberOfSignatures(orderDetails.getSigningInfo().getNumSigRequired().intValue())
                        .doneNumberOfSignatures(orderDetails.getSigningInfo().getNumSigDone().intValue())
                        .partnerId(orderDetails.getOriginatorInfo().getPartnerID())
                        .userId(orderDetails.getOriginatorInfo().getUserID())
                        .timestamp(orderDetails.getOriginatorInfo().getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
