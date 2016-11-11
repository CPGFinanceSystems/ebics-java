package de.cpg.oss.ebics.io;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class IOUtils {

    /**
     * Creates many directories from a given full path.
     */
    public static File createDirectories(final File directory) {
        if (!directory.mkdirs()) {
            log.warn("Could not create all directories for {}", directory.getAbsolutePath());
        }
        return directory;
    }

    /**
     * Returns the content of a file as byte array.
     *
     * @param path the file path
     * @return the byte array content of the file
     */
    public static byte[] getFileContent(final String path) throws IOException {
        try (final InputStream inputStream = new FileInputStream(path)) {
            return read(inputStream);
        }
    }

    public static byte[] read(final InputStream is) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            final byte[] b = new byte[4096];
            int n;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        }
    }
}
