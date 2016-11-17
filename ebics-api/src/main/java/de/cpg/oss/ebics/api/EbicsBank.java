package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URI;
import java.util.Collection;

@Value
@Wither
@Builder
public class EbicsBank implements Identifiable {

    private static final long serialVersionUID = 3L;

    @NonNull
    private final URI uri;

    private final EbicsAuthenticationKey authenticationKey;
    private final EbicsEncryptionKey encryptionKey;

    @NonNull
    private final String hostId;
    private final String name;

    private final Collection<String> supportedOrderTypes;

    @Override
    public String getId() {
        return getHostId();
    }
}
