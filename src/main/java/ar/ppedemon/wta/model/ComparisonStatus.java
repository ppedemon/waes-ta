package ar.ppedemon.wta.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Class modeling comparison status information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComparisonStatus {

    /**
     * Construct a {@link ComparisonStatus} from the given {@link Comparison}.
     *
     * @param comparison {@link Comparison} instance
     * @return  {@link ComparisonStatus} for the given comparison
     */
    public static ComparisonStatus fromComparison(Comparison comparison) {
        return new ComparisonStatus(
                comparison.getUserId(),
                comparison.getCmpId(),
                comparison.getVersion(),
                Objects.nonNull(comparison.getLeft()),
                Objects.nonNull(comparison.getRight()),
                comparison.getResult());
    }

    private final String userId;
    private final String cmpId;
    private final long version;
    private final boolean lhsReady;
    private final boolean rhsReady;
    private final ComparisonResult result;

    private ComparisonStatus(
            String userId,
            String cmpId,
            long version,
            boolean lhsReady,
            boolean rhsReady,
            ComparisonResult result) {
        this.userId = userId;
        this.cmpId = cmpId;
        this.version = version;
        this.lhsReady = lhsReady;
        this.rhsReady = rhsReady;
        this.result = result;
    }

    public String getUserId() {
        return userId;
    }

    public String getCmpId() {
        return cmpId;
    }

    public long getVersion() {
        return version;
    }

    public boolean isLhsReady() {
        return lhsReady;
    }

    public boolean isRhsReady() {
        return rhsReady;
    }

    public ComparisonResult getResult() {
        return result;
    }
}
