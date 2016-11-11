package de.cpg.oss.ebics.api;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public interface MessageProvider {

    default Locale getLocale() {
        return Locale.getDefault();
    }

    default String getString(final String key, final String bundleName) {
        return getString(key, bundleName, getLocale());
    }

    default String getString(final String key, final String bundleName, final Object... params) {
        return getString(key, bundleName, getLocale(), params);
    }

    default String getString(final String key, final String bundleName, final Locale locale) {
        try {
            return ResourceBundle.getBundle(bundleName, locale).getString(key);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    default String getString(final String key, final String bundleName, final Locale locale, final Object... params) {
        try {
            return MessageFormat.format(ResourceBundle.getBundle(bundleName, locale).getString(key), params);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }
}
