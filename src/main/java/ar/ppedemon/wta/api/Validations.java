package ar.ppedemon.wta.api;

import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.reactivex.ext.web.api.validation.CustomValidator;

import java.util.Base64;
import java.util.Objects;

/**
 * HTTP requests validations used by the {@link ComparisonVerticle} class.
 */
class Validations {

    /**
     * Check whether the given payload is a non null valid base64 string short than the given maximum length.
     *
     * @param text     text to check
     * @param maxSize  maximum payload size in bytes
     * @throws ValidationException if text is null or not a valid base64 string
     */
    void validBase64Text(String text, int maxSize) {
        if (Objects.isNull(text)) {
            throw new ValidationException("No payload");
        }

        if (text.length() > maxSize) {
            throw new ValidationException(String.format("Payload exceeded max size = %d bytes", maxSize));
        }

        try {
            Base64.getDecoder().decode(text);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Provide a base64 text validator.
     *
     * @param maxSize  maximum payload size in bytes
     * @return  base64 text validator
     */
    CustomValidator base64TextValidator(int maxSize) {
        return new CustomValidator(context -> validBase64Text(context.getBodyAsString("UTF-8"), maxSize));
    }
}
