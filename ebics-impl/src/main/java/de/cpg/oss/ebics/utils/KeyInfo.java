package de.cpg.oss.ebics.utils;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class KeyInfo { // Must be public to be accessible from template engine
    @NonNull
    private final String version;
    private final int exponentBits;
    @NonNull
    private final String exponentHex;
    private final int modulusBits;
    @NonNull
    private final String modulusHex;
    @NonNull
    private final String digestHex;
}
