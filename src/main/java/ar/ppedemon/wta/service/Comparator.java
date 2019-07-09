package ar.ppedemon.wta.service;

import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;

/**
 * Interface defining comparators. Comparators produce a {@link ComparisonResult} for a given
 * {@link Comparison} instance.
 *
 * @author ppedemon
 */
public interface Comparator {

    /**
     * Compare both sides of the given comparison.
     *
     * Precondition: both sides of the comparison must be set.
     *
     * @param comparison {@link Comparison} instance providing data to compare
     * @return {@link ComparisonResult}
     */
    ComparisonResult compare(Comparison comparison);
}
