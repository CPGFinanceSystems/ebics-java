package de.cpg.oss.ebics.api;

import javaslang.control.Either;
import lombok.*;
import lombok.experimental.Wither;

import java.util.Collection;
import java.util.stream.Collectors;

@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EbicsUser implements Identifiable {

    private static final long serialVersionUID = 4L;

    private final EbicsSignatureKey signatureKey;
    private final EbicsEncryptionKey encryptionKey;
    private final EbicsAuthenticationKey authenticationKey;

    private final String securityMedium;

    @NonNull
    private final String userId;
    private final String name;

    @NonNull
    private final UserStatus status;
    private final Collection<String> permittedOrderTypes;

    private final transient PasswordCallback passwordCallback;

    @Override
    public String getId() {
        return getUserId();
    }

    public Collection<Either<OrderType, String>> getPermittedOrderTypes() {
        return permittedOrderTypes.stream()
                .map(OrderType::ofRaw)
                .collect(Collectors.toList());
    }

    // We all love JPA, don't we?
    private EbicsUser() {
        this(null, null, null, null, "", null, UserStatus.NEW, null, null);
    }
}
