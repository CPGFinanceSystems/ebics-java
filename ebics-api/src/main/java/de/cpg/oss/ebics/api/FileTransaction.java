package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.UUID;

@Value
@Wither
@Builder(builderClassName = "Builder")
public class FileTransaction implements Serializable {

    private static final long serialVersionUID = 2L;

    @NonNull
    private final UUID id;
    private final byte[] remoteTransactionId;
    @NonNull
    private final OrderType orderType;
    private final byte[] nonce;
    private final byte[] digest;
    private final int segmentNumber;
    private final int numSegments;

    /**
     * Returns the next segment number to be transferred.
     */
    public FileTransaction next() {
        if (!hasNext()) {
            throw new IllegalStateException(MessageFormat.format(
                    "All segments ({0} in total) already processed for transaction {1}",
                    getNumSegments(), this.getId()));
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
