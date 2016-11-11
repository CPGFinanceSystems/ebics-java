package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

class PrivateKeyDeserializer extends StdDeserializer<PrivateKey> {

    private static final long serialVersionUID = 1L;

    PrivateKeyDeserializer(final Class<?> clazz) {
        super(clazz);
    }

    @Override
    public PrivateKey deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            final KeyFactory keyFactory = PublicKeyDeserializer.keyFactory();
            return privateKey(keyFactory, jsonParser.readValueAs(byte[].class));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JsonParseException(jsonParser, e.getLocalizedMessage(), e);
        }
    }

    private static PrivateKey privateKey(final KeyFactory keyFactory, final byte[] encoded) throws InvalidKeySpecException {
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}
