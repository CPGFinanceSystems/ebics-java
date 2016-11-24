package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.EbicsResponse;

import java.io.InputStream;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EbicsResponseElement implements ResponseElement<EbicsResponse> {

    @Getter
    private final EbicsResponse response;

    public static EbicsResponseElement parse(final InputStream inputStream) throws EbicsException {
        return new EbicsResponseElement(parseXml(inputStream));
    }

    @Override
    public Class<EbicsResponse> getResponseClass() {
        return EbicsResponse.class;
    }

    public EbicsResponse.Header getHeader() {
        return response.getHeader();
    }

    public EbicsResponse.Body getBody() {
        return response.getBody();
    }

    public byte[] getTransactionId() {
        return getHeader().getStatic().getTransactionID();
    }

    @Override
    public byte[] getOrderData() {
        return getBody().getDataTransfer().getOrderData().getValue();
    }

    @Override
    public byte[] getTransactionKey() {
        return getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
    }

    @Override
    public ReturnCode getReturnCode() {
        return ReturnCode.toReturnCode(
                getHeader().getMutable().getReturnCode(),
                getHeader().getMutable().getReportText());
    }

    static EbicsResponse parseXml(final InputStream inputStream) throws EbicsException {
        return XmlUtil.parse(EbicsResponse.class, inputStream);
    }
}
