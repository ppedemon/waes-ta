package ar.ppedemon.wta.service;

import ar.ppedemon.wta.data.ComparisonDao;
import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Chirp service implementation backed by a {@link ar.ppedemon.wta.data.ComparisonDao}.
 */
public class DaoComparisonService implements ComparisonService {

    private final ComparisonDao comparisonDao;
    private final Comparator comparator;
    private final Vertx vertx;

    @Inject
    public DaoComparisonService(ComparisonDao comparisonDao, Comparator comparator, Vertx vertx) {
        this.comparisonDao = comparisonDao;
        this.comparator = comparator;
        this.vertx = vertx;
    }

    @Override
    public Single<Boolean> upsertLeft(String userId, String cmpId, String data) {
        return comparisonDao.upsertSide(userId, cmpId, ComparisonDao.Side.LEFT, data);
    }

    @Override
    public Single<Boolean> upsertRight(String userId, String cmpId, String data) {
        return comparisonDao.upsertSide(userId, cmpId, ComparisonDao.Side.RIGHT, data);
    }

    @Override
    public Maybe<Comparison> get(String userId, String cmpId) {
        return comparisonDao.get(userId, cmpId);
    }

    @Override
    public Maybe<ResultWrapper<ComparisonResult>> compare(String userId, String cmpId) {

        /*
         * The following logic can be summarized as follows:
         *
         *  1. Look for intended comparison. If none available, return an empty Maybe.
         *
         *  2. If available, check if there's a cached result. Since the comparison
         *     invariant ensures cached results aren't stale, it's safe to return it.
         *
         *  3. No result cached, so check if comparison is complete. If not, return
         *     a Maybe with a failed result.
         *
         *  4. No result, and comparison is ok. We then execute a pipeline calling the
         *     comparator, and updating the result if not stale (which might happen if
         *     the comparison sides are modified while executing the comparator). This
         *     ensures the comparison invariant is preserved. Finally, return a Maybe
         *     with the comparison result.
         */

        return comparisonDao.get(userId, cmpId)
                .flatMapSingleElement(comparison -> {
                    if (Objects.nonNull(comparison.getResult())) {
                        return Single.just(ResultWrapper.ok(comparison.getResult()));
                    } else {
                        if (!comparison.valid()) {
                            return Single.just(ResultWrapper.error("Incomplete comparison"));
                        }
                        return compare(comparison).flatMap(result ->
                                comparisonDao.updateResult(comparison, result)
                                        .flatMap(__ -> Single.just(ResultWrapper.ok(result)))
                        );
                    }
                });
    }

    /**
     * Comparison will take a significant time for large data. So we execute them in a worker pool.
     *
     * @param comparison  comparison to process
     * @return  Computation delivering comparison result when completed
     */
    private Single<ComparisonResult> compare(Comparison comparison) {
        return vertx.<ComparisonResult>rxExecuteBlocking(future ->
            future.complete(comparator.compare(comparison))
        ).flatMapSingle(Single::just);
    }

    @Override
    public Single<Boolean> delete(String userId, String cmpId) {
        return comparisonDao.delete(userId, cmpId);
    }
}
