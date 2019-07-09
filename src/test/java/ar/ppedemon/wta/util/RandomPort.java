package ar.ppedemon.wta.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility for randomly probing available ports during testing.
 */
public class RandomPort {

    /**
     * Get random available port.
     *
     * @return random available port
     * @throws RuntimeException if port lookup fails
     */
    public static int get() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
