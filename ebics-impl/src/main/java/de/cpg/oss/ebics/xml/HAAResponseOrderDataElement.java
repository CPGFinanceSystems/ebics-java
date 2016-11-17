package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HAAResponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HAAResponseOrderDataElement implements ResponseOrderDataElement {

    private final HAAResponseOrderDataType responseOrderData;

    public static HAAResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HAAResponseOrderDataElement(XmlUtil.parse(HAAResponseOrderDataType.class, orderDataXml));
    }

    public Collection<String> getSupportedOrderTypes() {
        return Collections.unmodifiableCollection(responseOrderData.getOrderTypes());
    }
}
