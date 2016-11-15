package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.EbicsResponse;

import java.io.InputStream;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EbicsResponseElement {

    @Getter
    private final EbicsResponse response;

    static EbicsResponse parse(final InputStream inputStream) throws EbicsException {
        return XmlUtil.parse(EbicsResponse.class, inputStream);
    }

    public static EbicsResponseElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new EbicsResponseElement(parse(contentFactory.getContent()));
    }

    public void report(final MessageProvider messageProvider) throws EbicsException {
        final ReturnCode returnCode = getReturnCode();
        if (!returnCode.isOk()) {
            returnCode.throwException(messageProvider);
        }
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

    public byte[] getOrderData() {
        return getBody().getDataTransfer().getOrderData().getValue();
    }

    public ReturnCode getReturnCode() {
        return ReturnCode.toReturnCode(
                getHeader().getMutable().getReturnCode(),
                getHeader().getMutable().getReportText());
    }
}
