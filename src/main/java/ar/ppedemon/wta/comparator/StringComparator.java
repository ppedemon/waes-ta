package ar.ppedemon.wta.comparator;

import org.apache.commons.codec.Charsets;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * String comparator. This class will interpret the base64 encoded sides
 * in a given comparison as strings with a given encoding.
 *
 * @author ppedemon
 */
public class StringComparator extends AbstractComparator<Character> {

    private final Charset charset;

    /**
     * Build a new instance assuming base64 encoded sides are UTF8 strings.
     */
    public StringComparator() {
        this(Charsets.UTF_8);
    }

    /**
     * Construct an instance comparing base64 content by interpreting
     * such contents as strings in the given charset.
     *
     * @param charset  charset used to interpret the bytes in base64 content to compare
     */
    public StringComparator(Charset charset) {
        this.charset = charset;
    }

    @Override
    List<Character> decode(String base64Side) {
        return new String(Base64.getDecoder().decode(base64Side), charset)
                .chars().mapToObj(i -> (char)i).collect(Collectors.toList());
    }
}
