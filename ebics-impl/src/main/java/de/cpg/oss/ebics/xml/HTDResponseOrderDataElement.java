package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.UserStatus;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.HTDReponseOrderDataType;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HTDResponseOrderDataElement {

    private final HTDReponseOrderDataType responseOrderData;

    public static HTDResponseOrderDataElement parse(final ContentFactory contentFactory) throws EbicsException {
        return new HTDResponseOrderDataElement(XmlUtil.parse(HTDReponseOrderDataType.class, contentFactory.getContent()));
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
}
