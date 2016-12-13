package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;

@Builder
public class EbicsSessionParameter {

    @Getter
    @NonNull
    private final String userId;
    @Getter
    @NonNull
    private final String partnerId;
    @Getter
    @NonNull
    private final String hostId;
    @Getter
    @NonNull
    private final URI bankUri;
    @Getter
    @NonNull
    private final PersistenceProvider persistenceProvider;

    private final PasswordCallback passwordCallback;
    private final XmlMessageTracer xmlMessageTracer;

    public Optional<PasswordCallback> getPasswordCallback() {
        return Optional.ofNullable(passwordCallback);
    }

    public Optional<XmlMessageTracer> getXmlMessageTracer() {
        return Optional.ofNullable(xmlMessageTracer);
    }
}
