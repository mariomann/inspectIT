package rocks.inspectit.server.plugins.coscale;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.coscale.sdk.client.ApiClient;
import com.coscale.sdk.client.ApiFactory;
import com.coscale.sdk.client.Credentials;
import com.coscale.sdk.client.applications.ApplicationsApi;
import com.coscale.sdk.client.data.DataApi;
import com.coscale.sdk.client.events.EventsApi;
import com.coscale.sdk.client.metrics.MetricsApi;
import com.coscale.sdk.client.requests.RequestsApi;
import com.coscale.sdk.client.servers.ServersApi;

import rocks.inspectit.server.plugins.AbstractPlugin;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * CoScale data publishing plugin.
 *
 * @author Alexander Wert
 *
 */
@Component
public class CoScalePlugin extends AbstractPlugin {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Plugin state.
	 */
	@Value(value = "${cmr.data.extensions.coscale.baseUrl}")
	private String baseURL;

	/**
	 * Plugin state.
	 */
	@Value(value = "${cmr.data.extensions.coscale.active}")
	private boolean pluginActive;

	/**
	 * Id of the application managed with CoScale.
	 */
	@Value(value = "${cmr.data.extensions.coscale.appId}")
	private String appId;

	/**
	 * CoScale access token.
	 */
	@Value(value = "${cmr.data.extensions.coscale.token}")
	private String coScaleToken;

	/**
	 * Log-in state.
	 */
	private boolean loggedIn = false;

	/**
	 * CoScale Data API.
	 */
	private DataApi dataApi;

	/**
	 * CoScale Servers API.
	 */
	private ServersApi serversApi;

	/**
	 * CoScale Requests API.
	 */
	private RequestsApi requestsApi;

	/**
	 * CoScale Events API.
	 */
	private EventsApi eventsApi;

	/**
	 * CoScale Metrics API.
	 */
	private MetricsApi metricsApi;

	/**
	 * CoScale Applications API.
	 */
	private ApplicationsApi applicationsApi;

	/**
	 * Initialize plugin.
	 */
	@PostConstruct
	@PropertyUpdate(properties = { "cmr.data.extensions.coscale.active", "cmr.data.extensions.coscale.token", "cmr.data.extensions.coscale.appId", "cmr.data.extensions.coscale.baseUri" })
	public void init() {
		if (pluginActive) {
			activatePlugin();
		} else {
			deactivatePlugin();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return pluginActive;
	}

	/**
	 * Indicates whether the plugin is connected to the CoScale service.
	 *
	 * @return Returns true if plugin is connected.
	 */
	public boolean isConnected() {
		return loggedIn;
	}

	/**
	 * Activates the CoScale plugin.
	 */
	private void activatePlugin() {
		try {
			connectToCoScale();
			loggedIn = true;
			notifyActivated();
		} catch (IOException e) {
			log.error("Failed activating CoScale Plugin!", e);
			loggedIn = false;
		}
	}

	/**
	 * Connects to the CoScale service.
	 *
	 * @throws IOException
	 *             if connection fails
	 */
	private void connectToCoScale() throws IOException {
		Credentials credentials = Credentials.Token(coScaleToken);
		ApiFactory apiFactory = new ApiFactory(appId, credentials);
		ApiClient.setSource("inspectIT");
		apiFactory.getApiClient().setBaseURL(baseURL);
		dataApi = apiFactory.getDataApi();
		eventsApi = apiFactory.getEventsApi();
		requestsApi = apiFactory.getRequestsApi();
		serversApi = apiFactory.getServersApi();
		metricsApi = apiFactory.getMetricsApi();
		applicationsApi = apiFactory.getApplicationsApi();
		metricsApi.all();
		loggedIn = true;
	}

	/**
	 * Deactivates the coscale plugin.
	 */
	private void deactivatePlugin() {
		loggedIn = false;
		notifyDeactivated();
	}

	// ****************************************************************
	// ****************************************************************
	// *** GETTERS and SETTERS
	// ****************************************************************
	// ****************************************************************

	/**
	 * Gets {@link #dataApi}.
	 *
	 * @return {@link #dataApi}
	 */
	public DataApi getDataApi() {
		return this.dataApi;
	}

	/**
	 * Gets {@link #serversApi}.
	 *
	 * @return {@link #serversApi}
	 */
	public ServersApi getServersApi() {
		return this.serversApi;
	}

	/**
	 * Gets {@link #requestsApi}.
	 *
	 * @return {@link #requestsApi}
	 */
	public RequestsApi getRequestsApi() {
		return this.requestsApi;
	}

	/**
	 * Gets {@link #eventsApi}.
	 *
	 * @return {@link #eventsApi}
	 */
	public EventsApi getEventsApi() {
		return this.eventsApi;
	}

	/**
	 * Gets {@link #metricsApi}.
	 *
	 * @return {@link #metricsApi}
	 */
	public MetricsApi getMetricsApi() {
		return this.metricsApi;
	}

	/**
	 * Gets {@link #applicationsApi}.
	 *
	 * @return {@link #applicationsApi}
	 */
	public ApplicationsApi getApplicationsApi() {
		return applicationsApi;
	}
}
