package ar.ppedemon.wta.comparator;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.model.Span;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Abstract comparator. Algorithm is here, it's up to subclasses
 * how to interpret the meaning of a base64 encoded string.
 *
 * @param <T> interpreted type for each character in base64 contents to compare
 *
 * @author ppedemon
 */
public abstract class AbstractComparator<T> implements Comparator {

    /**
     * Define how to interpret a given base64 encoded text. That is, subclasses are responsible
     * for defining the semantics of a base64 encoded text chunk.
     *
     * @param base64Text  base64 encoded text chunk to decode
     * @return  decoding result, as a list of elements of type {@code T} (one for each character of the input)
     */
    abstract List<T> decode(String base64Text);

    @Override
    public ComparisonResult compare(Comparison comparison) {
        Objects.requireNonNull(comparison.getLeft(), "Left hand side of comparison is null");
        Objects.requireNonNull(comparison.getRight(), "Right hand side of comparison is null");

        List<T> lhs = decode(comparison.getLeft());
        List<T> rhs = decode(comparison.getRight());

        if (lhs.equals(rhs)) {
            return new ComparisonResult(ComparisonResult.Status.EQUAL, Lists.newArrayList());
        }

        if (lhs.size() != rhs.size()) {
            return new ComparisonResult(ComparisonResult.Status.DIFFERENT_LENGTH, Lists.newArrayList());
        }

        return new ComparisonResult(ComparisonResult.Status.EQUAL_LENGTH, differences(lhs, rhs));
    }

    /**
     * Compute list of differences for two different strings of equal length.
     *
     * @param lhs  lhs side of comparison
     * @param rhs  rhs side of comparison
     * @return list of {@link Span} instances
     */
    private List<Span> differences(List<T> lhs, List<T> rhs) {
        List<Span> differences = Lists.newArrayList();

        int diffOffset = 0;
        boolean inDiff = false;

        for (int i = 0; i < lhs.size(); i++) {
            T left = lhs.get(i);
            T right = rhs.get(i);

            if (!inDiff && !left.equals(right)) {
                inDiff = true;
                diffOffset = i;
            }

            if (inDiff && left.equals(right)) {
                inDiff = false;
                differences.add(new Span(diffOffset, i - diffOffset));
            }
        }

        if (inDiff) {
            differences.add(new Span(diffOffset, lhs.size() - diffOffset));
        }

        return differences;
    }
}
