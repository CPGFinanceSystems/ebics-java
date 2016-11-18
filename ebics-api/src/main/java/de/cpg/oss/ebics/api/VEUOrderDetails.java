package de.cpg.oss.ebics.api;

import javaslang.control.Either;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Wither
@Builder
public class VEUOrderDetails {

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

    // optional information
    private final byte[] dataDigest;
    private final Either<SignatureVersion, String> dataSignatureVersion;
    private final Integer orderCount;
    private final BigDecimal orderSumAmount;
    private final String firstOrderAccountNumber;
    private final String firstOrderCurrency;
    private final String summary;
}
