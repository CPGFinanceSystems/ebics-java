package de.cpg.oss.ebics.utils;

import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.MutableHeaderType;
import org.ebics.h004.StaticHeaderType;
import org.ebics.h004.TransactionPhaseType;
import org.junit.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class XmlUtilTest {

    @Test
    public void testPrettyPrint() throws Exception {
        final EbicsRequest request = EbicsRequest.builder()
                .withHeader(EbicsRequest.Header.builder()
                        .withAuthenticate(true)
                        .withMutable(MutableHeaderType.builder()
                                .withTransactionPhase(TransactionPhaseType.INITIALISATION)
                                .build())
                        .withStatic(StaticHeaderType.builder()
                                .withHostID("HOSTID")
                                .withNonce("nonce".getBytes())
                                .withPartnerID("PARTNERID")
                                .withTimestamp(OffsetDateTime.parse("2016-11-29T16:15:27.91+01:00"))
                                .build())
                        .build())
                .withBody(EbicsRequest.Body.builder().build())
                .build();

        assertThat(XmlUtil.prettyPrint(EbicsRequest.class, request))
                .hasContentEqualTo(XmlUtilTest.class.getResourceAsStream("/ebicsRequest.xml"));
    }

    @Test
    public void testValidate() throws Exception {
        XmlUtil.validate(IOUtil.read(XmlUtilTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml")));
    }

    @Test
    public void testParse() throws Exception {
        final EbicsRequest ebicsRequest = XmlUtil.parse(
                EbicsRequest.class,
                XmlUtilTest.class.getResourceAsStream("/ebicsRequest.xml"));

        assertThat(ebicsRequest.getHeader().isAuthenticate());
        assertThat(ebicsRequest.getHeader().getStatic().getHostID()).isEqualTo("HOSTID");
        assertThat(ebicsRequest.getHeader().getStatic().getPartnerID()).isEqualTo("PARTNERID");
        assertThat(ebicsRequest.getHeader().getMutable().getTransactionPhase())
                .isEqualTo(TransactionPhaseType.INITIALISATION);
    }
}
