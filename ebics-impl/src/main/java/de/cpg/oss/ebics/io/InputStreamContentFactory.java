package de.cpg.oss.ebics.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class InputStreamContentFactory implements ContentFactory {

    private static final long serialVersionUID = 1L;
    @Getter

    private final InputStream content;

    public static InputStreamContentFactory of(final HttpEntity httpEntity) throws IOException {
        return new InputStreamContentFactory(httpEntity.getContent());
    }
}
