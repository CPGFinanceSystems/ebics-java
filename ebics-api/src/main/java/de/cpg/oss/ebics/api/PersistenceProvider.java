package de.cpg.oss.ebics.api;

import java.io.IOException;

public interface PersistenceProvider {

    <T extends Identifiable> T save(Class<T> clazz, T object) throws IOException;

    <T extends Identifiable> T load(Class<T> clazz, String id) throws IOException;

    boolean delete(Identifiable identifiable) throws IOException;
}
