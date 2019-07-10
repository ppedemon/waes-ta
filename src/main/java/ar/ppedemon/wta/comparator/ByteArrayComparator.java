package ar.ppedemon.wta.comparator;

import com.google.common.primitives.Bytes;

import java.util.Base64;
import java.util.List;

/**
 * Byte array comparator. This class will interpret the base64 encoded sides
 * in a given comparison as a byte array.
 *
 * @author ppedemon
 */
public class ByteArrayComparator extends AbstractComparator<Byte> {
    @Override
    List<Byte> decode(String base64Text) {
        return Bytes.asList(Base64.getDecoder().decode(base64Text));
    }
}
