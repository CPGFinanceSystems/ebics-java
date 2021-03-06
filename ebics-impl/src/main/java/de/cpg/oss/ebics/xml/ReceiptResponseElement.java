package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.ebics.h004.EbicsResponse;

import java.io.InputStream;

public class ReceiptResponseElement extends EbicsResponseElement {

    private ReceiptResponseElement(final EbicsResponse ebicsResponse) {
        super(ebicsResponse);
    }

    public static ReceiptResponseElement parse(final InputStream inputStream) {
        return new ReceiptResponseElement(parseXml(inputStream));
    }

    @Override
    public void report(final MessageProvider messageProvider) throws EbicsException {
        final ReturnCode returnCode = getReturnCode();
        if (!returnCode.equals(ReturnCode.EBICS_DOWNLOAD_POSTPROCESS_DONE)) {
            returnCode.throwException(messageProvider);
        }
    }
}
