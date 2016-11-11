package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.security.PublicKey;

class PublicKeySerializer extends StdSerializer<PublicKey> {

    private static final long serialVersionUID = 1L;

    static final String PUBLIC_NODE_NAME = "public";

    protected PublicKeySerializer(final Class<PublicKey> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(final PublicKey publicKey, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBinaryField(PUBLIC_NODE_NAME, publicKey.getEncoded());
        jsonGenerator.writeEndObject();
    }
}
