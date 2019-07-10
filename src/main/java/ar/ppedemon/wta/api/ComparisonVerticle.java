package ar.ppedemon.wta.api;

import ar.ppedemon.wta.model.ComparisonStatus;
import ar.ppedemon.wta.model.UpsertResponse;
import ar.ppedemon.wta.service.ComparisonService;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;

import javax.inject.Inject;

/**
 * Comparison api verticle.
 *
 * @author ppedemon
 */
public class ComparisonVerticle extends RestApiVerticle {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "0.0.0.0";

    private final ComparisonService comparisonService;

    @Inject
    ComparisonVerticle(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    /**
     * Start up the Comparison rest API server.
     *
     * @param startFuture  {@link Future} used to signal success or failure of verticle startup
     * @throws Exception  any exception thrown on verticle initialization will be bubbled up
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        JsonObject config = config();

        JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setPublicKey(config.getJsonObject("auth").getString("pubkey")))
        );

        Router router = Router.router(vertx)
                .errorHandler(401, this::unauthorized)
                .errorHandler(404, this::notFound)
                .errorHandler(500, this::internalServerError);

        router.get("/swagger/*").handler(StaticHandler.create());

        router.route("/v1/*")
                .handler(JWTAuthHandler.create(jwtAuth))
                .failureHandler(this::failureHandler);

        Validations validations = new Validations();

        HTTPRequestValidationHandler sideValidator = HTTPRequestValidationHandler.create()
                .addCustomValidatorFunction(validations.base64TextValidator());

        router.put("/v1/diff/:id/left")
                .handler(BodyHandler.create())
                .handler(sideValidator)
                .handler(this::upsertLeft);

        router.put("/v1/diff/:id/right")
                .handler(BodyHandler.create())
                .handler(sideValidator)
                .handler(this::upsertRight);

        router.get("/v1/diff/:id")
                .handler(this::compare);

        router.get("/v1/diff/:id/status")
                .handler(this::status);

        router.delete("/v1/diff/:id")
                .handler(this::delete);

        int port = config.getInteger("port", DEFAULT_PORT);
        String host = config.getString("host", DEFAULT_HOST);

        vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(port, host)
                .ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);

    }

    /**
     * Upsert left hand side of an equality.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    private void upsertLeft(RoutingContext context) {
        String userId = userId(context);
        String id = context.pathParam("id");
        String data = context.getBodyAsString();

        upsertSide(context, comparisonService.upsertLeft(userId, id, data), UpsertResponse.lhsResponse(userId, id));
    }

    /**
     * Upsert right hand side of an equality.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    private void upsertRight(RoutingContext context) {
        String userId = userId(context);
        String id = context.pathParam("id");
        String data = context.getBodyAsString();

        upsertSide(context, comparisonService.upsertRight(userId, id, data), UpsertResponse.rhsResponse(userId, id));
    }

    /**
     * Upsert left or right side of a comparison.
     * @param context   {@link RoutingContext} for the HTTP interaction
     * @param upsertOp  computation making upsert
     * @param response  HTTP response payload
     */
    private void upsertSide(RoutingContext context, Single<Boolean> upsertOp, UpsertResponse response) {
        upsertOp.subscribe(
                created -> {
                    if (created) {
                        created(context, context.request().absoluteURI(), Json.encodePrettily(response));
                    } else {
                        ok(context, Json.encodePrettily(response));
                    }
                },
                error -> internalServerError(context, error)
        );
    }

    /**
     * Return comparison results for some comparison.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    private void compare(RoutingContext context) {
        String userId = userId(context);
        String id = context.pathParam("id");

        comparisonService.compare(userId, id).subscribe(
                result -> {
                    if (result.success()) {
                        ok(context, Json.encodePrettily(result.result()));
                    } else {
                        badRequest(context, result.errorMessage());
                    }
                },
                error -> internalServerError(context, error),
                () -> notFound(context)
        );
    }

    /**
     * Get status describing some comparison.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    private void status(RoutingContext context) {
        String userId = userId(context);
        String id = context.pathParam("id");

        comparisonService.get(userId, id).subscribe(
                comparison -> ok(context, Json.encodePrettily(ComparisonStatus.fromComparison(comparison))),
                error -> internalServerError(context, error),
                () -> notFound(context)
        );
    }

    /**
     * Delete a comparison.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    private void delete(RoutingContext context) {
        String userId = userId(context);
        String id = context.pathParam("id");

        comparisonService.delete(userId, id).subscribe(
                result -> {
                    if (result) {
                        noContent(context);
                    } else {
                        notFound(context);
                    }
                },
                error -> internalServerError(context, error)
        );
    }
}
