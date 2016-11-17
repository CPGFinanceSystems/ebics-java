package de.cpg.oss.ebics.io;

import lombok.AllArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@AllArgsConstructor
class ByteArrayContentFactory implements ContentFactory {

    private static final long serialVersionUID = 1L;

    private final byte[] content;

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(content);
    }
}
