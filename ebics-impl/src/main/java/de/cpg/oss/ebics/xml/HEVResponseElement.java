package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h000.EbicsHEVResponse;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HEVResponseElement implements ResponseElement<EbicsHEVResponse> {

    @Getter
    private final EbicsHEVResponse response;

    public static HEVResponseElement parse(final InputStream inputStream) {
        return new HEVResponseElement(XmlUtil.parse(EbicsHEVResponse.class, inputStream));
    }

    @Override
    public Class<EbicsHEVResponse> getResponseClass() {
        return EbicsHEVResponse.class;
    }

    @Override
    public ReturnCode getReturnCode() {
        return ReturnCode.valueOf(response.getSystemReturnCode().getReturnCode(),
                response.getSystemReturnCode().getReportText());
    }

    @Override
    public byte[] getOrderData() {
        return new byte[0];
    }

    @Override
    public byte[] getTransactionKey() {
        return new byte[0];
    }

    public Set<String> getSupportedEbicsVersions() {
        return response.getVersionNumbers().stream()
                .map(EbicsHEVResponse.VersionNumber::getProtocolVersion)
                .collect(Collectors.toSet());
    }
}
