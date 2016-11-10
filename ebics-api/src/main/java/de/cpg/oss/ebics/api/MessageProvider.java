package de.cpg.oss.ebics.api;

import java.util.Locale;

public interface MessageProvider {

    String getString(String key, String bundleName);

    String getString(String key, String bundleName, Object... params);

    String getString(String key, String bundleName, Locale locale);

    String getString(String key, String bundleName, Locale locale, Object... params);
}
