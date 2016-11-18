package de.cpg.oss.ebics.api;

import javaslang.control.Either;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class VEUOrder {

    @NonNull
    private final Either<OrderType, String> type;
    @NonNull
    private final String id;
    private final int dataSize;
    private final boolean readyToBeSigned;
    private final int requiredNumberOfSignatures;
    private final int doneNumberOfSignatures;
    @NonNull
    private final String partnerId;
    @NonNull
    private final String userId;
    @NonNull
    private final OffsetDateTime timestamp;
}
