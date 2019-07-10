package ar.ppedemon.wta.comparator;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.model.Span;
import ar.ppedemon.wta.util.Base64Encoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Byte array comparator")
public class ByteArrayComparatorTest {

    private StringComparator comparator = new StringComparator();
    private Base64Encoder base64Encoder = new Base64Encoder();

    @Test
    @DisplayName("must correctly report equality")
    void comparison_whenEqualSides_mustReturnEqual() {
        String enc = base64Encoder.encode(new byte[] {10, 20, 30});
        Comparison c = new Comparison("1", "1").setLeft(enc).setRight(enc);
        ComparisonResult r = comparator.compare(c);
        Assertions.assertEquals(r.getStatus(), ComparisonResult.Status.EQUAL);
    }

    @Test
    @DisplayName("must correctly report inequality")
    void comparison_whenSidesWithDifferentLength_mustReturnDifferentSizes() {
        String lhs = base64Encoder.encode(new byte[] {10, 20, 30, 40});
        String rhs = base64Encoder.encode(new byte[] {10, 20});
        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);
        Assertions.assertEquals(r.getStatus(), ComparisonResult.Status.DIFFERENT_LENGTH);
    }

    @Test
    @DisplayName("must report full length span for totally different sides of equal length")
    void comparison_whenDifferentSidesWithSameLength_mustReturnSingleSpan() {
        String lhs = base64Encoder.encode(new byte[] {10, 20, 30, 40, 50});
        String rhs = base64Encoder.encode(new byte[] {11, 21, 31, 41, 51});

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(1, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0), new Span(0, 5));
    }

    @Test
    @DisplayName("must report two spans for side with to different regions")
    void comparison_whenSideWithTwoDifferentRegions_mustReportTwoSpans() {
        String lhs = base64Encoder.encode(new byte[] {10, 20, 10, 20});
        String rhs = base64Encoder.encode(new byte[] {20, 20, 20, 20});

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0), new Span(0, 1));
        Assertions.assertEquals(r.getDifferences().get(1), new Span(2, 1));
    }

    @Test
    @DisplayName("must handle border spans")
    void comparison_whenSideWithBorderDifferences_mustReportTwoSpans() {
        String lhs = base64Encoder.encode(new byte[] {10, 20, 20, 10});
        String rhs = base64Encoder.encode(new byte[] {20, 20, 20, 20});

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0), new Span(0,1));
        Assertions.assertEquals(r.getDifferences().get(1), new Span(3, 1));
    }

    @Test
    @DisplayName("must handle inner spans")
    void comparison_whenSideInnerDefferences_mustReportSpans() {
        String lhs = base64Encoder.encode(new byte[] {10, 20, 20, 10, 20, 20, 10});
        String rhs = base64Encoder.encode(new byte[] {10, 30, 30, 10, 40, 40, 10});

        Comparison c = new Comparison("1", "1").setLeft(lhs).setRight(rhs);
        ComparisonResult r = comparator.compare(c);

        Assertions.assertEquals(2, r.getDifferences().size());
        Assertions.assertEquals(r.getDifferences().get(0), new Span(1, 2));
        Assertions.assertEquals(r.getDifferences().get(1), new Span(4, 2));
    }
}
