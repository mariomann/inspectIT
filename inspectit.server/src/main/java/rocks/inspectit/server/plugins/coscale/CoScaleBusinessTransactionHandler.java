package rocks.inspectit.server.plugins.coscale;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.coscale.sdk.client.commons.Options.Builder;
import com.coscale.sdk.client.data.BinaryData;
import com.coscale.sdk.client.data.DataInsert;
import com.coscale.sdk.client.data.DataInsertBuilder;
import com.coscale.sdk.client.events.Event;
import com.coscale.sdk.client.events.EventData;
import com.coscale.sdk.client.events.EventDataInsert;
import com.coscale.sdk.client.events.EventInsert;
import com.coscale.sdk.client.metrics.DataType;
import com.coscale.sdk.client.metrics.Metric;
import com.coscale.sdk.client.metrics.MetricGroup;
import com.coscale.sdk.client.metrics.MetricGroupInsert;
import com.coscale.sdk.client.metrics.MetricInsert;
import com.coscale.sdk.client.metrics.SubjectType;

import rocks.inspectit.server.dao.InvocationDataDao;
import rocks.inspectit.server.plugins.IPluginStateListener;
import rocks.inspectit.server.plugins.coscale.util.BusinessTransactionCoScaleMetrics;
import rocks.inspectit.server.storage.CmrStorageManager;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageFileType;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataSaverProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationClonerDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationExtractorDataProcessor;

@Component
public class CoScaleBusinessTransactionHandler implements Runnable, IPluginStateListener {
	private static final String EVENT_NAME = "inspectIT data";

	private static final String EVENT_ICON_NAME = "gears";

	private static final String EVENT_TYPE = "inspectIT";

	private static final String EVENT_ATTRIBUTE_DESCRIPTION = "[{\"name\":\"filename\",\"type\":\"string\"}]";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd_HH-mm");

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	@Autowired
	CoScalePlugin plugin;

	/**
	 * Storage service used for storage creation when sending data to CoScale.
	 */
	@Autowired
	IStorageService storageService;

	/**
	 * Storage manager used for storage creation when sending data to CoScale.
	 */
	@Autowired
	CmrStorageManager storageManager;

	/**
	 * Smoothing factor for baseline calculation.
	 */
	@Value(value = "${cmr.data.extensions.coscale.anomaly.smoothingFactor}")
	private double smoothingFactor;

	/**
	 * Smoothing factor for the trend component of the baseline.
	 */
	@Value(value = "${cmr.data.extensions.coscale.anomaly.trendSmoothingFactor}")
	private double trendSmoothingFactor;

	/**
	 * Number of the slowest invocation sequences to send in case of an anomaly.
	 */
	@Value(value = "${cmr.data.extensions.coscale.numInvocationsToSend}")
	private int numInvocationsToSend;

	/**
	 * Time interval for aggregation in minutes.
	 */
	@Value(value = "${cmr.data.extensions.coscale.anomaly.interval}")
	private int timeInterval = 1;

	/**
	 * Write response times to CoScales.
	 */
	@Value(value = "${cmr.data.extensions.coscale.writeTimings}")
	private boolean writeTimingsToCoScale;

	/**
	 * Factor to multiply the standard deviation to get the BaseLineThreshold.
	 */
	@Value(value = "${cmr.data.extensions.coscale.anomaly.standardDevFactor}")
	private int standardDevFactor = 3;

	@Value(value = "${cmr.data.extensions.coscale.anomaly.minAnomalyDuration}")
	private int minAnomalyDuration;

	private ScheduledExecutorService executorService;

	private final Map<Integer, CoScaleBusinessTransactionData> businessTxData = new ConcurrentHashMap<>();

