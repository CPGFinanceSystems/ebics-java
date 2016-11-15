package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.io.ContentFactory;
import org.ebics.h004.EbicsResponse;

public class ReceiptResponseElement extends EbicsResponseElement {

    private ReceiptResponseElement(final EbicsResponse ebicsResponse) {
        super(ebicsResponse);
    }

    public static ReceiptResponseElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new ReceiptResponseElement(parse(contentFactory.getContent()));
    }

    @Override
    public void report(final MessageProvider messageProvider) throws EbicsException {
        final ReturnCode returnCode = getReturnCode();
        if (!returnCode.equals(ReturnCode.EBICS_DOWNLOAD_POSTPROCESS_DONE)) {
            returnCode.throwException(messageProvider);
        }
    }
}
