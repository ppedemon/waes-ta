package ar.ppedemon.wta.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Span in a text: offset plus length.
 *
 * @author ppedemon
 */
public class Span {

    private final int offset;
    private final int length;

    @JsonCreator
    public Span(@JsonProperty("offset") int offset, @JsonProperty("length") int length) {
        this.offset = offset;
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Span)) {
            return false;
        }

        Span other = (Span)obj;
        return other.getOffset() == offset && other.getLength() == length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }
}
