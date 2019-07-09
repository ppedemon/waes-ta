package ar.ppedemon.wta.api;

import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.reactivex.ext.web.api.validation.CustomValidator;

import java.util.Base64;
import java.util.Objects;

/**
 * HTTP requests validations used by the {@link ChirpVerticle} class.
 */
class Validations {

    /**
     * Check whether the given text is a non null valid base64 string.
     * @param text  text to check
     * @throws ValidationException if text is null or not a valid base64 string
     */
    void validBase64Text(String text) {
        if (Objects.isNull(text)) {
            throw new ValidationException("No payload");
        }

        try {
            Base64.getDecoder().decode(text);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Provide a base64 text validator.
     * @return  base64 text validator
     */
    CustomValidator base64TextValidator() {
        return new CustomValidator(context -> validBase64Text(context.getBodyAsString("UTF-8")));
    }
}
