package ar.ppedemon.wta.integration;

import ar.ppedemon.wta.Binder;
import ar.ppedemon.wta.MainVerticle;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.util.Base64Encoder;
import ar.ppedemon.wta.util.JWTUtil;
import ar.ppedemon.wta.util.RandomPort;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(VertxExtension.class)
@DisplayName("Comparison Api")
class ComparisonIntegrationTest {

    private static final String USER_ID = UUID.randomUUID().toString();

    private static JWTUtil jwtUtil;
    private static Base64Encoder base64Encoder;

    @BeforeAll
    static void init(Vertx vertx) {
        jwtUtil = new JWTUtil(vertx, "auth/pubkey", "auth/privkey");
        base64Encoder = new Base64Encoder();
    }

    @BeforeEach
    void start(Vertx vertx, VertxTestContext context) {
        int restPort = RandomPort.get();
        int mongoPort = Integer.parseInt(System.getProperty("mongo.port"));

        RestAssured.port = restPort;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/v1";
        RestAssured.urlEncodingEnabled = false;

        JsonObject config = new JsonObject()
                .put("port", restPort)
                .put("auth", new JsonObject().put("pubkey", jwtUtil.getPublicKey()))
                .put("mongo", new JsonObject().put("host", "127.0.0.1").put("port", mongoPort))
                .put("guice_binder", Binder.class.getCanonicalName());

        RxHelper.deployVerticle(vertx, new MainVerticle(), new DeploymentOptions().setConfig(config))
                .ignoreElement()
                .andThen(resetDb(vertx, config.getJsonObject("mongo")))
                .subscribe(context::completeNow, context::failNow);
    }

    @AfterEach
    void stop(Vertx vertx, VertxTestContext context) {
        Flowable.fromIterable(vertx.deploymentIDs()).flatMapCompletable(vertx::rxUndeploy).subscribe(
                context::completeNow, __ -> context.completeNow()
        );
    }

    @Test
    @DisplayName("must insert left hand side of comparison for non-existing Id")
    void upsertLeft_whenNonExistingId_mustInsertComparison(VertxTestContext context) {
        insertSide("1", "left", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(201)
                .header("Location", endsWith("/diff/1/left"));

        context.completeNow();
    }

    @Test
    @DisplayName("must update left hand side of comparison for existing Id")
    void upsertLeft_whenExistingId_mustUpdateComparison(VertxTestContext context) {
        insertSide("1", "left", "Hi there");

        insertSide("1", "left", "Updated").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200);

        context.completeNow();
    }

    @Test
    @DisplayName("must insert right hand side of comparison for non-existing Id")
    void upsertRight_whenNonExistingId_mustInsertComparison(VertxTestContext context) {
        insertSide("1", "right", "Hi there").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(201)
                .header("Location", endsWith("/diff/1/right"));

        context.completeNow();
    }

    @Test
    @DisplayName("must update right hand side of comparison for existing Id")
    void upsertRight_whenExistingId_mustUpdateComparison(VertxTestContext context) {
        insertSide("1", "right", "Hi there");

        insertSide("1", "right", "Updated").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200);

