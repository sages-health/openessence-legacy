package edu.jhuapl.graphs;

import java.util.List;

/**
 * A specialized {@link DataSeriesInterface} whose points have a date based {@link TimePointInterface#getDescriminator()
 * descriminator}. TimeSeriesInterfaces can be put into a time series chart.
 *
 * @see DataSeriesInterface
 * @see TimePointInterface
 */
public interface TimeSeriesInterface extends DataSeriesInterface {

    /**
     * Gets a series of {@link TimePointInterface time-based} points. {@inheritDoc}
     */
    @Override
    public List<? extends TimePointInterface> getPoints();
}
