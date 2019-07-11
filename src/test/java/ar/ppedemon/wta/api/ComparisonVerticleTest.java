package ar.ppedemon.wta.api;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.model.Span;
import ar.ppedemon.wta.service.ComparisonService;
import ar.ppedemon.wta.service.ResultWrapper;
import ar.ppedemon.wta.util.Base64Encoder;
import ar.ppedemon.wta.util.JWTUtil;
import ar.ppedemon.wta.util.RandomPort;
import com.google.common.collect.Lists;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import joptsimple.internal.Strings;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("Comparison verticle")
class ComparisonVerticleTest {

    private static final String USER_ID = UUID.randomUUID().toString();

    private static JWTUtil jwtUtil;
    private static Base64Encoder base64Encoder;

    @Mock
    private ComparisonService comparisonService;

    @InjectMocks
    private ComparisonVerticle comparisonVerticle;

    @BeforeAll
    static void init(Vertx vertx) {
        jwtUtil = new JWTUtil(vertx, "auth/pubkey", "auth/privkey");
        base64Encoder = new Base64Encoder();
    }

    @BeforeEach
    void start(Vertx vertx, VertxTestContext context) {
        int port = RandomPort.get();
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/v1";

        JsonObject config = new JsonObject()
                .put("port", port)
                .put("auth", new JsonObject().put("pubkey", jwtUtil.getPublicKey()));

        RxHelper.deployVerticle(vertx, comparisonVerticle, new DeploymentOptions().setConfig(config))
                .ignoreElement()
                .subscribe(context::completeNow, context::failNow);
    }

    @AfterEach
    void stop(Vertx vertx, VertxTestContext context) {
        Flowable.fromIterable(vertx.deploymentIDs()).flatMapCompletable(vertx::rxUndeploy).subscribe(
                context::completeNow, context::failNow
        );
    }

