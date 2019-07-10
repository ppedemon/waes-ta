package ar.ppedemon.wta.service;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Interface defining contract for comparison service implementations.
 */
public interface ComparisonService {
    /**
     * Upsert the left hand side of a given comparison. This will create a new comparison,
     * or update it if already existing. This method is idempotent.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @param data    data for left hand side
     * @return  computation producing boolean set to true iif comparison side was created
     */
    Single<Boolean> upsertLeft(String userId, String cmpId, String data);

    /**
     * Upsert the right hand side of a given comparison. This will create a new comparison,
     * or update it if already existing. This method is idempotent.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @param data    data for right hand side
     * @return  computation producing boolean set to true iif comparison side was created
     */
    Single<Boolean> upsertRight(String userId, String cmpId, String data);

    /**
     * Get a comparison for the given user Id and comparison Id.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @return  computation delivering the intended comparison, or empty if comparison not found
     */
    Maybe<Comparison> get(String userId, String cmpId);

    /**
     * Get comparison results for the comparison for the given user Id and comparison Id.
     *
     * @param userId  user Id
     * @param cmpId  comparison Id
     * @return  computation delivering the wrapped comparison result, or empty if the comparison can't be found.
     *   If the comparison has either side not defined, the wrapper will be unsuccessful.
     */
    Maybe<ResultWrapper<ComparisonResult>> compare(String userId, String cmpId);

    /**
     * Delete a comparison given a user Id and a comparison Id.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @return  computation signaling whether the deletion took place or not
     *   (in case the intended comparison was not found)
     */
    Single<Boolean> delete(String userId, String cmpId);
}
