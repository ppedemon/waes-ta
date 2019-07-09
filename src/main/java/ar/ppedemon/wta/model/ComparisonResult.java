package ar.ppedemon.wta.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class modeling the result of a comparison. Possible configurations are:
 *
 *   - Equal objects
 *   - Objects with different length
 *   - Objects with same length but still different
 *
 *  In the last case, the comparison will include a list of {@link Span}
 *  instance specifying all differences.
 */
public class ComparisonResult {

    public enum Status {
        EQUAL, EQUAL_LENGTH, DIFFERENT_LENGTH
    }

    private final Status status;
    private final List<Span> differences;

    @JsonCreator
    public ComparisonResult(
            @JsonProperty("status") Status status,
            @JsonProperty("differences") List<Span> differences) {
        this.status = status;
        this.differences = differences;
    }

    public Status getStatus() {
        return status;
    }

    public List<Span> getDifferences() {
        return differences;
    }

    /**
     * Return whether this result represents equality.
     * @return  whether this result represents equality
     */
    @JsonIgnore
    public boolean isEqual() {
        return status == Status.EQUAL;
    }

    /**
     * Return whether this result represent not equality.
     * @return  whether this result represent not equality
     */
    @JsonIgnore
    public boolean notEqual() {
        return !isEqual();
    }
}
