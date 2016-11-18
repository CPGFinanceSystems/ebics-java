package de.cpg.oss.ebics.api;

import javaslang.control.Either;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

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

    public Collection<Either<OrderType, String>> getSupportedOrderTypes() {
        return supportedOrderTypes.stream()
                .map(OrderType::ofRaw)
                .collect(Collectors.toList());
    }
}
