package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.PersistenceProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InMemoryPersistenceProvider implements PersistenceProvider {

    public static final PersistenceProvider INSTANCE = new InMemoryPersistenceProvider();

    private final Map<String, Object> objectMap = new HashMap<>();

    @Override
    public <T extends Identifiable> T save(final Class<T> clazz, final T object) throws IOException {
        objectMap.put(object.getId(), object);
        return object;
    }

    @Override
    public <T extends Identifiable> T load(final Class<T> clazz, final String id) throws IOException {
        final Optional<T> object = Optional.ofNullable((T) objectMap.get(id));
        return object.orElseThrow(() -> new IOException(MessageFormat.format("Object with ID {0} not found", id)));
    }

    @Override
    public boolean delete(final Identifiable identifiable) throws IOException {
        return objectMap.remove(identifiable.getId()) != null;
    }

    @Override
    public <T extends Identifiable> boolean delete(final Class<T> clazz, final String id) throws IOException {
        return objectMap.remove(id) != null;
    }
}
