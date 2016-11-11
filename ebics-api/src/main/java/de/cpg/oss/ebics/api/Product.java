package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Locale;

/**
 * Optional information about the client product.
 */
@Value
@Builder
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final String name;
    @NonNull
    private final Locale locale;
    private final String instituteId;

    public String getLanguage() {
        return getLocale().getCountry();
    }
}
