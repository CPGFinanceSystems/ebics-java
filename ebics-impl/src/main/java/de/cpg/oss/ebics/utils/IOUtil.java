package de.cpg.oss.ebics.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public abstract class IOUtil {

    /**
     * Creates many directories from a given full path.
     */
    public static File createDirectories(final File directory) {
        if (!directory.mkdirs()) {
            log.warn("Could not create all directories for {}", directory.getAbsolutePath());
        }
        return directory;
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

    public static InputStream wrap(final byte[] data) {
        return new ByteArrayInputStream(data);
    }
}
