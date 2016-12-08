package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.PersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@Slf4j
@SpringBootTest
public class JpaPersistenceProviderTest extends AbstractTransactionalJUnit4SpringContextTests implements PersistenceProviderTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private PersistenceProvider persistenceProvider;

    @BeforeClass
    public static void createTestData() throws Exception {
        AbstractPersistenceProviderTest.createTestData();
    }

    @Test
    @Override
    public void testEbicsBankPersistence() throws Exception {
        persistenceProviderTest().testEbicsBankPersistence();
    }

    @Test
    @Override
    public void testEbicsPartnerPersistence() throws Exception {
        persistenceProviderTest().testEbicsPartnerPersistence();
    }

    @Test
    @Override
    public void testEbicsUserPersistence() throws Exception {
        persistenceProviderTest().testEbicsUserPersistence();
    }

    @Test
    @Override
    public void testFileTransferPersistence() throws Exception {
        persistenceProviderTest().testFileTransferPersistence();
    }

    private PersistenceProviderTest persistenceProviderTest() {
        return new AbstractPersistenceProviderTest() {
            @Override
            PersistenceProvider persistenceProvider() {
                return persistenceProvider;
            }
        };
    }
}
