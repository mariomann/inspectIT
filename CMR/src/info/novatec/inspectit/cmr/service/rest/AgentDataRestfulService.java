/**
 *
 */
package info.novatec.inspectit.cmr.service.rest;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.rest.error.JsonError;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Restful service provider for agent data.
 *
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/agentdata")
public class AgentDataRestfulService {

	/**
	 * Reference to the existing {@link IGlobalDataAccessService}.
	 */
	@Autowired
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Reference to the existing {@link IInvocationDataAccessService}.
	 */
	@Autowired
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider agentStatusProvider;

	/**
	 * Returns Overview of captured invocation sequences for a given platformId.
	 * <p>
	 * <i> Example URL: /agentdata/get-invocation-sequences?platformId=30</i>
	 * </p>
	 *
	 * @param platformId
	 *            PLATFORMID bounded from path.
	 * @return List<InvocationSequenceData>
	 */
	@RequestMapping(method = RequestMethod.GET, value = "get-invocation-sequences")
	@ResponseBody
	public List<InvocationSequenceData> getInvocationSequenceOverview(@RequestParam(value = "platformId", required = true) long platformId) {
		return invocationDataAccessService.getInvocationSequenceOverview(platformId, 100, null);
	}

	/**
	 * Returns agent data "last data" was sent.
	 * <p>
	 * <i> Example URL: /agentdata/detailedOverview</i>
	 * </p>
	 *
	 * @return Map<String, String>
	 */
	@RequestMapping(method = RequestMethod.GET, value = "detailedOverview")
	@ResponseBody
	public Map<String, String> getOverview() {
		Map<PlatformIdent, AgentStatusData> agentsOverviewMap = globalDataAccessService.getAgentsOverview();
		Map<String, String> agentsMap = new HashMap<String, String>();

		for (Entry<PlatformIdent, AgentStatusData> platformIdent : agentsOverviewMap.entrySet()) {
			agentsMap.put(platformIdent.getKey().getAgentName(), agentsOverviewMap.get(platformIdent.getKey()).getAgentConnection().toString() + ", id=" + platformIdent.getKey().getId() + ", Version="
					+ platformIdent.getKey().getVersion() + ", MillisSinceLastData=" + agentsOverviewMap.get(platformIdent.getKey()).getMillisSinceLastData());
		}

		return agentsMap;
	}

	/**
	 * Handling of all the exceptions happening in this controller.
	 *
	 * @param exception
	 *            Exception being thrown
	 * @return {@link ModelAndView}
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(Exception exception) {
		return new JsonError(exception).asModelAndView();
	}

}
