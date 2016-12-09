package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.EbicsKeyManagementResponse;

import java.io.InputStream;

/**
 * The <code>KeyManagementResponseElement</code> is the common element
 * for ebics key management requests. This element aims to control the
 * returned code from the ebics server and throw an exception if it is
 * not an EBICS_OK code.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyManagementResponseElement implements ResponseElement<EbicsKeyManagementResponse> {

    @Getter
    private EbicsKeyManagementResponse response;

    public static KeyManagementResponseElement parse(final InputStream inputStream) {
        return new KeyManagementResponseElement(XmlUtil.parse(EbicsKeyManagementResponse.class, inputStream));
    }

    @Override
    public byte[] getTransactionKey() {
        return response.getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
    }

    @Override
    public byte[] getOrderData() {
        return response.getBody().getDataTransfer().getOrderData().getValue();
    }

    @Override
    public Class<EbicsKeyManagementResponse> getResponseClass() {
        return EbicsKeyManagementResponse.class;
    }

    @Override
    public ReturnCode getReturnCode() {
        return ReturnCode.valueOf(
                response.getHeader().getMutable().getReturnCode(),
                response.getHeader().getMutable().getReportText());
    }
}