	public void processData(int businessTxId, String businessTxName, InvocationSequenceData invocation) {
		CoScaleBusinessTransactionData btData = getBusinessTransactionData(businessTxId, businessTxName);
		double duration = InvocationSequenceDataHelper.calculateDuration(invocation);
		btData.incrementCurrentResponseTimeSumBy(duration);
		btData.incrementCurrentResponseTimeCount();

		if (duration > btData.getExponentialSmooting().getBaselineThreshold()) {
			btData.getSlowestInvocationSequences().add(invocation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (CoScaleBusinessTransactionData btData : businessTxData.values()) {
			if (btData.getCurrentResponseTimeCount() > 0) {
				double meanResponseTime;
				synchronized (this) {
					meanResponseTime = btData.getCurrentResponseTimeSum() / btData.getCurrentResponseTimeCount();
					btData.setCurrentResponseTimeCount(0);
					btData.setCurrentResponseTimeSum(0.0);
				}
				if (btData.getInitializationCounter() <= 0) {
					if (meanResponseTime > btData.getExponentialSmooting().getBaselineThreshold()) {
						btData.getAnomaly().incrementAndGet();
					} else {
						if (btData.getAnomaly().get() >= minAnomalyDuration) {
							sendAsyncInvocationSequencesAsStorage(btData.getBusinessTxName(), btData.getSlowestInvocationSequences(), btData.getAnomaly().get() * timeInterval);
						}
						btData.getAnomaly().set(0);
						btData.getSlowestInvocationSequences().clear();
					}
				}

				if (writeTimingsToCoScale && (null != btData.getBtMetrics())) {
					DataInsertBuilder builder = new DataInsertBuilder();
					builder.addDoubleData(btData.getBtMetrics().getResponseTimeMetricId(), 0, meanResponseTime);
					if (btData.getInitializationCounter() <= 0) {
						builder.addDoubleData(btData.getBtMetrics().getRtThresholdMetricId(), 0, btData.getExponentialSmooting().getBaselineThreshold());
					}

					writeMetricData(builder.build());
				}

				if (btData.getInitializationCounter() > 0) {
					btData.decrementInitializationCounter();
					btData.getSlowestInvocationSequences().clear();
				}

				btData.getExponentialSmooting().push(meanResponseTime);
			} else {
				synchronized (this) {
					btData.setCurrentResponseTimeCount(0);
					btData.setCurrentResponseTimeSum(0.0);
					btData.getSlowestInvocationSequences().clear();
				}
			}
		}
	}

	private CoScaleBusinessTransactionData getBusinessTransactionData(int businessTxId, String businessTxName) {
		if (!businessTxData.containsKey(businessTxId)) {
			BusinessTransactionCoScaleMetrics metrics = null;
			if (writeTimingsToCoScale) {
				metrics = getBusinessTransactionMetrics(businessTxName);
			}
			CoScaleBusinessTransactionData data = new CoScaleBusinessTransactionData(businessTxName, metrics, smoothingFactor, trendSmoothingFactor, 100, numInvocationsToSend, standardDevFactor);
			businessTxData.put(businessTxId, data);
		}
		return businessTxData.get(businessTxId);
	}

	@PropertyUpdate(properties = { "cmr.data.extensions.coscale.writeTimings" })
	public void updateMetrics() {
		if (writeTimingsToCoScale) {
			for (CoScaleBusinessTransactionData btData : businessTxData.values()) {
				BusinessTransactionCoScaleMetrics metrics = getBusinessTransactionMetrics(btData.getBusinessTxName());
				btData.setBtMetrics(metrics);
			}
		}
	}

	/**
	 * Send invocation sequences as storage file to CoScale.
	 *
	 * @param businessTxName
	 *            name of the business transaction.
	 * @param invocations
	 *            invocation sequences to send.
	 * @param durationSeconds
	 *            the duration of the anomaly in seconds
	 */
	public void sendAsyncInvocationSequencesAsStorage(final String businessTxName, final List<InvocationSequenceData> invocations, final int durationSeconds) {
		if (invocations.isEmpty()) {
			return;
		}
		final String storageName = businessTxName + "_" + DATE_FORMAT.format(new Date());
		final String fileName = storageName + StorageFileType.ZIP_STORAGE_FILE.getExtension();
		final List<InvocationSequenceData> invocationSequencesCopied = new ArrayList<>(invocations.size());
		for (InvocationSequenceData isData : invocations) {
			invocationSequencesCopied.add(isData);
		}
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				if (plugin.isActive()) {
					if (!plugin.isConnected()) {
						throw new IllegalStateException("CoScale cplugin is not connected! Check Application Id and Access token in the CoScale settings!");
					}
					StorageData storageData = null;
					try {
						storageData = createStorage(storageName, invocationSequencesCopied);
						BinaryData binData = wrapInBinaryData(fileName, storageData);

						Event event = retreiveEventCategory();

						EventDataInsert eventDataInsert = new EventDataInsert("Detailed inspectIT data available!", -1L * durationSeconds, 0L, null, "a");
						EventData eventData = plugin.getEventsApi().insertData(event.id, eventDataInsert);
						// Sleep to ensure that eventData has been pesisted at CoScale side before
						// attaching binary data to it.
						Thread.sleep(1000);
						plugin.getEventsApi().uploadBinary(event.id, eventData.id, binData);
					} catch (Exception e) {
						log.error("Failed sending storage to CoScale!", e);
					} finally {
						if (null != storageData) {
							try {
								storageManager.deleteStorage(storageData);
							} catch (IOException | BusinessException e) {
								log.error("Failed deleting storage!", e);
							}
						}
					}
				}
			}

		});
	}

	/**
	 * @param fileName
	 * @param storageData
	 * @return
	 * @throws IOException
	 * @throws SerializationException
	 */
	private BinaryData wrapInBinaryData(final String fileName, StorageData storageData) throws IOException, SerializationException {
		ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
		storageManager.zipStorageData(storageData, byteArrayOutStream);
		BinaryData binData = new BinaryData(byteArrayOutStream.toByteArray(), fileName);
		byteArrayOutStream.close();
		return binData;
	}

	/**
	 * @param queryBuilder
	 * @return
	 * @throws IOException
	 */
	private Event retreiveEventCategory() throws IOException {
		Builder queryBuilder = new Builder();
		queryBuilder.selectBy("name", EVENT_NAME);
		Event event;
		List<Event> events = plugin.getEventsApi().all(queryBuilder.build());
		if (!events.isEmpty()) {
			event = events.get(0);
		} else {
			EventInsert eventInsert = new EventInsert(EVENT_NAME, "Detailed invocation data from inspectIT", null, EVENT_TYPE, EVENT_ICON_NAME);
			eventInsert.attributeDescriptions = EVENT_ATTRIBUTE_DESCRIPTION;
			event = plugin.getEventsApi().insert(eventInsert);
		}
		return event;
	}

	/**
	 * Retrieves metrics for the given business transaction name.
	 *
	 * @param businessTxName
	 *            name of the business transaction to retrieve the metrics for
	 * @return {@link BusinessTransactionCoScaleMetrics} instance wrapping the metrics
	 */
	public BusinessTransactionCoScaleMetrics getBusinessTransactionMetrics(String businessTxName) {
		if (plugin.isActive() && plugin.isConnected()) {
			try {
				Builder queryBuilder = new Builder();
				queryBuilder.selectBy("name", "inspectIT");

				List<MetricGroup> metricGroups = plugin.getMetricsApi().getAllMetricGroups(queryBuilder.build());
				MetricGroup group;
				if (metricGroups.isEmpty()) {
					MetricGroupInsert groupInsert = new MetricGroupInsert("inspectIT", "inspectIT metrics", "inspectIT timings", SubjectType.APPLICATION);
					group = plugin.getMetricsApi().insertMetricGroup(groupInsert);
				} else {
					group = metricGroups.get(0);
				}
				BusinessTransactionCoScaleMetrics metrics = new BusinessTransactionCoScaleMetrics();

				long responseTimeMetricId = createMetric("RT - " + businessTxName, "Response Time of business transaction " + businessTxName, group.id);
				long thresholdMetricId = createMetric("T - " + businessTxName, "Threshold for business transaction " + businessTxName, group.id);
				metrics.setResponseTimeMetricId(responseTimeMetricId);
				metrics.setRtThresholdMetricId(thresholdMetricId);
				return metrics;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	/**
	 * Writes metric data to CoScale.
	 *
	 * @param dataInsert
	 *            data to write
	 */
	public void writeMetricData(DataInsert dataInsert) {
		if (plugin.isActive() && plugin.isConnected()) {
			try {
				plugin.getDataApi().insert("a", dataInsert);
			} catch (IOException e) {
				log.warn("Cannot write data for metric.", e);
			}
		} else {
			log.warn("Cannot write data for metric. CoScale plugin is not connected!");
		}
	}

	/**
	 * Creates a CoScale metric with the given name if it does not exist yet. If the metric already
	 * exists, this method returns the id of the metric.
	 *
	 * @param metricName
	 *            Name of the metric
	 * @param description
	 *            description for the metric
	 * @param groupId
	 *            metric group id
	 * @return The id of the created metric.
	 * @throws IOException
	 *             throws this exception if communication with CoScale fails.
	 */
	private long createMetric(String metricName, String description, long groupId) throws IOException {
		Builder queryBuilder = new Builder();
		queryBuilder.selectBy("name", metricName);

		List<Metric> metrics = plugin.getMetricsApi().all(queryBuilder.build());
		Metric metric;
		if (metrics.isEmpty()) {
			MetricInsert metricInsert = new MetricInsert(metricName, description, DataType.DOUBLE, SubjectType.APPLICATION, "ms", timeInterval * 60);
			metric = plugin.getMetricsApi().insert(metricInsert);
			plugin.getMetricsApi().addMetricToGroup(metric.id, groupId);
		} else {
			metric = metrics.get(0);
		}
		return metric.id;
	}

	/**
	 * Creates a storage for the given set of invocation sequences.
	 *
	 * @param storageName
	 *            Name of the storage.
	 * @param invocations
	 *            collection of invocation sequences
	 * @return The created Storage Object.
	 * @throws BusinessException
	 *             thrown if Storage cannot be created.
	 */
	private StorageData createStorage(String storageName, Collection<InvocationSequenceData> invocations) throws BusinessException {
		StorageData storageData = new StorageData();
		storageData.setName(storageName);

		// create processors for storage creation
		List<Class<? extends DefaultData>> classes = new ArrayList<Class<? extends DefaultData>>(Collections.singleton(InvocationSequenceData.class));
		DataSaverProcessor saverProcessor = new DataSaverProcessor(classes, true);
		InvocationExtractorDataProcessor invocExtractorDataProcessor = new InvocationExtractorDataProcessor(Collections.singletonList((AbstractDataProcessor) saverProcessor));
		List<AbstractDataProcessor> processors = new ArrayList<>();
		processors.add(saverProcessor);
		processors.add(invocExtractorDataProcessor);
		processors.add(new InvocationClonerDataProcessor());

		// create storage
		storageData = storageService.createAndOpenStorage(storageData);
		storageService.writeToStorage(storageData, invocations, processors, true);
		storageService.closeStorage(storageData);

		return storageData;
	}

	@PostConstruct
	public void init() {
		if (!plugin.getPluginStateListeners().contains(this)) {
			plugin.addPluginStateListener(this);
		}
		if (plugin.isActive()) {
			pluginActivated();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pluginActivated() {
		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(this, timeInterval, timeInterval, TimeUnit.MINUTES);
		sendTest();
	}

	@Autowired
	private InvocationDataDao invocationDataDao;
	private void sendTest(){
		List<InvocationSequenceData> resultWithChildren = invocationDataDao.getInvocationSequenceDetail(0, 0, -1, null, null, null);
		if (!resultWithChildren.isEmpty()) {
			sendAsyncInvocationSequencesAsStorage("TestBT", resultWithChildren, 2);
		}


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pluginDeactivated() {
		if (null != executorService) {
			executorService.shutdownNow();
		}
	}
}
