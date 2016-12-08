package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.PersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;

import java.io.File;

@Slf4j
public class JsonPersistenceProviderTest extends AbstractPersistenceProviderTest {

    private static final File TEST_DATA_DIR = new File("target/test");

    private PersistenceProvider persistenceProvider;

    @Before
    public void createSerializationManager() {
        persistenceProvider = new JsonPersistenceProvider(TEST_DATA_DIR);
    }

    @Override
    PersistenceProvider persistenceProvider() {
        return persistenceProvider;
    }
}
