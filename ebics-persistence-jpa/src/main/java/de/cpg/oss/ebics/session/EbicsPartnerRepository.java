package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.EbicsPartner;

public interface EbicsPartnerRepository extends EbicsRepository<EbicsPartner> {

    @Override
    default Class<EbicsPartner> getEntityType() {
        return EbicsPartner.class;
    }
}
