package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HAAResponseOrderDataType;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HAAResponseOrderDataElement {

    private final HAAResponseOrderDataType responseOrderData;

    public static HAAResponseOrderDataElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new HAAResponseOrderDataElement(XmlUtil.parse(HAAResponseOrderDataType.class, contentFactory.getContent()));
    }

    public Collection<String> getSupportedOrderTypes() {
        return Collections.unmodifiableCollection(responseOrderData.getOrderTypes());
    }
}
