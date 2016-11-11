package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.cpg.oss.ebics.api.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
public class JsonSerializationManager implements SerializationManager {

    private final ObjectMapper objectMapper;
    private final File serializationDirectory;

    public JsonSerializationManager(final File serializationDirectory) {
        this.objectMapper = objectMapper();
        this.serializationDirectory = serializationDirectory;
    }

    @Override
    public void serialize(final Identifiable identifiable) throws IOException {
        final File file = new File(serializationDirectory, filenameFrom(identifiable));
        log.debug("Serialize {} into {}", identifiable.getId(), file.getAbsolutePath());
        objectMapper.writer().writeValue(file, identifiable);
    }

    @Override
    public <T extends Identifiable> T deserialize(final Class<T> clazz, final String id) throws IOException {
        final File file = new File(serializationDirectory, filenameFrom(clazz, id));
        log.debug("Deserialize {} from {}", id, file.getAbsolutePath());
        return objectMapper.readerFor(clazz).readValue(new FileInputStream(file));
    }

    private static <T extends Identifiable> String filenameFrom(final Class<T> clazz, final String id) {
        return clazz.getSimpleName().concat("_").concat(id).concat(".json");
    }

    private static <T extends Identifiable> String filenameFrom(final T identifiable) {
        return filenameFrom(identifiable.getClass(), identifiable.getId());
    }

    private static ObjectMapper objectMapper() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(new PrivateKeySerializer(PrivateKey.class));
        module.addDeserializer(PrivateKey.class, new PrivateKeyDeserializer(PrivateKey.class));

        module.addSerializer(new PublicKeySerializer(PublicKey.class));
        module.addDeserializer(PublicKey.class, new PublicKeyDeserializer(PublicKey.class));

        module.setMixInAnnotation(EbicsBank.class, EbicsBankMixin.class);
        module.setMixInAnnotation(EbicsPartner.class, EbicsPartnerMixin.class);
        module.setMixInAnnotation(EbicsUser.class, EbicsUserMixin.class);

        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setVisibility(objectMapper.getVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE))
                .registerModule(module)
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
