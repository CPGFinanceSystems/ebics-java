package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
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

        final SignedInfoElement signedInfo = new SignedInfoElement(XmlUtils.digest(EbicsRequest.class, ebicsRequest));
        ebicsRequest.setAuthSignature(signedInfo.build());

        final byte[] signature = XmlUtils.sign(EbicsRequest.class, ebicsRequest, session.getUser());
        ebicsRequest.getAuthSignature().getSignatureValue().setValue(signature);

        return ebicsRequest;
    }

    protected abstract EbicsRequest buildEbicsRequest() throws EbicsException;
}
