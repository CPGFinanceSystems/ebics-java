package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

@Value
@Wither
@Builder
public class EbicsSession {

    private final EbicsUser user;
    private final EbicsPartner partner;
    private final EbicsBank bank;
    private final EbicsConfiguration configuration;
    private final Product product;
    private final Map<String, String> parameters = new HashMap<>();

    public RSAPublicKey getBankE002Key() {
        return (RSAPublicKey) getBank().getE002Key();
    }

    public String getHostId() {
        return getBank().getHostId();
    }

    public TraceManager getTraceManager() {
        return getConfiguration().getTraceManager();
    }

    public MessageProvider getMessageProvider() {
        return getConfiguration().getMessageProvider();
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
