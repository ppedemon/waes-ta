package ar.ppedemon.wta;

import ar.ppedemon.wta.data.ComparisonDao;
import ar.ppedemon.wta.data.MongoComparisonDao;
import ar.ppedemon.wta.service.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.commons.io.Charsets;

/**
 * Dependency injection binders.
 */
public class Binder extends AbstractModule {

    @Provides
    public ComparisonService provideComparisonService(ComparisonDao comparisonDao, Comparator comparator, Vertx vertx) {
        return new DaoComparisonService(comparisonDao, comparator, vertx);
    }

    @Provides
    public ComparisonDao provideComparisonDao(MongoClient mongoClient) {
        return new MongoComparisonDao(mongoClient);
    }

    @Provides
    public Comparator provideComparator() {
        return new StringComparator(Charsets.UTF_8);
    }

    @Provides
    public MongoClient provideMongoClient(Vertx vertx) {
        JsonObject config = vertx.getOrCreateContext().config().getJsonObject("mongo", new JsonObject());
        return MongoClient.createShared(vertx, config);
    }

    @Provides
    Vertx provideRxVertx(io.vertx.core.Vertx vertx) {
        return new Vertx(vertx);
    }

    @Override
    protected void configure() {
    }
}