        context.completeNow();
    }

    @Test
    @DisplayName("upserts must increment comparison version")
    void upsert_always_mustIncrementComparisonVersion(VertxTestContext context) {
        insertSide("1", "right", "Hi there");
        get("1").then().assertThat().body("version", equalTo(1));
        insertSide("1", "left", "Hi there");
        get("1").then().assertThat().body("version", equalTo(2));

        context.completeNow();
    }

    @Test
    @DisplayName("must compute result for valid comparison")
    void compare_whenValidComparison_mustReturnResult(VertxTestContext context) {
        String left = readResource("payloads/left-payload.txt");
        String right = readResource("payloads/right-payload.txt");

        insertSide("2", "left", left);
        insertSide("2", "right", right);

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL_LENGTH.toString()))
                .body("differences", hasSize(4));

        context.completeNow();
    }

    @Test
    @DisplayName("comparisons must be idempotent")
    void compare_whenRepeatedComparison_mustReturnSameResult(VertxTestContext context) {
        String left = readResource("payloads/left-payload.txt");
        String right = readResource("payloads/right-payload.txt");

        insertSide("2", "left", left);
        insertSide("2", "right", right);

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL_LENGTH.toString()))
                .body("differences", hasSize(4));

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL_LENGTH.toString()))
                .body("differences", hasSize(4));

        context.completeNow();
    }

    @Test
    @DisplayName("comparison results must be different when comparison change")
    void compare_whenChangingComparison_mustGiveDifferentResults(VertxTestContext context) {
        String left = readResource("payloads/left-payload.txt");
        String right = readResource("payloads/right-payload.txt");

        insertSide("2", "right", right);
        insertSide("2", "left", left);

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL_LENGTH.toString()))
                .body("differences", hasSize(4));

        insertSide("2", "right", "aaaaa");

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.DIFFERENT_LENGTH.toString()))
                .body("differences", hasSize(0));

        context.completeNow();
    }

    @Test
    @DisplayName("comparison results must be reset when comparison change")
    void compare_whenChangingComparison_mustResetResult(VertxTestContext context) {
        insertSide("1", "left", "left hand side");
        insertSide("1", "right", "right hand side");
        compare("1");
        get("1").then().assertThat().body("$", hasKey("result"));

        insertSide("1", "right", "rhs");
        get("1").then().assertThat().body("$", not(hasKey("result")));

        context.completeNow();
    }

    @Test
    @DisplayName("comparing on non-existing comparison must return 404")
    void compare_whenNonExistingComparison_mustReturn404(VertxTestContext context) {
        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(404)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("compare must correctly handle equal base64 binary data")
    void compare_whenEqualBinaryData_mustReturnEqualsComparison(VertxTestContext context) {
        byte[] left = readBinaryResource("payloads/stickroll.gif");
        byte[] right = readBinaryResource("payloads/stickroll.gif");

        insertSide("1", "left", left);
        insertSide("1", "right", right);

        compare("1").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL.toString()));

        context.completeNow();
    }

    @Test
    @DisplayName("compare must correctly handle different base64 binary data")
    void compare_whenDifferentBinaryData_mustReturnNotEqualsComparison(VertxTestContext context) {
        byte[] left = readBinaryResource("payloads/stickroll.gif");
        byte[] right = readBinaryResource("payloads/arrow.gif");

        insertSide("1", "left", left);
        insertSide("1", "right", right);

        compare("1").then()
                .log().ifValidationFails()
                .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.DIFFERENT_LENGTH.toString()));

        context.completeNow();
    }

    @Test
    @DisplayName("comparison must work with empty strings")
    void compare_whenEmptySides_mustReturnEqualComparison(VertxTestContext context) {
        insertSide("2", "right", "");
        insertSide("2", "left", "");

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(200)
                .body("status", equalTo(ComparisonResult.Status.EQUAL.toString()));

        context.completeNow();
    }

    @Test
    @DisplayName("comparing incomplete comparisons must return 400")
    void compare_whenIncompleteComparisons_mustReturn400(VertxTestContext context) {
        insertSide("2", "right", "");

        compare("2").then()
                .log().ifValidationFails()
        .and().assertThat()
                .statusCode(400)
                .contentType(ContentType.JSON);

        context.completeNow();
    }

    @Test
    @DisplayName("must return status of existing comparison")
    void getStatus_whenExistingComparison_mustReturnStatus(VertxTestContext context) {
        insertSide("1", "left", "Left side");

        get("1").then().log().ifValidationFails().and().assertThat()
                .statusCode(200)
                .body("version", equalTo(1))
                .body("lhsReady", equalTo(true), "rhsReady", equalTo(false));

        context.completeNow();
    }

    @Test
    @DisplayName("deleting existing comparison must remove it")
    void delete_existingComparison_mustRemove(VertxTestContext context) {
        insertSide("1", "right", "");

        delete("1").then().log().ifValidationFails().and().assertThat().statusCode(204);
        compare("1").then().assertThat().statusCode(404);

        context.completeNow();
    }

    @Test
    @DisplayName("deletion must be idempotent")
    void delete_nonExistingComparison_mustReturn404(VertxTestContext context) {
        insertSide("1", "right", "");

        delete("1").then().log().ifValidationFails().and().assertThat().statusCode(204);
        delete("1").then().log().ifValidationFails().and().assertThat().statusCode(404);

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

    private Response insertSide(String id, String side, byte[] data) {
        return given()
                .headers("Authorization", jwtUtil.token(USER_ID))
                .contentType(ContentType.TEXT)
                .body(base64Encoder.encode(data))
                .put(String.format("/diff/%s/%s", id, side))
                .thenReturn();
    }

    private Response compare(String id) {
        return given()
                .headers("Authorization", jwtUtil.token(USER_ID))
                .get(String.format("/diff/%s", id))
                .thenReturn();
    }

    private Response get(String id) {
        return given()
                .headers("Authorization", jwtUtil.token(USER_ID))
                .get(String.format("/diff/%s/status", id))
                .thenReturn();
    }

    private Response delete(String id) {
        return given()
                .headers("Authorization", jwtUtil.token(USER_ID))
                .delete(String.format("/diff/%s", id))
                .thenReturn();
    }

    private String readResource(String path) {
        try {
            return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readBinaryResource(String path) {
        try {
            return Resources.toByteArray(Resources.getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Completable resetDb(Vertx vertx, JsonObject config) {
        MongoClient client = MongoClient.createNonShared(vertx, config);
        return client.rxGetCollections().flatMapCompletable(
                cols -> Completable.concat(cols.stream().map(client::rxDropCollection).collect(Collectors.toList()))
        );
    }
}
