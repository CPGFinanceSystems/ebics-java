package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.FileTransferState;
import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.NoDownloadDataAvailableException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.io.ContentFactory;
import org.ebics.h004.EbicsResponse;

public class DInitializationResponseElement extends EbicsResponseElement {

    private DInitializationResponseElement(final EbicsResponse ebicsResponse) {
        super(ebicsResponse);
    }

    public static DInitializationResponseElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new DInitializationResponseElement(parse(contentFactory.getContent()));
    }

    @Override
    public void report(final MessageProvider messageProvider) throws EbicsException {
        if (ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.equals(getReturnCode())) {
            throw new NoDownloadDataAvailableException();
        }
    }

    public FileTransferState getFileTransferState() {
        return FileTransferState.builder()
                .numSegments(getHeader().getStatic().getNumSegments().intValue())
                .segmentNumber(getHeader().getMutable().getSegmentNumber().getValue().intValue())
                .transactionId(getTransactionId())
                .build();
    }
}
