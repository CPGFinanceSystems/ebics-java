package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.ebics.h004.EbicsRequest;

interface EbicsRequestElement {

    EbicsRequest createForSigning(EbicsSession session) throws EbicsException;

    default EbicsRequest create(final EbicsSession session) throws EbicsException {
        return sign(createForSigning(session), session.getUser());
    }

    static EbicsRequest sign(final EbicsRequest requestToSign, final EbicsUser user) throws EbicsException {
        requestToSign.setAuthSignature(XmlSignatureFactory.signatureType(
                XmlUtil.digest(EbicsRequest.class, requestToSign)));
        requestToSign.getAuthSignature().getSignatureValue().setValue(
                XmlUtil.sign(EbicsRequest.class, requestToSign, user));
        return requestToSign;
    }
}
