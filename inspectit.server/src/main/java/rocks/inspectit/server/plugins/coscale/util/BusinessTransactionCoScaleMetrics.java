package rocks.inspectit.server.plugins.coscale.util;

/**
 * Metrics wrapper for business transaction metrics (response time and baseline threshold).
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionCoScaleMetrics {
	/**
	 * Id of the response time metric.
	 */
	private long responseTimeMetricId;

	/**
	 * Id of the baseline threshold metric.
	 */
	private long rtThresholdMetricId;

	/**
	 * Gets {@link #responseTimeMetricId}.
	 *
	 * @return {@link #responseTimeMetricId}
	 */
	public long getResponseTimeMetricId() {
		return this.responseTimeMetricId;
	}

	/**
	 * Sets {@link #responseTimeMetricId}.
	 *
	 * @param responseTimeMetricId
	 *            New value for {@link #responseTimeMetricId}
	 */
	public void setResponseTimeMetricId(long responseTimeMetricId) {
		this.responseTimeMetricId = responseTimeMetricId;
	}

	/**
	 * Gets {@link #rtThresholdMetricId}.
	 *
	 * @return {@link #rtThresholdMetricId}
	 */
	public long getRtThresholdMetricId() {
		return this.rtThresholdMetricId;
	}

	/**
	 * Sets {@link #rtThresholdMetricId}.
	 *
	 * @param rtThresholdMetricId
	 *            New value for {@link #rtThresholdMetricId}
	 */
	public void setRtThresholdMetricId(long rtThresholdMetricId) {
		this.rtThresholdMetricId = rtThresholdMetricId;
	}

}
