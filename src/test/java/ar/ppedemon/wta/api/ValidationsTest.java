package ar.ppedemon.wta.api;

import ar.ppedemon.wta.util.Base64Encoder;
import io.vertx.ext.web.api.validation.ValidationException;
import joptsimple.internal.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Validations")
class ValidationsTest {

    private static int MAX_SIZE = 1000;

    private Validations validations;
    private Base64Encoder base64Encoder;

    @BeforeEach
    void init() {
        validations = new Validations();
        base64Encoder = new Base64Encoder();
    }

    @Test
    @DisplayName("valid base64 strings must be valid")
    void validation_whenValidString_mustSucceed() {
        String valid = base64Encoder.encode("This is valid");
        Assertions.assertDoesNotThrow(() -> validations.validBase64Text(valid, MAX_SIZE));
    }

    @Test
    @DisplayName("null strings must be invalid")
    void validation_whenNullString_mustThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> validations.validBase64Text(null, MAX_SIZE));
    }

    @Test
    @DisplayName("non base64 strings must be invalid")
    void validation_whenNotBase64_mustThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> validations.validBase64Text("?*+zz", MAX_SIZE));
    }

    @Test
    @DisplayName("Strings exceeding maximum size must be invalid")
    void validation_whenStringTooLong_mustThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> validations.validBase64Text(
                Strings.repeat('a', MAX_SIZE+1), MAX_SIZE));
    }
}
