package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.BankAccountInformation;
import de.cpg.oss.ebics.api.EbicsPartner;
import de.cpg.oss.ebics.api.Identifiable;
import de.cpg.oss.ebics.api.PersistenceProvider;
import javaslang.collection.Stream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

@Component
@Transactional
public class JpaPersistenceProvider implements PersistenceProvider {

    private final EbicsBankRepository bankRepository;
    private final EbicsPartnerRepository partnerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final EbicsUserRepository userRepository;
    private final FileTransferRepository fileTransferRepository;

    public JpaPersistenceProvider(final EbicsBankRepository bankRepository,
                                  final EbicsPartnerRepository partnerRepository,
                                  final BankAccountRepository bankAccountRepository,
                                  final EbicsUserRepository userRepository,
                                  final FileTransferRepository fileTransferRepository) {
        this.bankRepository = bankRepository;
        this.partnerRepository = partnerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.fileTransferRepository = fileTransferRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T save(final Class<T> clazz, final T object) throws IOException {
        if (EbicsPartner.class.isAssignableFrom(clazz)) {
            final EbicsPartner partner = (EbicsPartner) object;
            partner.getBankAccounts().forEach(bankAccountRepository::save);
        }
        return ((EbicsRepository<T>) findRepositoryFor(clazz)).save(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable> T load(final Class<T> clazz, final String id) throws IOException {
        return (T) Optional.ofNullable(findRepositoryFor(clazz).findOne(id))
                .orElseThrow(() -> new IOException(MessageFormat.format(
                        "{0} with ID {1} not found!",
                        clazz.getSimpleName(),
                        id)));
    }

    @Override
    public boolean delete(final Identifiable identifiable) throws IOException {
        return delete(identifiable.getClass(), identifiable.getId());
    }

    @Override
    public <T extends Identifiable> boolean delete(final Class<T> clazz, final String id) throws IOException {
        findRepositoryFor(clazz).delete(id);
        return true;
    }

    private <T extends Identifiable> EbicsRepository<? extends Identifiable> findRepositoryFor(final Class<T> clazz) {
        return Stream.of(bankRepository, partnerRepository, userRepository, fileTransferRepository)
                .find(r -> r.getEntityType().isAssignableFrom(clazz))
                .getOrElseThrow(() -> new IllegalArgumentException("Unknown object type " + clazz.getName()));

    }
}
