package ar.ppedemon.wta.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;

import java.io.IOException;

/**
 * JWT setup utility class.
 */
public class JWTUtil {

    private final String pubKey;
    private final JWTAuth jwtAuth;

    /**
     * Create a new instance.
     * @param vertx        {@link Vertx} instance
     * @param pubKeyPath   path to public key resource
     * @param privKeyPath  path to private key resource
     */
    public JWTUtil(Vertx vertx, String pubKeyPath, String privKeyPath) {
        this.pubKey = readResourceKey(pubKeyPath);
        String privKey = readResourceKey(privKeyPath);
        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions().setAlgorithm("RS256").setPublicKey(pubKey).setSecretKey(privKey)
        ));
    }

    /**
     * Helper method: read a PEM-encoded public or private key.
     * @param path  resource path for the key
     * @return  PEM-encoded key, as a string
     */
    private String readResourceKey(String path) {
        try {
            return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPublicKey() {
        return pubKey;
    }

    /**
     * Generatte a JWT using this instance key pair. The token will have a
     * {@code subject} property set to the given user id.
     *
     * @param userId  user id to be stored in the generated JWT
     * @return JWT token with subject set to the given user id
     */
    public String token(String userId) {
        return "Bearer " + jwtAuth.generateToken(
                new JsonObject().put("sub", userId),
                new JWTOptions().setAlgorithm("RS256")
        );
    }
}
