package de.cpg.oss.ebics.api;

import lombok.*;

import java.util.UUID;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileTransferSegment implements Identifiable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final String segmentId;
    @Getter
    @NonNull
    private final byte[] content;

    @Override
    public String getId() {
        return segmentId;
    }

    public UUID getSegmentId() {
        return UUID.fromString(segmentId);
    }

    public static FileTransferSegment valueOf(final UUID segmentId, final byte[] content) {
        return new FileTransferSegment(segmentId.toString(), content);
    }

    public static FileTransferSegment create(final byte[] content, final int contentLength) {
        final byte[] contentCopy = new byte[contentLength];
        System.arraycopy(content, 0, contentCopy, 0, contentLength);
        return new FileTransferSegment(UUID.randomUUID().toString(), contentCopy);
    }

    // We all love JPA, don't we?
    private FileTransferSegment() {
        this("", new byte[0]);
    }
}
