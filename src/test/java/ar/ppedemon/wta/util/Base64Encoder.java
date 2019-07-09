package ar.ppedemon.wta.util;

import org.apache.commons.codec.Charsets;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Base64 encoding utilities.
 *
 * @author ppedemon
 */
public final class Base64Encoder {

    /**
     * Encode string, using UTF8.
     * @param s string to encode
     * @return  encoded base64 string
     */
    public String encode(String s) {
        return encode(s, Charsets.UTF_8);
    }

    /**
     * Encode string using given charset.
     * @param s        string to encode
     * @param charset  string charset
     * @return  encoded base64 string
     */
    public String encode(String s, Charset charset) {
        return Base64.getEncoder().encodeToString(s.getBytes(charset));
    }
}
