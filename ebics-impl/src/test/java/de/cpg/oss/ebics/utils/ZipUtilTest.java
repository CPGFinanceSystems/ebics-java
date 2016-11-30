package de.cpg.oss.ebics.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipUtilTest {

    @Test
    public void testCompression() throws Exception {
        final String input = "some random input";
        final InputStream inputStream = new ByteArrayInputStream(input.getBytes());

        final InputStream compressed = ZipUtil.compress(inputStream);
        assertThat(compressed).isNotEqualTo(inputStream);

        final byte[] uncompressed = IOUtil.read(ZipUtil.uncompress(compressed));
        assertThat(uncompressed).isEqualTo(input.getBytes());
    }
}
