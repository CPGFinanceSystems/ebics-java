package org.kopi.ebics.exception;

import de.cpg.oss.ebics.api.exception.ReturnCode;
import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class ReturnCodeTest {

    @Test
    public void testNoDuplicateReturnCodes() {
        assertThat(Stream.of(ReturnCode.values()).map(ReturnCode::getCode).distinct().count())
                .isEqualTo(ReturnCode.values().length - 1); // one code is duplicated by specification
    }
}
