package ar.ppedemon.wta.service;

import ar.ppedemon.wta.data.ComparisonDao;
import ar.ppedemon.wta.model.ComparisonResult;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Chirp service implementation backed by a {@link ar.ppedemon.wta.data.ComparisonDao}.
 */
public class DaoComparisonService implements ComparisonService {

    private final ComparisonDao comparisonDao;
    private final Comparator comparator;

    @Inject
    public DaoComparisonService(ComparisonDao comparisonDao, Comparator comparator) {
        this.comparisonDao = comparisonDao;
        this.comparator = comparator;
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
    public Maybe<ResultWrapper<ComparisonResult>> compare(String userId, String cmpId) {
        return comparisonDao.get(userId, cmpId)
                .flatMapSingleElement(comparison -> {
                    if (Objects.nonNull(comparison.getResult())) {
                        return Single.just(ResultWrapper.ok(comparison.getResult()));
                    } else {
                        if (!comparison.valid()) {
                            return Single.just(ResultWrapper.error("Incomplete comparison"));
                        }
                        ComparisonResult result = comparator.compare(comparison);
                        return comparisonDao.updateResult(comparison, result).map(__ -> ResultWrapper.ok(result));
                    }
                });
    }

    @Override
    public Single<Boolean> delete(String userId, String cmpId) {
        return comparisonDao.delete(userId, cmpId);
    }
}
