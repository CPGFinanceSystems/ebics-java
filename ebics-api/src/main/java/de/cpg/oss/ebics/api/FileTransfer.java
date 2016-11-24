package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.text.MessageFormat;
import java.util.UUID;

@Value
@Wither
@Builder(builderClassName = "Builder")
public class FileTransfer implements Identifiable {

    private static final long serialVersionUID = 3L;

    @NonNull
    private final UUID transferId;
    private final byte[] transactionId;
    @NonNull
    private final OrderType orderType;
    private final byte[] nonce;
    private final byte[] digest;
    private final int segmentNumber;
    private final int numSegments;

    @Override
    public String getId() {
        return transferId.toString();
    }

    /**
     * Returns the next segment number to be transferred.
     */
    public FileTransfer next() {
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
