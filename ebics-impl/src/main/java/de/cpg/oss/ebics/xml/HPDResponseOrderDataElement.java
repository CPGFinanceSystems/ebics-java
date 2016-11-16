package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HPDResponseOrderDataType;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HPDResponseOrderDataElement {

    private final HPDResponseOrderDataType responseOrderData;

    public static HPDResponseOrderDataElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new HPDResponseOrderDataElement(XmlUtil.parse(HPDResponseOrderDataType.class, contentFactory.getContent()));
    }

    public String getBankName() throws EbicsException {
        return responseOrderData.getAccessParams().getInstitute();
    }

    public String[] getSupportedSignatureVersions() throws EbicsException {
        final List<String> versions = responseOrderData.getProtocolParams().getVersion().getSignature();
        return versions.toArray(new String[versions.size()]);
    }
}
