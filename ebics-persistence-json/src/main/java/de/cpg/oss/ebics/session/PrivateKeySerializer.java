package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.security.PrivateKey;

class PrivateKeySerializer extends StdSerializer<PrivateKey> {

    private static final long serialVersionUID = 1L;

    PrivateKeySerializer(final Class<PrivateKey> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(final PrivateKey privateKey, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeBinary(privateKey.getEncoded());
    }
}