    @Test
    @DisplayName("must give 404 for non-existent urls")
    void request_whenNonExistingUrl_mustReturn404(VertxTestContext context) {
        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/wrong")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(404)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must give 401 for unauthorized requests")
    void request_whenUnauthorized_mustReturn401(VertxTestContext context) {
        given()
                .contentType(ContentType.TEXT)
                .body(base64Encoder.encode("Hi there"))
        .when()
                .put("/diff/1/left")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(401)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must create left hand side of comparison for non-existing Id")
    void upsertLeft_whenNonExistingId_mustInsertComparison(VertxTestContext context) {
        when(comparisonService.upsertLeft(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(true));

        insertSide("1", "left", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(201)
                .header("Location", CoreMatchers.endsWith("/diff/1/left"))
                .contentType(ContentType.JSON)
                .body("userId", equalTo(USER_ID), "cmpId", equalTo("1"), "side", equalTo("left"));

        context.completeNow();
    }

    @Test
    @DisplayName("must update left hand side of comparison for existing Id")
    void upsertLeft_whenExistingId_mustUpdateComparison(VertxTestContext context) {
        when(comparisonService.upsertLeft(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(false));

        insertSide("1", "left", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(USER_ID), "cmpId", equalTo("1"), "side", equalTo("left"));

        context.completeNow();
    }

    @Test
    @DisplayName("must create right hand side of comparison for non-existing Id")
    void upsertRight_whenNonExistingId_mustInsertComparison(VertxTestContext context) {
        when(comparisonService.upsertRight(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(true));

        insertSide("1", "right", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(201)
                .header("Location", CoreMatchers.endsWith("/diff/1/right"))
                .contentType(ContentType.JSON)
                .body("userId", equalTo(USER_ID), "cmpId", equalTo("1"), "side", equalTo("right"));

        context.completeNow();
    }

    @Test
    @DisplayName("must update right hand side of comparison for existing Id")
    void upsertRight_whenExistingId_mustUpdateComparison(VertxTestContext context) {
        when(comparisonService.upsertRight(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(false));

        insertSide("1", "right", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(USER_ID), "cmpId", equalTo("1"), "side", equalTo("right"));

        context.completeNow();
    }

    @Test
    @DisplayName("must reject long inputs on the left side")
    void upsert_left_whenLeftInputTooLong_mustReturn400(VertxTestContext context) {
        int offendingSize = Constants.MAX_SIZE + 1;
        insertSide("1", "left", Strings.repeat('a', offendingSize)).then()
                .log().ifValidationFails()
         .and().assertThat()
                .statusCode(400)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must reject long inputs on the right side")
    void upsert_left_whenRightInputTooLong_mustReturn400(VertxTestContext context) {
        int offendingSize = Constants.MAX_SIZE + 1;
        insertSide("1", "right", Strings.repeat('a', offendingSize)).then()
                .log().ifValidationFails()
                .and().assertThat()
                .statusCode(400)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must handle server errors when upserting left hand side")
    void upsertLeft_whenServiceError_mustReturn500(VertxTestContext context) {
        when(comparisonService.upsertLeft(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        insertSide("1", "left", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(500)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must handle server errors when upserting right hand side")
    void upsertRight_whenServiceError_mustReturn500(VertxTestContext context) {
        when(comparisonService.upsertRight(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        insertSide("1", "right", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(500)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must return comparison results when performing a valid comparison")
    void compare_whenValidComparison_mustReturnComparisonResult(VertxTestContext context) {
        when(comparisonService.compare(anyString(), anyString()))
                .thenReturn(Maybe.just(ResultWrapper.ok(new ComparisonResult(
                        ComparisonResult.Status.DIFFERENT_LENGTH,
                        Lists.newArrayList(new Span(2, 10))
                ))));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo(ComparisonResult.Status.DIFFERENT_LENGTH.toString()))
                .body("differences", hasSize(1))
                .body("differences[0].offset", equalTo(2), "differences[0].length", equalTo(10));

        context.completeNow();
    }

    @Test
    @DisplayName("must return 404 when comparing non existent comparisons")
    void compare_whenNonExistingComparison_mustReturn404(VertxTestContext context) {
        when(comparisonService.compare(anyString(), anyString()))
                .thenReturn(Maybe.empty());

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(404)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must return 400 when comparing an invalid comparison")
    void compare_whenInvalidComparison_mustReturn400(VertxTestContext context) {
        when(comparisonService.compare(anyString(), anyString()))
                .thenReturn(Maybe.just(ResultWrapper.error("Incomplete comparison")));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(400)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must handle service errors when comparing")
    void compare_whenServiceError_mustReturn500(VertxTestContext context) {
        when(comparisonService.compare(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(500)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must return status of existing comparison")
    void getStatus_whenExistingComparison_mustReturnStatus(VertxTestContext context) {
        when(comparisonService.get(anyString(), anyString())).thenReturn(Maybe.just(
                new Comparison(USER_ID, "1")
                        .setLeft("1")
                        .setVersion(2)
                        .setResult(new ComparisonResult(ComparisonResult.Status.EQUAL, Lists.newArrayList()))));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1/status")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("lhsReady", equalTo(true), "rhsReady", equalTo(false))
                .body("result.status", equalTo(ComparisonResult.Status.EQUAL.toString()));

        context.completeNow();
    }

    @Test
    @DisplayName("must return 404 when getting status of non-existing comparison")
    void getStatus_whenNonExistingComparison_mustReturn404(VertxTestContext context) {
        when(comparisonService.get(anyString(), anyString())).thenReturn(Maybe.empty());

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1/status")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(404)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must handle service errors when getting status")
    void getStatus_whenServiceError_mustReturn500(VertxTestContext context) {
        when(comparisonService.get(anyString(), anyString())).thenThrow(new RuntimeException("Service error"));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .get("/diff/1/status")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(500)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must invoke delete service when deleting existing comparison")
    void delete_whenExistingComparison_mustInvokeDeleteService(VertxTestContext context) {
        when(comparisonService.delete(anyString(), anyString())).thenReturn(Single.just(true));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .delete("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(204);

        verify(comparisonService, times(1)).delete(anyString(), anyString());

        context.completeNow();
    }

    @Test
    @DisplayName("must return 404 when deleting non-existing comparison")
    void delete_whenNonExistingComparison_mustReturn404(VertxTestContext context) {
        when(comparisonService.delete(anyString(), anyString())).thenReturn(Single.just(false));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .delete("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(404)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must handle service errors when deleting")
    void delete_whenServiceError_mustReturn500(VertxTestContext context) {
        when(comparisonService.delete(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        given()
                .headers("Authorization", jwtUtil.token(USER_ID))
        .when()
                .delete("/diff/1")
        .then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(500)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    private Response insertSide(String id, String side, String data) {
        return given()
                .headers("Authorization", jwtUtil.token(USER_ID))
                .contentType(ContentType.TEXT)
                .body(base64Encoder.encode(data))
                .put(String.format("/diff/%s/%s", id, side))
                .thenReturn();
    }
}
