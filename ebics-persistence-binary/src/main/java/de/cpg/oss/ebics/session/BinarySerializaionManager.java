package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.SerializationManager;

import java.io.*;
import java.text.MessageFormat;


public class BinarySerializaionManager implements SerializationManager {

    private final File serializationDir;

    public BinarySerializaionManager(final File serializationDir) {
        this.serializationDir = serializationDir;
    }

    @Override
    public void serialize(final Identifiable object) throws IOException {
        try (final ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(new File(serializationDir, filenameFor(object))))) {
            out.writeObject(object);
        }
    }

    @Override
    public <T extends Identifiable> T deserialize(final Class<T> clazz, final String id) throws IOException {
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(new File(serializationDir, filenameFor(clazz, id))))) {
            final Object object = objectInputStream.readObject();
            if (clazz.isAssignableFrom(object.getClass())) {
                return clazz.cast(object);
            } else {
                throw new IOException(MessageFormat.format(
                        "Invalid object class! Expected {0}, got {1} for {2}",
                        clazz.getName(), object.getClass().getName(), id));
            }
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String filenameFor(final Identifiable identifiable) {
        return filenameFor(identifiable.getClass(), identifiable.getId());
    }

    private static <T extends Identifiable> String filenameFor(final Class<T> clazz, final String id) {
        return clazz.getSimpleName().concat("_").concat(id).concat(".bin");
    }
}
