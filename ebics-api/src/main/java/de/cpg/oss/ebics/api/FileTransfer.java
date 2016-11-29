package de.cpg.oss.ebics.api;

import lombok.*;
import lombok.experimental.Wither;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Wither
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileTransfer implements Identifiable {

    private static final long serialVersionUID = 5L;

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
    private final Collection<String> segmentIds;

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

    public int getNumSegments() {
        return segmentIds.size();
    }

    public List<UUID> getSegmentIds() {
        return segmentIds.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    @Builder
    private FileTransfer(final UUID transferId,
                         final byte[] transactionId,
                         final OrderType orderType,
                         final byte[] nonce,
                         final byte[] digest,
                         final int segmentNumber,
                         final List<UUID> segmentIds) {
        this(transferId.toString(),
                transactionId,
                orderType,
                nonce,
                digest,
                segmentNumber,
                segmentIds.stream().map(UUID::toString).collect(Collectors.toList()));
    }

    // We all love JPA, don't we?
    private FileTransfer() {
        this(UUID.randomUUID(), null, OrderType.INI, null, null, 0, Collections.emptyList());
    }
}
