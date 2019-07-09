package ar.ppedemon.wta;

import ar.ppedemon.wta.api.ComparisonVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

/**
 * Verticle representing the entry point for the whole application.
 *
 * <p>This class is responsible for properly configuring and deploying
 * the verticles implementing the rest APIs provided by this project.
 */
public class MainVerticle extends AbstractVerticle {

    private static String DEPLOYMENT_PREFIX = "java-guice:";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config());
        String deploymentName = DEPLOYMENT_PREFIX + ComparisonVerticle.class.getName();

        vertx.deployVerticle(deploymentName, deploymentOptions, ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }
}
