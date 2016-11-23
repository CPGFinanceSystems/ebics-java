package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.UserStatus;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HKDResponseOrderDataType;
import org.ebics.h004.UserInfoType;

import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HKDResponseOrderDataElement implements ResponseOrderDataElement<HKDResponseOrderDataType> {

    @Getter
    private final HKDResponseOrderDataType responseOrderData;

    public static HKDResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HKDResponseOrderDataElement(XmlUtil.parse(HKDResponseOrderDataType.class, orderDataXml));
    }

    /**
     * @return a map with user IDs as key and the user status as value
     */
    public Map<String, UserStatus> getUserStatus() {
        return responseOrderData.getUserInfos().stream()
                .map(UserInfoType::getUserID)
                .map(userId -> new SimpleImmutableEntry<>(userId.getValue(), UserStatus.fromEbicsStatus(userId.getStatus())))
                .collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));
    }

    /**
     * @return a map with user IDs as key and a list of order types as value
     */
    public Map<String, Collection<String>> getPermittedUserOrderTypes() {
        return responseOrderData.getUserInfos().stream()
                .map(userInfo -> new SimpleImmutableEntry<String, Collection<String>>(
                        userInfo.getUserID().getValue(),
                        userInfo.getPermissions().stream().flatMap(permission -> permission.getOrderTypes().stream())
                                .collect(Collectors.toList())))
                .collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));
    }

    @Override
    public Class<HKDResponseOrderDataType> getResponseOrderDataClass() {
        return HKDResponseOrderDataType.class;
    }
}