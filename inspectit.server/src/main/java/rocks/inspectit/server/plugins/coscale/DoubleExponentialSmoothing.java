package rocks.inspectit.server.plugins.coscale;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * This class realizes a baselining approach based on double exponential smoothing for one time
 * series.
 *
 * @author Marius Oehler
 *
 */
public class DoubleExponentialSmoothing {
	/**
	 * The time constant.
	 */
	private final double smoothingFactor;

	/**
	 * The trend smoothing factor.
	 */
	private final double trendSmoothingFactor;

	/**
	 * The current value.
	 */
	private double currentValue;

	/**
	 * The current trend value.
	 */
	private double currentTrend;

	/**
	 * The count of pushes.
	 */
	private long pushCount = 0;

	/**
	 * Current calculated value of the baseline threshold.
	 */
	private Double currentBaselineThreshold = Double.NaN;

	/**
	 * The number of raw values to keep.
	 */
	private final int numRawValuesToKeep;

	/**
	 * Queue of raw values.
	 */
	private final LinkedBlockingQueue<Double> rawValues;

	/**
	 * Minimum value.
	 */
	private double minValue = Double.MAX_VALUE;

	/**
	 * Constructor.
	 *
	 * @param smoothingFactor
	 *            the smoothing factor
	 * @param trendSmoothingFactor
	 *            the trend smoothing factor
	 * @param numRawValuesToKeep
	 *            The number of raw values to keep.
	 */
	public DoubleExponentialSmoothing(double smoothingFactor, double trendSmoothingFactor, int numRawValuesToKeep) {
		this.smoothingFactor = smoothingFactor;
		this.trendSmoothingFactor = trendSmoothingFactor;
		this.numRawValuesToKeep = numRawValuesToKeep;
		if (numRawValuesToKeep > 2) {
			rawValues = new LinkedBlockingQueue<Double>(numRawValuesToKeep);
		} else {
			throw new IllegalArgumentException("DoubleExponentialSmoothing must keep at least 3 raw values!");
		}
	}

	/**
	 * Adds a new value to the baseline calculation.
	 *
	 * @param value
	 *            value to add
	 */
	public synchronized void push(double value) {
		if (numRawValuesToKeep > 0) {
			while (rawValues.size() >= numRawValuesToKeep) {
				rawValues.poll();
			}
			rawValues.offer(value);
		}

		if (pushCount <= 0L) {
			currentValue = value;
		} else if (pushCount == 1L) {
			currentTrend = value - currentValue;
			currentValue = value;
		} else {
			double nextValue = (smoothingFactor * value) + ((1 - smoothingFactor) * (currentValue + currentTrend));
			currentTrend = (trendSmoothingFactor * (nextValue - currentValue)) + ((1 - trendSmoothingFactor) * currentTrend);
			currentValue = nextValue;
		}

		pushCount++;
		currentBaselineThreshold = Double.NaN;
		minValue = value < minValue ? value : minValue;
	}

	/**
	 * Returns current baseline value.
	 *
	 * @return Returns current baseline value.
	 */
	public double getValue() {
		return Math.max(minValue, currentValue);
	}

	/**
	 * Gets {@link #rawValues}.
	 *
	 * @return {@link #rawValues}
	 */
	public LinkedBlockingQueue<Double> getRawValues() {
		return rawValues;
	}

	/**
	 * Returns the current baseline threshold. Re-calculates it if needed.
	 *
	 * @return Returns the current baseline threshold.
	 */
	public double getBaselineThreshold() {
		if (currentBaselineThreshold.isNaN()) {
			if (getRawValues().size() > 2) {
				StandardDeviation sd = new StandardDeviation();
				double standardDev = sd.evaluate(toDoubleArray(getRawValues()));
				double mean = getValue();
				currentBaselineThreshold = mean + (1 * standardDev);
			} else {
				currentBaselineThreshold = Double.MAX_VALUE;
			}
		}
		return currentBaselineThreshold;
	}

	/**
	 * Transform a double collection to a double array.
	 *
	 * @param collection
	 *            collection to transform
	 * @return double array
	 */
	private static double[] toDoubleArray(Collection<Double> collection) {
		double[] returnArray = new double[collection.size()];
		int index = 0;
		Iterator<Double> iterator = collection.iterator();
		while (iterator.hasNext()) {
			returnArray[index++] = iterator.next();
		}
		return returnArray;
	}
}
