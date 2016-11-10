/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.MessageProvider;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A mean to manage application messages.
 *
 * @author Hachani
 */
public class DefaultMessageProvider implements MessageProvider {

    private final Locale locale;

    public DefaultMessageProvider(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getString(final String key, final String bundleName) {
        return getString(key, bundleName, locale);
    }

    @Override
    public String getString(final String key, final String bundleName, final Object... params) {
        return getString(key, bundleName, locale, params);
    }

    @Override
    public String getString(final String key, final String bundleName, final Locale locale) {
        try {
            return ResourceBundle.getBundle(bundleName, locale).getString(key);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    @Override
    public String getString(final String key, final String bundleName, final Locale locale, final Object... params) {
        try {
            return MessageFormat.format(ResourceBundle.getBundle(bundleName, locale).getString(key), params);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }
}
