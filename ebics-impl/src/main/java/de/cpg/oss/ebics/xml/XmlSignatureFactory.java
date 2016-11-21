package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.utils.XmlUtil;
import org.w3.xmldsig.*;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

abstract class XmlSignatureFactory {

    static RSAKeyValue rsaPublicKey(final PublicKey publicKey) {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        return RSAKeyValue.builder()
                .withExponent(rsaPublicKey.getPublicExponent().toByteArray())
                .withModulus(rsaPublicKey.getModulus().toByteArray())
                .build();
    }

    static SignatureType signatureType(final byte[] digest) {
        return SignatureType.builder()
                .withSignedInfo(SignedInfo.builder()
                        .withCanonicalizationMethod(CanonicalizationMethod.builder()
                                .withAlgorithm(XmlUtil.CANONICALIZAION_METHOD)
                                .build())
                        .withSignatureMethod(SignatureMethod.builder()
                                .withAlgorithm(XmlUtil.SIGNATURE_METHOD)
                                .build())
                        .addReferences(Reference.builder()
                                .withURI("#xpointer(" + XmlUtil.XPATH_SELECTOR + ")")
                                .withTransforms(Transforms.builder()
                                        .withTransforms(Transform.builder()
                                                .withAlgorithm(XmlUtil.CANONICALIZAION_METHOD)
                                                .build())
                                        .build())
                                .withDigestMethod(DigestMethod.builder()
                                        .withAlgorithm(XmlUtil.DIGEST_METHOD)
                                        .build())
                                .withDigestValue(digest)
                                .build())
                        .build())
                .withSignatureValue(SignatureValue.builder().build())
                .build();
    }
}
