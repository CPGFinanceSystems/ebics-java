package de.cpg.oss.ebics.api;

import javaslang.control.Either;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.math.BigDecimal;

@Value
@Wither
@Builder
public class DetailedVEUOrder {

    @NonNull
    private final VEUOrder order;
    private final byte[] dataDigest;
    private final Either<SignatureVersion, String> dataSignatureVersion;
    private final int orderCount;
    private final BigDecimal orderSumAmount;
    private final String fileFormat;
    private final String currency;
    private final String summary;
}
