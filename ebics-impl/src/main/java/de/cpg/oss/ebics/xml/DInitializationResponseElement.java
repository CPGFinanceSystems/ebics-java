package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.NoDownloadDataAvailableException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.ebics.h004.EbicsResponse;

import java.io.InputStream;

public class DInitializationResponseElement extends EbicsResponseElement {

    private DInitializationResponseElement(final EbicsResponse ebicsResponse) {
        super(ebicsResponse);
    }

    public static DInitializationResponseElement parse(final InputStream inputStream) throws EbicsException {
        return new DInitializationResponseElement(parseXml(inputStream));
    }

    @Override
    public void report(final MessageProvider messageProvider) throws EbicsException {
        if (ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.equals(getReturnCode())) {
            throw new NoDownloadDataAvailableException();
        }
    }

    public int getNumSegments() {
        return getHeader().getStatic().getNumSegments().intValue();
    }

    public int getSegmentNumber() {
        return getHeader().getMutable().getSegmentNumber().getValue().intValue();
    }
}
