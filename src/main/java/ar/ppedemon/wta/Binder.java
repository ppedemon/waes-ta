package ar.ppedemon.wta;

import ar.ppedemon.wta.comparator.ByteArrayComparator;
import ar.ppedemon.wta.comparator.Comparator;
import ar.ppedemon.wta.data.ComparisonDao;
import ar.ppedemon.wta.data.MongoComparisonDao;
import ar.ppedemon.wta.service.ComparisonService;
import ar.ppedemon.wta.service.PersistentComparisonService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;

/**
 * Dependency injection binders.
 */
public class Binder extends AbstractModule {

    @Provides
    public ComparisonService provideComparisonService(ComparisonDao comparisonDao, Comparator comparator, Vertx vertx) {
        return new PersistentComparisonService(comparisonDao, comparator, vertx);
    }

    @Provides
    public ComparisonDao provideComparisonDao(MongoClient mongoClient) {
        return new MongoComparisonDao(mongoClient);
    }

    @Provides
    public Comparator provideComparator() {
        return new ByteArrayComparator();
    }

    @Provides
    public MongoClient provideMongoClient(Vertx vertx) {
        JsonObject config = vertx.getOrCreateContext().config().getJsonObject("mongo", new JsonObject());
        return MongoClient.createShared(vertx, config);
    }

    @Provides
    public Vertx provideRxVertx(io.vertx.core.Vertx vertx) {
        return new Vertx(vertx);
    }

    @Override
    protected void configure() {
        bind(Integer.class).annotatedWith(Names.named("maxPayloadSize")).toInstance(5 * (1 << 20));
    }
}
