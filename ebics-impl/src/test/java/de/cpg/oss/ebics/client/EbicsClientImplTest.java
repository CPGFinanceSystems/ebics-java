package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsClient;
import de.cpg.oss.ebics.api.EbicsConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class EbicsClientImplTest {

    private EbicsClient ebicsClient;

    @Before
    public void createTestClient() throws Exception {
        ebicsClient = new EbicsClientImpl(new EbicsConfiguration());
    }

    @Test
    public void testBankSupportedVersions() throws Exception {
        try {
            ebicsClient.bankSupportedEbicsVersions("HOSTID", URI.create("http://localhost:4711"));
            throw new IllegalStateException("Expected method to throw connection exception");
        } catch (final RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(ConnectException.class);
        }
    }
}
