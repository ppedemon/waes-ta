package ar.ppedemon.wta.data;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.ext.mongo.MongoClient;

import javax.inject.Inject;

/**
 * Comparison DAO implementation based on a Mongo database.
 *
 * We ensure the comparison invariant (comparison results aren't set or correspond
 * to the current lhs and rhs) by means of optimistic version control. Upserts
 * increment version, result updates require that version hasn't changed.
 *
 * @author ppedemn
 */
public class MongoComparisonDao implements ComparisonDao {

    private static final String COMPARISONS = "comparisons";

    private final MongoClient mongoClient;

    @Inject
    public MongoComparisonDao(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public Single<Boolean> upsertSide(String userId, String cmpId, Side side, String data) {

        /*
         * Updating a comparison side involves doing atomically:
         *
         *   1. Updating side data (duh!).
         *   2. Invalidating any prior comparison result (so comparison invariant is kept).
         *   3. Incrementing the version.
         *
         *  The effect of incrementing the version is optimistically controlling that comparison
         *  results won't be stored if any comparison side is updated while results are computed
         *  (that is, if version changed since sides were read for computing their comparison
         *  result). This way we avoid storing stale comparison results, thus preserving the
         *  Comparison invariant.
         */

        JsonObject updateSide = new JsonObject().put(side == Side.LEFT? "left" : "right", data);
        JsonObject resetResult = new JsonObject().put("result", "");
        JsonObject incVersion = new JsonObject().put("version", 1);

        return mongoClient
                .rxUpdateCollectionWithOptions(
                        COMPARISONS,
                        queryFrom(userId, cmpId),
                        new JsonObject().put("$set", updateSide).put("$unset", resetResult).put("$inc", incVersion),
                        new UpdateOptions().setUpsert(true))
                .map(opResult -> opResult.getDocMatched() == 0);
    }

    @Override
    public Maybe<Comparison> get(String userId, String cmpId) {
        return mongoClient.rxFindOne(COMPARISONS, queryFrom(userId, cmpId), new JsonObject())
                .map(json -> Json.mapper.convertValue(json, Comparison.class));
    }

    @Override
    public Single<Boolean> updateResult(Comparison comparison, ComparisonResult result) {

        // Query requires last seen version for comparison to update
        JsonObject query = queryFrom(
                comparison.getUserId(),
                comparison.getCmpId()
        ).put("version", comparison.getVersion());

        JsonObject update = new JsonObject().put("result", new JsonObject(Json.encode(result)));

        return mongoClient.rxUpdateCollection(COMPARISONS, query, new JsonObject().put("$set", update))
                .map(opResult -> opResult.getDocModified() == 1);
    }

    @Override
    public Single<Boolean> delete(String userId, String cmpId) {
        return mongoClient.rxRemoveDocument(COMPARISONS, queryFrom(userId, cmpId))
                .map(opResult -> opResult.getRemovedCount() == 1);
    }

    /**
     * Construct a query uniquely identifying a {@link ar.ppedemon.wta.model.Comparison}.
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @return  json object modeling query uniquely identifying a comparison
     */
    private JsonObject queryFrom(String userId, String cmpId) {
        return new JsonObject().put("cmpId", cmpId).put("userId", userId);
    }
}
