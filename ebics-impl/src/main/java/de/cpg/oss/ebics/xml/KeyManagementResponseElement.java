package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.EbicsKeyManagementResponse;

/**
 * The <code>KeyManagementResponseElement</code> is the common element
 * for ebics key management requests. This element aims to control the
 * returned code from the ebics server and throw an exception if it is
 * not an EBICS_OK code.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyManagementResponseElement {

    @Getter
    private EbicsKeyManagementResponse response;

    public static KeyManagementResponseElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new KeyManagementResponseElement(XmlUtil.parse(EbicsKeyManagementResponse.class, contentFactory.getContent()));
    }

    /**
     * Returns the transaction key of the response.
     */
    public byte[] getTransactionKey() {
        return response.getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
    }

    /**
     * Returns the order data of the response.
     */
    public byte[] getOrderData() {
        return response.getBody().getDataTransfer().getOrderData().getValue();
    }

    public ReturnCode getReturnCode() {
        return ReturnCode.toReturnCode(
                response.getHeader().getMutable().getReturnCode(),
                response.getHeader().getMutable().getReportText());
    }

    public void report(final MessageProvider messageProvider) throws EbicsException {
        final ReturnCode returnCode = getReturnCode();
        if (!returnCode.isOk()) {
            returnCode.throwException(messageProvider);
        }
    }
}
