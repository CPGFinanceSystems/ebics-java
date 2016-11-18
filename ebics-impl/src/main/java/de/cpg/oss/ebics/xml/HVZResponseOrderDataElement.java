package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.SignatureVersion;
import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HVZResponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HVZResponseOrderDataElement implements ResponseOrderDataElement {

    private final HVZResponseOrderDataType responseOrderData;

    public static HVZResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HVZResponseOrderDataElement(XmlUtil.parse(HVZResponseOrderDataType.class, orderDataXml));
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
                        .dataDigest(orderDetails.getDataDigest().getValue())
                        .dataSignatureVersion(SignatureVersion.valueOf(orderDetails.getDataDigest().getSignatureVersion()))
                        .build())
                .collect(Collectors.toList());
    }
}
