package ar.ppedemon.wta.data;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Interface defining contract for comparison data objects.
 *
 * Implementations *must* ensure that the methods {@code upsertSide} and {@code updateResult}
 * preserve the comparison invariant: for any comparison, result is unset or corresponds to
 * the current lhs and rhs.
 *
 * That is to say, we avoid storing stale results in comparisons. This might happen if a
 * comparison is triggered and any side is updated in the meantime. When the comparison
 * finishes, we can't save it for it would be stale, breaking the invariant.
 *
 * How to ensure this is up to the implementation. A typical version-based optimistic
 * write control should be enough: side upserts increment version, comparison is only
 * saved if version hasn't changed.
 *
 * @author ppedemon
 */
public interface ComparisonDao {

    enum Side { LEFT, RIGHT }

    /**
     * Upsert the given side of a comparison. That is, create it if there's
     * no comparison for the given userId and comparison Id, or update it
     * otherwise. This operation is idempotent.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @param side    comparison side
     * @param data    comparison data for given side
     *
     * @return computation producing boolean set to true iif comparison side was created
     */
    Single<Boolean> upsertSide(String userId, String cmpId, Side side, String data);

    /**
     * Get a comparison with the given user Id and comparison Id.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @return  computation delivering the intended comparison, or empty if not found.
     */
    Maybe<Comparison> get(String userId, String cmpId);

    /**
     * Update the result for the given comparison. We must avoid stale updates here.
     *
     * @param comparison  comparison to update
     * @param result  comparison result
     * @return  computation signaling whether the update took place or not
     *   (because side were modified while computing the result)
     */
    Single<Boolean> updateResult(Comparison comparison, ComparisonResult result);

    /**
     * Delete a comparison with the given user Id and comparison Id.
     *
     * @param userId  user Id
     * @param cmpId   comparison Id
     * @return   computation signaling whether the deletion took place or not
     *   (in case the intended comparison was not found)
     */
    Single<Boolean> delete(String userId, String cmpId);
}
