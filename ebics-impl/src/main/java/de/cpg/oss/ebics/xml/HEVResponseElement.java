package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h000.EbicsHEVResponse;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HEVResponseElement implements ResponseElement<EbicsHEVResponse> {

    @Getter
    private final EbicsHEVResponse response;

    public static HEVResponseElement parse(final InputStream inputStream) throws EbicsException {
        return new HEVResponseElement(XmlUtil.parse(EbicsHEVResponse.class, inputStream));
    }

    @Override
    public Class<EbicsHEVResponse> getResponseClass() {
        return EbicsHEVResponse.class;
    }

    @Override
    public ReturnCode getReturnCode() {
        return ReturnCode.toReturnCode(response.getSystemReturnCode().getReturnCode(),
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

    public Collection<String> getSupportedEbicsVersions() {
        return response.getVersionNumbers().stream()
                .map(EbicsHEVResponse.VersionNumber::getProtocolVersion)
                .collect(Collectors.toList());
    }
}