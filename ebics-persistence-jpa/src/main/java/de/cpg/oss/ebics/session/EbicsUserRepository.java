package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.EbicsUser;

public interface EbicsUserRepository extends EbicsRepository<EbicsUser> {

    @Override
    default Class<EbicsUser> getEntityType() {
        return EbicsUser.class;
    }
}
