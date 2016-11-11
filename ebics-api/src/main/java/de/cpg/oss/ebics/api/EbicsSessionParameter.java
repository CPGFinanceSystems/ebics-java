package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URI;

@Value
@Wither
@Builder
public class EbicsSessionParameter {

    @NonNull
    private final String userId;
    @NonNull
    private final String partnerId;
    @NonNull
    private final String hostId;
    @NonNull
    private final URI bankUri;

    private final String userName;
    private final String bankName;
    private final PasswordCallback passwordCallback;
    private final SerializationManager serializationManager;
    private final LetterManager letterManager;
    private final TraceManager traceManager;
}
