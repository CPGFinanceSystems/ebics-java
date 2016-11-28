package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.PersistenceProvider;

import java.io.*;
import java.text.MessageFormat;


public class BinaryPersistenceProvider implements PersistenceProvider {

    private final File storageDirectory;

    public BinaryPersistenceProvider(final File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    @Override
    public <T extends Identifiable> T save(final Class<T> clazz, final T object) throws IOException {
        try (final ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(new File(storageDirectory, filenameFor(object))))) {
            out.writeObject(object);
            return object;
        }
    }

    @Override
    public <T extends Identifiable> T load(final Class<T> clazz, final String id) throws IOException {
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(new File(storageDirectory, filenameFor(clazz, id))))) {
            final Object object = objectInputStream.readObject();
            if (clazz.isAssignableFrom(object.getClass())) {
                return clazz.cast(object);
            } else {
                throw new IOException(MessageFormat.format(
                        "Invalid object class! Expected {0}, got {1} for {2}",
                        clazz.getName(), object.getClass().getName(), id));
            }
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean delete(final Identifiable identifiable) throws IOException {
        return new File(storageDirectory, filenameFor(identifiable)).delete();
    }

    private static String filenameFor(final Identifiable identifiable) {
        return filenameFor(identifiable.getClass(), identifiable.getId());
    }

    private static <T extends Identifiable> String filenameFor(final Class<T> clazz, final String id) {
        return clazz.getSimpleName().concat("_").concat(id).concat(".bin");
    }
}
