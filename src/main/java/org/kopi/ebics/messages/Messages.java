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

package org.kopi.ebics.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A mean to manage application messages.
 *
 * @author Hachani
 */
public abstract class Messages {

    /**
     * Return the corresponding value of a given key and string parameter.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @param param      the parameter
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName, final String param) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return MessageFormat.format(resourceBundle.getString(key), param);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Return the corresponding value of a given key and integer parameter.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @param param      the parameter
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName, final int param) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return MessageFormat.format(resourceBundle.getString(key), param);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Return the corresponding value of a given key and parameters.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return resourceBundle.getString(key);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Return the corresponding value of a given key and string parameter.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @param locale     the bundle locale
     * @param param      the parameter
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName, final Locale locale, final String param) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return MessageFormat.format(resourceBundle.getString(key), param);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Return the corresponding value of a given key and integer parameter.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @param locale     the bundle locale
     * @param param      the parameter
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName, final Locale locale, final int param) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return MessageFormat.format(resourceBundle.getString(key), param);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Return the corresponding value of a given key and parameters.
     *
     * @param key        the given key
     * @param bundleName the bundle name
     * @param locale     the bundle locale
     * @return the corresponding key value
     */
    public static String getString(final String key, final String bundleName, final Locale locale) {
        try {
            final ResourceBundle resourceBundle;

            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
            return resourceBundle.getString(key);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Sets the default locale.
     *
     * @param locale the locale
     */
    public static void setLocale(final Locale locale) {
        Messages.locale = locale;
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private static Locale locale;
}
