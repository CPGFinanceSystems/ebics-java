package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.EbicsBank;

public interface EbicsBankRepository extends EbicsRepository<EbicsBank> {

    @Override
    default Class<EbicsBank> getEntityType() {
        return EbicsBank.class;
    }
}
