package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.security.KeyPair;

class KeyPairSerializer extends StdSerializer<KeyPair> {

    private static final long serialVersionUID = 1L;

    static final String PUBLIC_NODE_NAME = "public";
    static final String PRIVATE_NODE_NAME = "private";

    protected KeyPairSerializer(final Class<KeyPair> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(final KeyPair keyPair, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBinaryField(PRIVATE_NODE_NAME, keyPair.getPrivate().getEncoded());
        jsonGenerator.writeBinaryField(PUBLIC_NODE_NAME, keyPair.getPublic().getEncoded());
        jsonGenerator.writeEndObject();
    }
}
