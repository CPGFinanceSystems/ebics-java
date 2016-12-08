package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.ebics.s001.*;

import javax.xml.bind.JAXBElement;
import java.time.ZoneOffset;
import java.util.function.Supplier;

public abstract class EbicsSignatureXmlFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public static SignaturePubKeyOrderData signaturePubKeyOrderData(final EbicsSession session) throws EbicsException {
        final PubKeyValueType pubKeyValue = OBJECT_FACTORY.createPubKeyValueType();
        pubKeyValue.setRSAKeyValue(XmlSignatureFactory.rsaPublicKey(session.getUser().getSignatureKey().getPublicKey()));
        pubKeyValue.setTimeStamp(session.getUser().getSignatureKey().getCreationTime().atOffset(ZoneOffset.UTC));

        final SignaturePubKeyInfo signaturePubKeyInfo = OBJECT_FACTORY.createSignaturePubKeyInfo();
        signaturePubKeyInfo.setPubKeyValue(pubKeyValue);
        signaturePubKeyInfo.setSignatureVersion(session.getUser().getSignatureKey().getVersion());

        final SignaturePubKeyOrderData signaturePubKeyOrderData = OBJECT_FACTORY.createSignaturePubKeyOrderData();
        signaturePubKeyOrderData.setSignaturePubKeyInfo(signaturePubKeyInfo);
        signaturePubKeyOrderData.setPartnerID(session.getPartner().getId());
        signaturePubKeyOrderData.setUserID(session.getUser().getId());

        return signaturePubKeyOrderData;
    }

    static JAXBElement<UserSignatureDataSigBookType> userSignature(
            final EbicsUser user,
            final String partnerId,
            final Supplier<byte[]> signatureSupplier) {
        final OrderSignatureData orderSignatureData = OBJECT_FACTORY.createOrderSignatureData();
        orderSignatureData.setSignatureVersion(user.getSignatureKey().getVersion());
        orderSignatureData.setPartnerID(partnerId);
        orderSignatureData.setUserID(user.getId());
        orderSignatureData.setSignatureValue(signatureSupplier.get());

        final UserSignatureDataSigBookType userSignatureData = OBJECT_FACTORY.createUserSignatureDataSigBookType();
        userSignatureData.getOrderSignaturesAndOrderSignatureDatas().add(orderSignatureData);

        return OBJECT_FACTORY.createUserSignatureData(userSignatureData);
    }
}
