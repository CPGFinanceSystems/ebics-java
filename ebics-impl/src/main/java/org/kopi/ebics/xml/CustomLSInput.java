package org.kopi.ebics.xml;

import org.w3c.dom.ls.LSInput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

class CustomLSInput implements LSInput {

    private final BufferedInputStream inputStream;

    private String publicId;
    private String systemId;

    CustomLSInput(final String publicId, final String sysId, final InputStream input) {
        this.publicId = publicId;
        this.systemId = sysId;
        this.inputStream = new BufferedInputStream(input);
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(final String publicId) {
        this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public InputStream getByteStream() {
        return null;
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    @Override
    public Reader getCharacterStream() {
        return null;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public String getStringData() {
        synchronized (inputStream) {
            try {
                final byte[] input = new byte[inputStream.available()];
                inputStream.read(input);
                return new String(input);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void setBaseURI(final String baseURI) {
    }

    @Override
    public void setByteStream(final InputStream byteStream) {
    }

    @Override
    public void setCertifiedText(final boolean certifiedText) {
    }

    @Override
    public void setCharacterStream(final Reader characterStream) {
    }

    @Override
    public void setEncoding(final String encoding) {
    }

    @Override
    public void setStringData(final String stringData) {
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(final String systemId) {
        this.systemId = systemId;
    }
}
