package de.cpg.oss.ebics.session;

public interface PersistenceProviderTest {

    void testEbicsBankPersistence() throws Exception;

    void testEbicsPartnerPersistence() throws Exception;

    void testEbicsUserPersistence() throws Exception;

    void testFileTransferPersistence() throws Exception;
}
