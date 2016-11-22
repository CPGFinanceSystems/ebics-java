package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

@Value
@Wither
@Builder
public class EbicsSession {

    @NonNull
    private final EbicsUser user;
    @NonNull
    private final EbicsPartner partner;
    @NonNull
    private final EbicsBank bank;
    @NonNull
    private final EbicsConfiguration configuration;
    private final Product product;
    private final SerializationManager serializationManager;
    private final TraceManager traceManager;
    private final Map<String, String> parameters = new HashMap<>();

    public RSAPublicKey getBankEncryptionKey() {
        return (RSAPublicKey) getBank().getEncryptionKey().getPublicKey();
    }

    public RSAPrivateKey getUserEncryptionKey() {
        return (RSAPrivateKey) getUser().getEncryptionKey().getPrivateKey();
    }

    public String getHostId() {
        return getBank().getHostId();
    }

    public MessageProvider getMessageProvider() {
        return configuration.getMessageProvider();
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

    private Map<String, String> getParameters() {
        return parameters;
    }
}
