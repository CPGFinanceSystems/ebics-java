package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsConfiguration;
import de.cpg.oss.ebics.api.SignatureVersion;
import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HVDResponseOrderDataType;

import java.io.InputStream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HVDResponseOrderDataElement implements ResponseOrderDataElement<HVDResponseOrderDataType> {

    @Getter
    private final HVDResponseOrderDataType responseOrderData;

    public static HVDResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HVDResponseOrderDataElement(XmlUtil.parse(HVDResponseOrderDataType.class, orderDataXml));
    }

    public VEUOrderDetails enrichVEUOrderDetails(final EbicsConfiguration configuration,
                                                 final VEUOrderDetails orderDetails) {
        return orderDetails.withDataDigest(responseOrderData.getDataDigest().getValue())
                .withDataSignatureVersion(SignatureVersion.ofRaw(responseOrderData.getDataDigest().getSignatureVersion()))
                .withSummary(new String(responseOrderData.getDisplayFile(), configuration.getVeuDisplayFileCharset()));
    }

    public boolean isOrderDetailsAvailable() {
        return responseOrderData.isOrderDetailsAvailable();
    }

    @Override
    public Class<HVDResponseOrderDataType> getResponseOrderDataClass() {
        return HVDResponseOrderDataType.class;
    }
}
