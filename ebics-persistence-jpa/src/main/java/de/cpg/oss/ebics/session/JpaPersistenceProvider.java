package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.PersistenceProvider;
import javaslang.collection.Stream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@Transactional
public class JpaPersistenceProvider implements PersistenceProvider {

    private final EbicsBankRepository bankRepository;
    private final EbicsPartnerRepository partnerRepository;
    private final EbicsUserRepository userRepository;
    private final FileTransferRepository fileTransferRepository;

    public JpaPersistenceProvider(final EbicsBankRepository bankRepository,
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
    public <T extends Identifiable> T save(final Class<T> clazz, final T object) throws IOException {
        return ((EbicsRepository<T>) findRepositoryFor(clazz)).save(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T load(final Class<T> clazz, final String id) throws IOException {
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
