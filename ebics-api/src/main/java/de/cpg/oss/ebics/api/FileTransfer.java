package de.cpg.oss.ebics.api;

import lombok.*;
import lombok.experimental.Wither;

import java.text.MessageFormat;
import java.util.UUID;

@Wither
@ToString
@EqualsAndHashCode
public class FileTransfer implements Identifiable {

    private static final long serialVersionUID = 4L;

    @NonNull
    private final String transferId;
    @Getter
    private final byte[] transactionId;
    @NonNull
    @Getter
    private final OrderType orderType;
    @Getter
    private final byte[] nonce;
    @Getter
    private final byte[] digest;
    @Getter
    private final int segmentNumber;
    @Getter
    private final int numSegments;

    @Override
    public String getId() {
        return transferId;
    }

    public UUID getTransferId() {
        return UUID.fromString(transferId);
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
     * Is the current segment the last one?
     */
    public boolean isLastSegment() {
        return getSegmentNumber() == getNumSegments();
    }

    @Builder
    private FileTransfer(final UUID transferId, final byte[] transactionId, final OrderType orderType, final byte[] nonce, final byte[] digest, final int segmentNumber, final int numSegments) {
        this(transferId.toString(), transactionId, orderType, nonce, digest, segmentNumber, numSegments);
    }

    private FileTransfer(final String transferId, final byte[] transactionId, final OrderType orderType, final byte[] nonce, final byte[] digest, final int segmentNumber, final int numSegments) {
        this.transferId = transferId;
        this.transactionId = transactionId;
        this.orderType = orderType;
        this.nonce = nonce;
        this.digest = digest;
        this.segmentNumber = segmentNumber;
        this.numSegments = numSegments;
    }


    // We all love JPA, don't we?
    private FileTransfer() {
        this(UUID.randomUUID(), null, OrderType.INI, null, null, 0, 0);
    }
}
