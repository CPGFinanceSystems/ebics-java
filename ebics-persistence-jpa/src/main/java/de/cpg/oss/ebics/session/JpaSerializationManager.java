package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.SerializationManager;
import javaslang.collection.Stream;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional
public class JpaSerializationManager implements SerializationManager {

    private final EbicsBankRepository bankRepository;
    private final EbicsPartnerRepository partnerRepository;
    private final EbicsUserRepository userRepository;
    private final FileTransferRepository fileTransferRepository;

    public JpaSerializationManager(final EbicsBankRepository bankRepository,
                                   final EbicsPartnerRepository partnerRepository,
                                   final EbicsUserRepository userRepository,
                                   final FileTransferRepository fileTransferRepository) {
        this.bankRepository = bankRepository;
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.fileTransferRepository = fileTransferRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T serialize(final Class<T> clazz, final T object) throws IOException {
        return ((EbicsRepository<T>) findRepositoryFor(clazz)).save(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T deserialize(final Class<T> clazz, final String id) throws IOException {
        return (T) findRepositoryFor(clazz).findOne(id);
    }

    @Override
    public boolean delete(final Identifiable identifiable) throws IOException {
        if (bankRepository.canHandle(identifiable)) {
            bankRepository.delete(identifiable.getId());
            return true;
        }
        return false;
    }

    private <T extends Identifiable> EbicsRepository<? extends Identifiable> findRepositoryFor(final Class<T> clazz) {
        return Stream.of(bankRepository, partnerRepository, userRepository, fileTransferRepository)
                .find(r -> r.getEntityType().isAssignableFrom(clazz))
                .getOrElseThrow(() -> new IllegalArgumentException("Unknown object type " + clazz.getName()));

    }
}
