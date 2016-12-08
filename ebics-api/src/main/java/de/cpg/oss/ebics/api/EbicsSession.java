package de.cpg.oss.ebics.api;

import lombok.*;
import lombok.experimental.Wither;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
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
    @NonNull
    @Getter
    private final EbicsBank bank;
    @Getter
    @NonNull
    private final EbicsConfiguration configuration;
    private final Product product;
    @Getter
    private final PersistenceProvider persistenceProvider;
    @Getter
    private final XmlMessageTracer xmlMessageTracer;
    @Getter
    private final FileTransferManager fileTransferManager;
    @Getter
    private final Map<String, String> parameters = new HashMap<>();

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

    public void addSessionParam(final String key, final String value) {
        getParameters().put(key, value);
    }

    public String getSessionParam(final String key) {
        if (key == null) {
            return null;
        }
        return getParameters().get(key);
    }
}
