package de.cpg.oss.ebics.api;

import lombok.*;
import lombok.experimental.Wither;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

@Wither
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EbicsSession {

    @Getter
    @NonNull
    private final EbicsUser user;
    @Getter
    @NonNull
    private final EbicsPartner partner;
    @Getter
    @NonNull
    private final EbicsBank bank;
    @Getter
    @NonNull
    private final EbicsConfiguration configuration;
    private final Product product;
    @Getter
    @NonNull
    private final PersistenceProvider persistenceProvider;
    @Getter
    @NonNull
    private final XmlMessageTracer xmlMessageTracer;
    @Getter
    @NonNull
    private final FileTransferManager fileTransferManager;

    public PublicKey getBankEncryptionKey() {
        return getBank().getEncryptionKey().getPublicKey();
    }

    public PrivateKey getUserEncryptionKey() {
        return getUser().getEncryptionKey().getPrivateKey();
    }

    public String getHostId() {
        return getBank().getHostId();
    }

    public MessageProvider getMessageProvider() {
        return configuration.getMessageProvider();
    }

    public Optional<Product> getProduct() {
        return Optional.ofNullable(product);
    }
}
