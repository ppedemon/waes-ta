package ar.ppedemon.wta.api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract verticle factoring out common functionality for Chirp rest APIs.
 */
abstract class RestApiVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Provide logger instantiated for some subclass.
     * @return  logger instantiated for some subclass
     */
    Logger logger() {
        return logger;
    }

    /**
     * Interceptor failurre handler detecting {@link ValidationException} failures,
     * which will be reported as bad requests. Any other error is delegated down
     * the interceptor chain.
     *
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    void failureHandler(RoutingContext context) {
        Throwable error = context.failure();
        if (error instanceof ValidationException) {
            badRequest(context, error.getMessage());
        } else {
            context.next();
        }
    }

    /**
     * Answer a 200 OK response with some payload.
     * @param context   {@link RoutingContext} for the HTTP interaction
     * @param response  String payload to include in response
     */
    void ok(RoutingContext context, String response) {
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(response);
    }

    /**
     * Answer a 201 CREATED response with some payload and setting the {@code Location} header.
     *
     * @param context   {@link RoutingContext} for the HTTP interaction
     * @param location  value for {@code Location} header
     * @param response  String payload to include in response
     */
    void created(RoutingContext context, String location, String response) {
        context.response()
                .setStatusCode(201)
                .putHeader("Location", location)
                .putHeader("content-type", "application/json")
                .end(response);
    }

    /**
     * Answer a 204 NO CONTENT response.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    void noContent(RoutingContext context) {
        context.response().setStatusCode(204).end();
    }

    /**
     * Answer a 404 NOT FOUND response in json format.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    void notFound(RoutingContext context) {
        fail(context, 404, "Not found");
    }

    /**
     * Answer a 401 UNAUTHORIZED response in json format.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    void unauthorized(RoutingContext context) {
        fail(context, 401, "Unauthorized");
    }

    /**
     * Answer a 400 BAD REQUEST response in json format.
     *
     * @param context  {@link RoutingContext} for the HTTP interaction
     * @param message  error message to include in json response payload
     */
    void badRequest(RoutingContext context, String message) {
        fail(context, 400, message);
    }

    /**
     * Answer a 500 INTERNAL SERVER ERROR response in json format.
     * @param context  {@link RoutingContext} for the HTTP interaction
     */
    void internalServerError(RoutingContext context) {
        internalServerError(context, context.failure());
    }

    /**
     * Answer a 500 INTERNAL SERVER ERROR response in json format, extracting
     * error message from an exception.
     *
     * @param context  {@link RoutingContext} for the HTTP interaction
     * @param error    exception that caused the error
     */
    void internalServerError(RoutingContext context, Throwable error) {
        logger.error("Internal server error", error);
        fail(context, 500, error.getMessage());
    }

    /**
     * Return in json forma for some HTTP failure.
     * @param context  {@link RoutingContext} for the HTTP interaction
     * @param status   response status
     * @param message  message to include in json payload
     */
    private void fail(RoutingContext context, int status, String message) {
        context.response()
                .setStatusCode(status)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", message).encode());
    }

    /**
     * Extract user id from the request's {@code Authorization} token. This method
     * assumes that the request was made by an authenticated user.
     *
     * @param context  {@link RoutingContext} for the HTTP interaction, including authenticated user
     * @return  user id
     */
    String userId(RoutingContext context) {
        return context.user().principal().getString("sub");
    }
}
