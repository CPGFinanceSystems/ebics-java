package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URI;

@Value
@Wither
@Builder
public class EbicsBank implements Identifiable {

    private static final long serialVersionUID = 2L;

    @NonNull
    private final URI uri;

    private final EbicsAuthenticationKey authenticationKey;
    private final EbicsEncryptionKey encryptionKey;

    @NonNull
    private final String hostId;
    private final String name;

    @Override
    public String getId() {
        return getHostId();
    }
}
