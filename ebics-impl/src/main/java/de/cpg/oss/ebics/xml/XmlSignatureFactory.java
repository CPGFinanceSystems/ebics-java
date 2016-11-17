package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.utils.XmlUtil;
import org.w3.xmldsig.*;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

abstract class XmlSignatureFactory {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    static RSAKeyValue rsaPublicKey(final PublicKey publicKey) {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        final RSAKeyValue rsaKeyValue = OBJECT_FACTORY.createRSAKeyValue();

        rsaKeyValue.setExponent(rsaPublicKey.getPublicExponent().toByteArray());
        rsaKeyValue.setModulus(rsaPublicKey.getModulus().toByteArray());

        return rsaKeyValue;
    }

    static SignatureType signatureType(final byte[] digest) {
        final Transform transform = OBJECT_FACTORY.createTransform();
        transform.setAlgorithm(XmlUtil.CANONICALIZAION_METHOD);

        final DigestMethod digestMethod = OBJECT_FACTORY.createDigestMethod();
        digestMethod.setAlgorithm(XmlUtil.DIGEST_METHOD);

        final Transforms transforms = OBJECT_FACTORY.createTransforms();
        transforms.getTransforms().add(transform);

        final Reference reference = OBJECT_FACTORY.createReference();
        reference.setURI("#xpointer(" + XmlUtil.XPATH_SELECTOR + ")");
        reference.setTransforms(transforms);
        reference.setDigestMethod(digestMethod);
        reference.setDigestValue(digest);

        final SignatureMethod signatureMethod = OBJECT_FACTORY.createSignatureMethod();
        signatureMethod.setAlgorithm(XmlUtil.SIGNATURE_METHOD);

        final CanonicalizationMethod canonicalizationMethod = OBJECT_FACTORY.createCanonicalizationMethod();
        canonicalizationMethod.setAlgorithm(XmlUtil.CANONICALIZAION_METHOD);

        final SignedInfo signedInfo = OBJECT_FACTORY.createSignedInfo();
        signedInfo.setCanonicalizationMethod(canonicalizationMethod);
        signedInfo.setSignatureMethod(signatureMethod);
        signedInfo.getReferences().add(reference);

        final SignatureType signatureType = OBJECT_FACTORY.createSignatureType();
        signatureType.setSignedInfo(signedInfo);
        signatureType.setSignatureValue(OBJECT_FACTORY.createSignatureValue());

        return signatureType;
    }
}
