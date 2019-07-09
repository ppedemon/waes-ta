package ar.ppedemon.wta.api;

import ar.ppedemon.wta.util.Base64Encoder;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Validations")
class ValidationsTest {

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
        Assertions.assertDoesNotThrow(() -> validations.validBase64Text(valid));
    }

    @Test
    @DisplayName("null strings must be invalid")
    void validation_whenNullString_mustThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> validations.validBase64Text(null));
    }

    @Test
    @DisplayName("non base64 strings must be invalid")
    void validation_whenNotBase64_mustThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> validations.validBase64Text("?*+zz"));
    }
}
