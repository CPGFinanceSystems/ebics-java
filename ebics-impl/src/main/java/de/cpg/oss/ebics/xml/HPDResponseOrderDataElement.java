package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HPDProtocolParamsType;
import org.ebics.h004.HPDResponseOrderDataType;

import java.io.InputStream;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HPDResponseOrderDataElement implements ResponseOrderDataElement {

    private final HPDResponseOrderDataType responseOrderData;

    public static HPDResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HPDResponseOrderDataElement(XmlUtil.parse(HPDResponseOrderDataType.class, orderDataXml));
    }

    public String getBankName() {
        return responseOrderData.getAccessParams().getInstitute();
    }

    /**
     * @return <code>true</code> if HKD and HTD order types are supported
     * @see de.cpg.oss.ebics.api.OrderType
     */
    public boolean isClientDataDownloadSupported() {
        return Optional.ofNullable(responseOrderData.getProtocolParams().getClientDataDownload())
                .map(HPDProtocolParamsType.ClientDataDownload::isSupported)
                .orElse(false);
    }

    /**
     * @return <code>true</code> if HAA order type is supported
     * @see de.cpg.oss.ebics.api.OrderType
     */
    public boolean isDownloadableOrderDataSupported() {
        return Optional.ofNullable(responseOrderData.getProtocolParams().getDownloadableOrderData())
                .map(HPDProtocolParamsType.DownloadableOrderData::isSupported)
                .orElse(false);
    }
}
