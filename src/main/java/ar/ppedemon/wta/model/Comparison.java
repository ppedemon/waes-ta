package ar.ppedemon.wta.model;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

/**
 * Class modeling a comparison of the form {@code a = b}.
 *
 * A comparison is formed by:
 *   - cmpId and userId fields: together define a unique key.
 *   - left and right hand side data.
 *   - version: updated whenever left or right hand side data is changed
 *   - result: lazily computed comparison result
 *
 * Invariant: if present, result *always* refer to comparison of the current
 * left and right hand side data. That is, we avoid the following lost-update
 * scenario:
 *
 *   1. Request for comparison reads lhs, rhs and calculates result.
 *   2. While (1) is executing, lhs/rhs are updated.
 *   3. Comparison finished and persisted. Now the result doesn't correspond to current lhs/rhs.
 *
 * @author ppedemon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comparison {

    private final String cmpId;
    private final String userId;
    private String left;
    private String right;
    private int version;
    private ComparisonResult result;

    @JsonCreator
    public Comparison(@JsonProperty("cmpId") String id, @JsonProperty("userId") String userId) {
        this.cmpId = id;
        this.userId = userId;
    }

    public String getCmpId() {
        return cmpId;
    }

    public String getUserId() {
        return userId;
    }

    public String getLeft() {
        return left;
    }

    public Comparison setLeft(String left) {
        this.left = left;
        return this;
    }

    public String getRight() {
        return right;
    }

    public Comparison setRight(String right) {
        this.right = right;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public Comparison setVersion(int version) {
        this.version = version;
        return this;
    }

    public ComparisonResult getResult() {
        return result;
    }

    public Comparison setResult(ComparisonResult result) {
        this.result = result;
        return this;
    }

    @JsonIgnore
    public boolean valid() {
        return Objects.nonNull(left) && Objects.nonNull(right);
    }
}
