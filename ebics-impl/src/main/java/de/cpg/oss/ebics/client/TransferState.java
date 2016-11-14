package de.cpg.oss.ebics.client;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.text.MessageFormat;

@Value
@Wither
@Builder
class TransferState implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final byte[] transactionId;
    @NonNull
    private final int segmentNumber;
    @NonNull
    private final int numSegments;

    /**
     * Returns the next segment number to be transferred.
     */
    TransferState next() {
        if (!hasNext()) {
            throw new IllegalStateException(MessageFormat.format(
                    "All segments ({0} in total) already processed for transaction {1}",
                    getNumSegments(), getTransactionId()));
        }
        return withSegmentNumber(getSegmentNumber() + 1);
    }

    boolean hasNext() {
        return getSegmentNumber() < getNumSegments();
    }

    /**
     * Is the current segment is the last one?
     */
    boolean isLastSegment() {
        return getSegmentNumber() == getNumSegments();
    }
}
