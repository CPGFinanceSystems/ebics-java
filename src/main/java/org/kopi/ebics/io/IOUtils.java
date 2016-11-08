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

package org.kopi.ebics.io;

import org.kopi.ebics.exception.EbicsException;

import java.io.*;


/**
 * Some IO utilities for EBICS files management.
 * EBICS server
 *
 * @author hachani
 */
public class IOUtils {

    /**
     * Creates a directory from a root one
     *
     * @param parent the parent directory
     * @param child  the directory name
     * @return The created directory
     */
    public static File createDirectory(final File parent, final String child) {
        final File directory;

        directory = new File(parent, child);
        directory.mkdir();

        return directory;
    }

    /**
     * Creates a directory from a root one
     *
     * @param parent the parent directory
     * @param child  the directory name
     * @return The created directory
     */
    public static File createDirectory(final String parent, final String child) {
        final File directory;

        directory = new File(parent, child);
        directory.mkdir();

        return directory;
    }

    /**
     * Creates a directory from a directory name
     *
     * @param name the absolute directory name
     * @return The created directory
     */
    public static File createDirectory(final String name) {
        final File directory;

        directory = new File(name);
        directory.mkdir();

        return directory;
    }

    /**
     * Creates many directories from a given full path.
     * Path should use default separator like '/' for UNIX
     * systems
     *
     * @param fullName the full absolute path of the directories
     * @return The created directory
     */
    public static File createDirectories(final String fullName) {
        final File directory;

        directory = new File(fullName);
        directory.mkdirs();

        return directory;
    }

    /**
     * Creates a new <code>java.io.File</code> from a given root.
     *
     * @param parent the parent of the file.
     * @param name   the file name.
     * @return the created file.
     */
    public static File createFile(final String parent, final String name) {
        final File file;

        file = new File(parent, name);

        return file;
    }

    /**
     * Creates a new <code>java.io.File</code> from a given root.
     *
     * @param parent the parent of the file.
     * @param name   the file name.
     * @return the created file.
     */
    public static File createFile(final File parent, final String name) {
        final File file;

        file = new File(parent, name);

        return file;
    }

    /**
     * Creates a file from its name. The name can be absolute if
     * only the directory tree is created
     *
     * @param name the file name
     * @return the created file
     */
    public static File createFile(final String name) {
        final File file;

        file = new File(name);

        return file;
    }

    /**
     * Returns the content of a file as byte array.
     *
     * @param path the file path
     * @return the byte array content of the file
     * @throws EbicsException
     */
    public static byte[] getFileContent(final String path) throws EbicsException {
        try {
            final InputStream input;
            final byte[] content;

            input = new FileInputStream(path);
            content = new byte[input.available()];
            input.read(content);
            input.close();
            return content;
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    public static byte[] read(final InputStream is) {
        try {
            try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                final byte[] b = new byte[4096];
                int n;
                while ((n = is.read(b)) != -1) {
                    output.write(b, 0, n);
                }
                return output.toByteArray();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] join(final byte[] first, final byte[] second) {
        final byte[] joined = new byte[first.length + second.length];

        System.arraycopy(first, 0, joined, 0, first.length);
        System.arraycopy(second, 0, joined, first.length, second.length);

        return joined;
    }
}
