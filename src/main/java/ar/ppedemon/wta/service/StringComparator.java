package ar.ppedemon.wta.service;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.model.Span;
import com.google.common.collect.Lists;
import org.apache.commons.codec.Charsets;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * String comparator. This class will interpret the base64 encoded sides
 * in the given {@link Comparison} as strings with a given encoding.
 *
 * @author ppedemon
 */
public class StringComparator implements Comparator {

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
    public ComparisonResult compare(Comparison comparison) {
        Objects.requireNonNull(comparison.getLeft(), "Left hand side of comparison is null");
        Objects.requireNonNull(comparison.getRight(), "Right hand side of comparison is null");

        String lhs = decode(comparison.getLeft());
        String rhs = decode(comparison.getRight());

        if (lhs.equals(rhs)) {
            return new ComparisonResult(ComparisonResult.Status.EQUAL, Lists.newArrayList());
        }

        if (lhs.length() != rhs.length()) {
            return new ComparisonResult(ComparisonResult.Status.DIFFERENT_LENGTH, Lists.newArrayList());
        }

        return new ComparisonResult(ComparisonResult.Status.EQUAL_LENGTH, differences(lhs, rhs));
    }

    /**
     * Base64-decode the given side contents, interpreting them as a string
     * using this instance's charset.
     *
     * @param base64Side  data to base64-decode
     * @return  base64-decoded string
     */
    private String decode(String base64Side) {
        return new String(Base64.getDecoder().decode(base64Side), charset);
    }

    /**
     * Compute list of differences for two different strings of equal length.
     * @param lhs  lhs string
     * @param rhs  rhs string
     * @return list of {@link Span} instances
     */
    private List<Span> differences(String lhs, String rhs) {
        List<Span> differences = Lists.newArrayList();

        int diffOffset = 0;
        boolean inDiff = false;

        for (int i = 0; i < lhs.length(); i++) {
            char lc = lhs.charAt(i);
            char rc = rhs.charAt(i);

            if (!inDiff && lc != rc) {
                inDiff = true;
                diffOffset = i;
            }

            if (inDiff && lc == rc) {
                inDiff = false;
                differences.add(new Span(diffOffset, i - diffOffset));
            }
        }

        if (inDiff) {
            differences.add(new Span(diffOffset, lhs.length() - diffOffset));
        }

        return differences;
    }
}
