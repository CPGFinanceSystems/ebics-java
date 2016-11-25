package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.Identifiable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EbicsRepository<T extends Identifiable> extends CrudRepository<T, String> {

    Class<T> getEntityType();

    default boolean canHandle(final Identifiable identifiable) {
        return identifiable.getClass().isAssignableFrom(getEntityType());
    }
}
