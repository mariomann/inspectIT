package rocks.inspectit.server.plugins.coscale;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 *
 *
 * @author Alexander Wert
 *
 */
public class CoScaleProcessor extends AbstractCmrDataProcessor {

	@Autowired
	private CoScalePlugin coScalePlugin;

	@Autowired
	private CachedDataService cachedDataService;

	@Autowired
	private CoScaleBusinessTransactionHandler coScaleBusinessTransactionhandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
		BusinessTransactionData businessTransaction = cachedDataService.getBusinessTransactionForId(invocation.getApplicationId(), invocation.getBusinessTransactionId());
		coScaleBusinessTransactionhandler.processData(invocation.getBusinessTransactionId(), businessTransaction.getName(), invocation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return coScalePlugin.isActive() && coScalePlugin.isConnected() && (defaultData instanceof InvocationSequenceData);
	}
}
