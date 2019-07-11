package ar.ppedemon.wta.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Helper class for reading resources.
 */
public class ResourceReader {

    /**
     * Read resource from class path, returning contents as string.
     * This method assumes UTF8 encoding.
     *
     * @param path  path relative to current classpath
     * @return  resource contents, as string
     * @throws RuntimeException  if resource can't be found or read fails
     */
    public String readResource(String path) {
        return readResource(path, Charsets.UTF_8);
    }

    /**
     * Read resource from class path, returning contents as string assuming the given charset.
     *
     * @param path     path relative to current classpath
     * @param charset  charset to be used for reading resource
     * @return  resource contents, as string
     * @throws RuntimeException  if resource can't be found or read fails
     */
    public String readResource(String path, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(path), charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read resource from class path, returning contents as a byte array.
     *
     * @param path  path relative to current classpath
     * @return  resource contents, as byte array
     * @throws RuntimeException  if resource can't be found or read fails
     */
    public byte[] readBinaryResource(String path) {
        try {
            return Resources.toByteArray(Resources.getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
