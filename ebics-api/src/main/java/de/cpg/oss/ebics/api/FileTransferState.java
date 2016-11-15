package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.text.MessageFormat;

@Value
@Wither
@Builder
public class FileTransferState implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final byte[] transactionId;
    private final int segmentNumber;
    private final int numSegments;

    /**
     * Returns the next segment number to be transferred.
     */
    public FileTransferState next() {
        if (!hasNext()) {
            throw new IllegalStateException(MessageFormat.format(
                    "All segments ({0} in total) already processed for transaction {1}",
                    getNumSegments(), getTransactionId()));
        }
        return withSegmentNumber(getSegmentNumber() + 1);
    }

    public boolean hasNext() {
        return getSegmentNumber() < getNumSegments();
    }

    /**
     * Is the current segment is the last one?
     */
    public boolean isLastSegment() {
        return getSegmentNumber() == getNumSegments();
    }
}
