package ar.ppedemon.wta.service;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.util.Base64Encoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("String comparator")
class StringComparatorTest {

    private StringComparator comparator = new StringComparator();
    private Base64Encoder base64Encoder = new Base64Encoder();

    @Test
    @DisplayName("must correctly report equality")
    void comparison_whenEqualSides_mustReturnEqual() {
        String enc = base64Encoder.encode("Hello world");
        Comparison c = new Comparison("1", "1").setLeft(enc).setRight(enc);
        ComparisonResult r = comparator.compare(c);
        Assertions.assertEquals(r.getStatus(), ComparisonResult.Status.EQUAL);
    }

    @Test
    @DisplayName("must correctly report inequality")
    void comparison_whenSidesWithDifferentLength_mustReturnDifferentSizes() {
        String lhs = base64Encoder.encode("Hello world");
        String rhs = base64Encoder.encode("Hello");
        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);
        Assertions.assertEquals(r.getStatus(), ComparisonResult.Status.DIFFERENT_LENGTH);
    }

    @Test
    @DisplayName("must report full length span for totally different sides of equal length")
    void comparison_whenDifferentSidesWithSameLength_mustReturnSingleSpan() {
        String lhs = base64Encoder.encode("áaaañ");
        String rhs = base64Encoder.encode("öbbbb");

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(1, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0).getOffset(), 0);
        Assertions.assertEquals(r.getDifferences().get(0).getLength(), 5);
    }

    @Test
    @DisplayName("must report two spans for side with to different regions")
    void comparison_whenSideWithTwoDifferentRegions_mustReportTwoSpans() {
        String lhs = base64Encoder.encode("abab");
        String rhs = base64Encoder.encode("bbbb");

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0).getOffset(), 0);
        Assertions.assertEquals(r.getDifferences().get(0).getLength(), 1);
        Assertions.assertEquals(r.getDifferences().get(1).getOffset(), 2);
        Assertions.assertEquals(r.getDifferences().get(1).getLength(), 1);
    }

    @Test
    @DisplayName("must handle border spans")
    void comparison_whenSideWithBorderDifferences_mustReportTwoSpans() {
        String lhs = base64Encoder.encode("abba");
        String rhs = base64Encoder.encode("bbbb");

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0).getOffset(), 0);
        Assertions.assertEquals(r.getDifferences().get(0).getLength(), 1);
        Assertions.assertEquals(r.getDifferences().get(1).getOffset(), 3);
        Assertions.assertEquals(r.getDifferences().get(1).getLength(), 1);
    }

    @Test
    @DisplayName("must handle inner spans")
    void comparison_whenSideInnerDefferences_mustReportSpans() {
        String lhs = base64Encoder.encode("abbabba");
        String rhs = base64Encoder.encode("accadda");

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0).getOffset(), 1);
        Assertions.assertEquals(r.getDifferences().get(0).getLength(), 2);
        Assertions.assertEquals(r.getDifferences().get(1).getOffset(), 4);
        Assertions.assertEquals(r.getDifferences().get(1).getLength(), 2);
    }
}
