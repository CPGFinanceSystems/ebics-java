package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.ObjectFactory;


@Slf4j
public abstract class EbicsRequestElement {

    protected static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    protected final EbicsSession session;

    public EbicsRequestElement(final EbicsSession session) {
        this.session = session;
    }

    public EbicsRequest build() throws EbicsException {
        final EbicsRequest ebicsRequest = buildEbicsRequest();

        ebicsRequest.setAuthSignature(XmlSignatureFactory.signatureType(
                XmlUtil.digest(EbicsRequest.class, ebicsRequest)));
        ebicsRequest.getAuthSignature().getSignatureValue().setValue(
                XmlUtil.sign(EbicsRequest.class, ebicsRequest, session.getUser()));

        return ebicsRequest;
    }

    protected abstract EbicsRequest buildEbicsRequest() throws EbicsException;
}
