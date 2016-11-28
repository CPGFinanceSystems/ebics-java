package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.AuthenticationVersion;
import de.cpg.oss.ebics.api.EbicsAuthenticationKey;
import de.cpg.oss.ebics.api.EbicsEncryptionKey;
import de.cpg.oss.ebics.api.EncryptionVersion;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.KeyUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HPBResponseOrderDataType;
import org.w3.xmldsig.RSAKeyValue;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HPBResponseOrderDataElement implements ResponseOrderDataElement<HPBResponseOrderDataType> {

    @Getter
    private final HPBResponseOrderDataType responseOrderData;

    public static HPBResponseOrderDataElement parse(final InputStream orderData) throws EbicsException {
        return new HPBResponseOrderDataElement(XmlUtil.parse(HPBResponseOrderDataType.class, orderData));
    }

    public EbicsAuthenticationKey getBankAuthenticationKey() throws EbicsException {
        final RSAKeyValue rsaKey = responseOrderData.getAuthenticationPubKeyInfo().getPubKeyValue().getRSAKeyValue();
        final RSAPublicKey publicKey = KeyUtil.getPublicKey(rsaKey.getModulus(), rsaKey.getExponent());
        return EbicsAuthenticationKey.builder()
                .publicKey(publicKey)
                .digest(KeyUtil.getKeyDigest(publicKey))
                .creationTime(responseOrderData.getAuthenticationPubKeyInfo().getPubKeyValue().getTimeStamp().toInstant())
                .version(AuthenticationVersion.valueOf(responseOrderData.getAuthenticationPubKeyInfo().getAuthenticationVersion()))
                .build();
    }

    public EbicsEncryptionKey getBankEncryptionKey() throws EbicsException {
        final RSAKeyValue rsaKey = responseOrderData.getEncryptionPubKeyInfo().getPubKeyValue().getRSAKeyValue();
        final RSAPublicKey publicKey = KeyUtil.getPublicKey(rsaKey.getModulus(), rsaKey.getExponent());
        return EbicsEncryptionKey.builder()
                .publicKey(publicKey)
                .digest(KeyUtil.getKeyDigest(publicKey))
                .creationTime(responseOrderData.getEncryptionPubKeyInfo().getPubKeyValue().getTimeStamp().toInstant())
                .version(EncryptionVersion.valueOf(responseOrderData.getEncryptionPubKeyInfo().getEncryptionVersion()))
                .build();
    }

    @Override
    public Class<HPBResponseOrderDataType> getResponseOrderDataClass() {
        return HPBResponseOrderDataType.class;
    }
}
