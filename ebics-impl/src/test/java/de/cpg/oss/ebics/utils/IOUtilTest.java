package de.cpg.oss.ebics.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class IOUtilTest {

    @Test
    public void testRead() throws Exception {
        final String input = "some random input";
        final InputStream inputStream = new ByteArrayInputStream(input.getBytes());

        final String content = new String(IOUtil.read(inputStream));

        assertThat(content).isEqualTo(input);
    }

    @Test
    public void testWrap() throws Exception {
        final byte[] data = "some data".getBytes();

        assertThat(IOUtil.wrap(data)).hasContentEqualTo(new ByteArrayInputStream(data));
    }
}
