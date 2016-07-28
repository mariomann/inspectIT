package rocks.inspectit.server.plugins.coscale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coscale.sdk.client.applications.Application;
import com.coscale.sdk.client.requests.Request;
import com.coscale.sdk.client.requests.RequestClassifierType;

import rocks.inspectit.server.plugins.IPluginStateListener;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.business.expression.IContainerExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.AndExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.ci.business.valuesource.PatternMatchingType;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HostValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpRequestMethodValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUriValueSource;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

/**
 * This Component is responsible for mapping CoScale request definitions to inspectIT's business
 * context definition.
 * 
 * @author Alexander Wert
 *
 */
@Component
public class CoScaleBusinessRequestMapper implements IPluginStateListener {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Configuration interface service to retrieve and map business transaction definitions.
	 */
	@Autowired
	IConfigurationInterfaceService ciService;

	/**
	 * The CoScale plug-in.
	 */
	@Autowired
	CoScalePlugin plugin;

	/**
	 * Maps CoScale's Request Definitions to inspectIT's {@link BusinessTransactionDefinition}s and
	 * registers the {@link BusinessTransactionDefinition}s.
	 */
	public void mapCoScaleRequestsToBusinessTransactionDefinitions() {
		try {
			ApplicationDefinition applicationDefinition = retrieveApplicationDefinition();

			List<Request> requests = retrieveCoScaleRequestDefinitions();
			Map<Long, Request> fullRequestMap = toMap(requests);
			List<Request> leafRequests = filterLeafRequests(requests);
			Collections.sort(leafRequests, new Comparator<Request>() {

				@Override
				public int compare(Request o1, Request o2) {
					return o1.priority.compareTo(o2.priority);
				}
			});

			boolean btDefinitionsChanged = updateBusinessTransactionDefinitions(applicationDefinition, fullRequestMap, leafRequests);

			if (btDefinitionsChanged) {
				ciService.updateApplicationDefinition(applicationDefinition);
			}
		} catch (Exception e) {
			log.error("Failed mapping CoScale requests to inspectIT business transactions!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pluginActivated() {
		mapCoScaleRequestsToBusinessTransactionDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pluginDeactivated() {
		// nothing to do here
	}

	/**
	 * Maps CoScale's Match-Expression to inspectIT's {@link PatternMatchingType}.
	 *
	 * @param value
	 *            Value to map.
	 * @return Returns mapped {@link PatternMatchingType} instance.
	 */
	private PatternMatchingType extractMatchExpression(String value) {
		PatternMatchingType matchingExpressing;
		switch (value.substring(0, 1)) {
		case "S":
			matchingExpressing = PatternMatchingType.ENDS_WITH;
			break;
		case "C":
			matchingExpressing = PatternMatchingType.CONTAINS;
			break;
		case "P":
			matchingExpressing = PatternMatchingType.STARTS_WITH;
			break;
		case "E":
			matchingExpressing = PatternMatchingType.EQUALS;
			break;
		case "R":
			matchingExpressing = PatternMatchingType.REGEX;
			break;
		default:
			throw new IllegalArgumentException("No existing matching rule");
		}

		return matchingExpressing;
	}

	/**
	 * Maps CoScale's RequestClassifierType to inspectIT's StringValueSource.
	 *
	 * @param classifierType
	 *            {@link RequestClassifierType} to map.
	 * @return Returns mapped {@link StringValueSource} instance.
	 */
	private StringValueSource extractValueSource(RequestClassifierType classifierType) {
		switch (classifierType) {
		case HOST:
			return new HostValueSource();
		case METHOD:
			return new HttpRequestMethodValueSource();
		case PAGE:
		case URL:
			return new HttpUriValueSource();
		default:
			throw new IllegalArgumentException("No existing classifier type!");
		}
	}

	/**
	 * Checks whether business transaction definitions for all CoScale's request definitions exist.
	 * If not, this method creates corresponding business transaction definitions.
	 *
	 * @param applicationDefinition
	 *            {@link ApplicationDefinition} to assign the created business transaction
	 *            definitions to.
	 * @param fullRequestMap
	 *            Map from ids to CoScale requests for all CoScale requests.
	 * @param leafRequests
	 *            list of all CoScale leaf requests (in the hierarchy of requests).
	 * @return Returns true, if there was a change in inspectITs business transaction definitions
	 */
	private boolean updateBusinessTransactionDefinitions(ApplicationDefinition applicationDefinition, Map<Long, Request> fullRequestMap, List<Request> leafRequests) {
		boolean btDefinitionChanged = false;
		for (Request request : leafRequests) {
			if ((request.requests == null) || request.requests.isEmpty()) {
				BusinessTransactionDefinition businessTransaction = null;
				for (BusinessTransactionDefinition existingBTDef : applicationDefinition.getBusinessTransactionDefinitions()) {
					if (request.name.equals(existingBTDef.getBusinessTransactionDefinitionName())) {
						businessTransaction = existingBTDef;
						break;
					}
				}
				if (businessTransaction == null) {
					// add new BTDefinition
					businessTransaction = new BusinessTransactionDefinition(request.name);
					AndExpression andExpression = new AndExpression();
					andExpression.setAdvanced(true);
					addMatchingExpressionsForRequest(request, fullRequestMap, andExpression, null);
					businessTransaction.setMatchingRuleExpression(andExpression);
					try {
						applicationDefinition.addBusinessTransactionDefinition(businessTransaction);
					} catch (BusinessException e) {
						log.error("Failed mapping CoScale requests to inspectIT business transactions!", e);
					}
					btDefinitionChanged = true;
				}
			}
		}
		return btDefinitionChanged;
	}

	/**
	 * Retrieve the {@link ApplicationDefinition} instance for the CoScale application key. If no
	 * {@link ApplicationDefinition} instance exists, then this method creates a new one.
	 *
	 * @return Returns the {@link ApplicationDefinition} corresponding to the CoScale application.
	 * @throws IOException
	 *             Thrown if retrieving the CoScale application fails.
	 * @throws BusinessException
	 *             Thrown if adding new ApplicationDefinition fails.
	 */
	private ApplicationDefinition retrieveApplicationDefinition() throws IOException, BusinessException {
		ApplicationDefinition applicationDefinition = null;
		Application coScaleApplication = plugin.getApplicationsApi().getApp();

		for (ApplicationDefinition appDefs : ciService.getApplicationDefinitions()) {
			// check application...???
			if (appDefs.getApplicationName().equals(coScaleApplication.name)) {
				applicationDefinition = appDefs;
				break;
			}
		}

		if (applicationDefinition == null) {
			applicationDefinition = new ApplicationDefinition();
			applicationDefinition.setApplicationName(coScaleApplication.name);
			applicationDefinition = ciService.addApplicationDefinition(applicationDefinition);
		}
		return applicationDefinition;
	}

	/**
	 * Retrieves all request definitions from CoScale.
	 *
	 * @return Returns a list of {@link Request}s.
	 */
	private List<Request> retrieveCoScaleRequestDefinitions() {
		if (plugin.isActive() && plugin.isConnected()) {
			try {
				return plugin.getRequestsApi().all();
			} catch (IOException e) {
				log.warn("Cannot retrieve CoScale request definitions!", e);
			}
		}
		throw new RuntimeException("Cannot retrieve CoScale request definitions! CoScale plugin is not connected.");
	}

	/**
	 * Recursively analyzes the CoScale request hierarchy and for each level adds a matching
	 * expression to the passed {@link IContainerExpression}.
	 *
	 * @param request
	 *            CoScale request to analyze.
	 * @param fullRequestMap
	 *            Map from IDs to {@link Request} for all CoScale requests.
	 * @param container
	 *            The {@link IContainerExpression} to add the matching expressions to.
	 * @param childExpr
	 *            The matching expression of the child request used to detect redundant matching
	 *            expressions in the {@link IContainerExpression}.
	 */
	private void addMatchingExpressionsForRequest(Request request, Map<Long, Request> fullRequestMap, IContainerExpression container, StringMatchingExpression childExpr) {
		if (null != request) {
			if (request.classifierConfig.length() > 1) {
				StringMatchingExpression stringExpr = new StringMatchingExpression();
				stringExpr.setStringValueSource(extractValueSource(request.classifierType));
				stringExpr.setMatchingType(extractMatchExpression(request.classifierConfig));
				if (request.classifierType.equals(RequestClassifierType.HOST) && request.classifierConfig.substring(1).contains(":")) {
					stringExpr.setSnippet(request.classifierConfig.substring(1, request.classifierConfig.indexOf(':')));
				} else {
					stringExpr.setSnippet(request.classifierConfig.substring(1));
				}
				addMatchingExpressionsForRequest(fullRequestMap.get(request.parentId), fullRequestMap, container, stringExpr);
				if ((null == childExpr) || !estimateExpressionSubsumes(stringExpr, childExpr)) {
					container.addOperand(stringExpr);
				}
			} else {
				addMatchingExpressionsForRequest(fullRequestMap.get(request.parentId), fullRequestMap, container, childExpr);
			}
		}
	}

	/**
	 * Estimates whather the given expression subsumes the subsumeeCandidate in terms of boolean
	 * algebra.
	 *
	 * @param expression
	 *            The expression that is a potential subsumer.
	 * @param subsumeeCandidate
	 *            The expression that is a potential subsumee.
	 * @return Returns true, if expression for certain subsumes the subsumeeCandidate. Otherwise,
	 *         returns false.
	 */
	private boolean estimateExpressionSubsumes(StringMatchingExpression expression, StringMatchingExpression subsumeeCandidate) {
		if (!expression.getStringValueSource().equals(subsumeeCandidate.getStringValueSource())) {
			return false;
		}
		if (expression.getMatchingType().equals(subsumeeCandidate.getMatchingType()) && expression.getSnippet().equals(subsumeeCandidate.getSnippet())) {
			return true;
		}
		switch (expression.getMatchingType()) {
		case CONTAINS:
			if (!subsumeeCandidate.getMatchingType().equals(PatternMatchingType.REGEX) && subsumeeCandidate.getSnippet().contains(expression.getSnippet())) {
				return true;
			}
			break;
		case ENDS_WITH:
			if ((subsumeeCandidate.getMatchingType().equals(PatternMatchingType.ENDS_WITH) || subsumeeCandidate.getMatchingType().equals(PatternMatchingType.EQUALS)
					|| subsumeeCandidate.getMatchingType().equals(PatternMatchingType.REGEX)) && subsumeeCandidate.getSnippet().endsWith(expression.getSnippet())) {
				return true;
			}
			break;
		case EQUALS:
			break;
		case REGEX:
			if (expression.getSnippet().equals(".*")) {
				return true;
			}
			break;
		case STARTS_WITH:
			if ((subsumeeCandidate.getMatchingType().equals(PatternMatchingType.STARTS_WITH) || subsumeeCandidate.getMatchingType().equals(PatternMatchingType.EQUALS)
					|| subsumeeCandidate.getMatchingType().equals(PatternMatchingType.REGEX)) && subsumeeCandidate.getSnippet().startsWith(expression.getSnippet())) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * Creates a map (IDs to {@link Request}s) for the given list of {@link Request}s.
	 *
	 * @param requests
	 *            the list of requests to create the map for.
	 * @return Returns a map (IDs to {@link Request}s).
	 */
	private Map<Long, Request> toMap(List<Request> requests) {
		Map<Long, Request> map = new HashMap<>();
		for (Request req : requests) {
			map.put(req.id, req);
		}
		return map;
	}

	/**
	 * Filters the leaf requests from the list of all requests by analyzing the request hierarchy.
	 *
	 * @param requests
	 *            a list of all requests.
	 * @return Returns a ist of leaf requests.
	 */
	private List<Request> filterLeafRequests(List<Request> requests) {
		Set<Long> parentIds = new HashSet<>();
		for (Request req : requests) {
			parentIds.add(req.parentId);
		}

		List<Request> resultList = new ArrayList<>();
		for (Request req : requests) {
			if (!parentIds.contains(req.id)) {
				resultList.add(req);
			}
		}

		return resultList;
	}

	/**
	 * Initializes the {@link CoScaleBusinessRequestMapper} component.
	 */
	@PostConstruct
	public void init() {
		if (plugin.isActive()) {
			pluginActivated();
		}

		plugin.addPluginStateListener(this);
	}
}
