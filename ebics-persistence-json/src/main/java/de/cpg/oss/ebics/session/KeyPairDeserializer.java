package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

class KeyPairDeserializer extends StdDeserializer<KeyPair> {

    private static final long serialVersionUID = 1L;

    protected KeyPairDeserializer(final Class<?> clazz) {
        super(clazz);
    }

    @Override
    public KeyPair deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            final KeyFactory keyFactory = PublicKeyDeserializer.keyFactory();
            final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            return new KeyPair(
                    PublicKeyDeserializer.publicKey(keyFactory, node.get(KeyPairSerializer.PUBLIC_NODE_NAME).binaryValue()),
                    privateKey(keyFactory, node.get(KeyPairSerializer.PRIVATE_NODE_NAME).binaryValue()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JsonParseException(jsonParser, e.getLocalizedMessage(), e);
        }
    }

    private static PrivateKey privateKey(final KeyFactory keyFactory, final byte[] encoded) throws InvalidKeySpecException {
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}
