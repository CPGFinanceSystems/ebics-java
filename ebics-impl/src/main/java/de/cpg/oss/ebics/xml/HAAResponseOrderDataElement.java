package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HAAResponseOrderDataType;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HAAResponseOrderDataElement implements ResponseOrderDataElement<HAAResponseOrderDataType> {

    @Getter
    private final HAAResponseOrderDataType responseOrderData;

    public static HAAResponseOrderDataElement parse(final InputStream orderDataXml) {
        return new HAAResponseOrderDataElement(XmlUtil.parse(HAAResponseOrderDataType.class, orderDataXml));
    }

    public Set<String> getSupportedOrderTypes() {
        return Collections.unmodifiableSet(new HashSet<>(responseOrderData.getOrderTypes()));
    }

    @Override
    public Class<HAAResponseOrderDataType> getResponseOrderDataClass() {
        return HAAResponseOrderDataType.class;
    }
}
