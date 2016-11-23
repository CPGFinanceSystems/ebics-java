package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.UserStatus;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HTDReponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HTDResponseOrderDataElement implements ResponseOrderDataElement<HTDReponseOrderDataType> {

    @Getter
    private final HTDReponseOrderDataType responseOrderData;

    public static HTDResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HTDResponseOrderDataElement(XmlUtil.parse(HTDReponseOrderDataType.class, orderDataXml));
    }

    public UserStatus getUserStatus() {
        return UserStatus.fromEbicsStatus(responseOrderData.getUserInfo().getUserID().getStatus());
    }

    /**
     * @return a list of {@link de.cpg.oss.ebics.api.OrderType} as string list
     */
    public Collection<String> getPermittedUserOrderTypes() {
        return responseOrderData.getUserInfo().getPermissions().stream()
                .flatMap(permission -> permission.getOrderTypes().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Class<HTDReponseOrderDataType> getResponseOrderDataClass() {
        return HTDReponseOrderDataType.class;
    }
}