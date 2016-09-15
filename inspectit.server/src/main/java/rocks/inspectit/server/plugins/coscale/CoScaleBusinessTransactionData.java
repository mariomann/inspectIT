package rocks.inspectit.server.plugins.coscale;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import rocks.inspectit.server.plugins.coscale.util.BusinessTransactionCoScaleMetrics;
import rocks.inspectit.server.plugins.coscale.util.LimitedSortedList;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * @author Alexander Wert
 *
 */
public class CoScaleBusinessTransactionData {
	/**
	 * Name of the business transaction.
	 */
	private final String businessTxName;

	/**
	 * List of slowest requests to be gather.
	 */
	private final LimitedSortedList<InvocationSequenceData> slowestInvocationSequences;

	/**
	 * Counter for anomaly duration (in time time intervals specified in the config.)
	 */
	private final AtomicInteger anomaly = new AtomicInteger(0);

	private final DoubleExponentialSmoothing exponentialSmooting;

	private double currentResponseTimeSum = 0.0;
	private int currentResponseTimeCount = 0;
	private BusinessTransactionCoScaleMetrics btMetrics;
	private int initializationCounter = 5;

	public CoScaleBusinessTransactionData(String businessTxName, BusinessTransactionCoScaleMetrics btMetrics, double smoothingFactor, double trendSmoothingFactor, int numRawValuesToKeep,
			int numSlowestInvocationsToKeep, int standardDevFactor) {
		this.businessTxName = businessTxName;
		slowestInvocationSequences = new LimitedSortedList<>(numSlowestInvocationsToKeep, new Comparator<InvocationSequenceData>() {

			@Override
			public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
				double diff = InvocationSequenceDataHelper.calculateDuration(o1) - InvocationSequenceDataHelper.calculateDuration(o2);
				if (diff > 0) {
					return 1;
				} else if (diff < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		this.exponentialSmooting = new DoubleExponentialSmoothing(smoothingFactor, trendSmoothingFactor, numRawValuesToKeep, standardDevFactor);
		this.btMetrics = btMetrics;
	}

	public void incrementCurrentResponseTimeCount() {
		this.currentResponseTimeCount++;
	}

	public void incrementCurrentResponseTimeSumBy(double duration) {
		this.currentResponseTimeSum += duration;
	}

	public void decrementInitializationCounter() {
		initializationCounter--;
	}

	// ****************************************************************
	// ****************************************************************
	// *** GETTERS and SETTERS
	// ****************************************************************
	// ****************************************************************

	/**
	 * Gets {@link #currentResponseTimeSum}.
	 *
	 * @return {@link #currentResponseTimeSum}
	 */
	public double getCurrentResponseTimeSum() {
		return this.currentResponseTimeSum;
	}

	/**
	 * Sets {@link #currentResponseTimeSum}.
	 *
	 * @param currentResponseTimeSum
	 *            New value for {@link #currentResponseTimeSum}
	 */
	public void setCurrentResponseTimeSum(double currentResponseTimeSum) {
		this.currentResponseTimeSum = currentResponseTimeSum;
	}

	/**
	 * Gets {@link #currentResponseTimeCount}.
	 *
	 * @return {@link #currentResponseTimeCount}
	 */
	public int getCurrentResponseTimeCount() {
		return this.currentResponseTimeCount;
	}

	/**
	 * Sets {@link #currentResponseTimeCount}.
	 *
	 * @param currentResponseTimeCount
	 *            New value for {@link #currentResponseTimeCount}
	 */
	public void setCurrentResponseTimeCount(int currentResponseTimeCount) {
		this.currentResponseTimeCount = currentResponseTimeCount;
	}


	/**
	 * Gets {@link #btMetrics}.
	 *
	 * @return {@link #btMetrics}
	 */
	public BusinessTransactionCoScaleMetrics getBtMetrics() {
		return this.btMetrics;
	}

	/**
	 * Sets {@link #btMetrics}.
	 *
	 * @param btMetrics
	 *            New value for {@link #btMetrics}
	 */
	public void setBtMetrics(BusinessTransactionCoScaleMetrics btMetrics) {
		this.btMetrics = btMetrics;
	}

	/**
	 * Gets {@link #initializationCounter}.
	 *
	 * @return {@link #initializationCounter}
	 */
	public int getInitializationCounter() {
		return this.initializationCounter;
	}

	/**
	 * Sets {@link #initializationCounter}.
	 *
	 * @param initializationCounter
	 *            New value for {@link #initializationCounter}
	 */
	public void setInitializationCounter(int initializationCounter) {
		this.initializationCounter = initializationCounter;
	}

	/**
	 * Gets {@link #businessTxName}.
	 *
	 * @return {@link #businessTxName}
	 */
	public String getBusinessTxName() {
		return this.businessTxName;
	}

	/**
	 * Gets {@link #slowestInvocationSequences}.
	 *
	 * @return {@link #slowestInvocationSequences}
	 */
	public LimitedSortedList<InvocationSequenceData> getSlowestInvocationSequences() {
		return this.slowestInvocationSequences;
	}

	/**
	 * Gets {@link #anomaly}.
	 *
	 * @return {@link #anomaly}
	 */
	public AtomicInteger getAnomaly() {
		return this.anomaly;
	}

	/**
	 * Gets {@link #exponentialSmooting}.
	 *
	 * @return {@link #exponentialSmooting}
	 */
	public DoubleExponentialSmoothing getExponentialSmooting() {
		return this.exponentialSmooting;
	}

}
