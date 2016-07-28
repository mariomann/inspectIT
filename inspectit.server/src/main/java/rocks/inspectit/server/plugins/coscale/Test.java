package rocks.inspectit.server.plugins.coscale;

import java.io.IOException;

import com.coscale.sdk.client.ApiFactory;
import com.coscale.sdk.client.Credentials;
import com.coscale.sdk.client.applications.ApplicationsApi;

/**
 * @author Alexander Wert
 *
 */
public class Test {
	public static void main(String[] args) throws IOException {
		String appId = "00000225-00aa-4516-9b9a-d4ee2cf4d248";
		String token = "08d12489-dad7-4949-9ab2-8a8c4b8c46f7";
		Credentials credentials = Credentials.Token(token);
		ApiFactory apiFactory = new ApiFactory(appId, credentials);
		apiFactory.getApiClient().setSource("inspectIT");
		apiFactory.getApiClient().setBaseURL("http://23.97.203.43");

		ApplicationsApi appApi = apiFactory.getApplicationsApi();
		appApi.getApp();

		// EventsApi eventsApi = apiFactory.getEventsApi();
		// String eventName = "InspectIT-Forensic";

		// Builder queryBuilder = new Builder();
		// queryBuilder.selectBy("name", eventName);
		//
		// List<Event> events = eventsApi.all(queryBuilder.build());
		// Event event;
		// if (!events.isEmpty()) {
		// event = events.get(0);
		// } else {
		// EventInsert eventInsert = new EventInsert(eventName, "forensic data from inspectIT",
		// null, "InspectIT", "gears");
		// event = eventsApi.insert(eventInsert);
		// }
		// Long metricId = null;
		// RequestsApi requestsApi = apiFactory.getRequestsApi();
		//
		// List<Request> requests = requestsApi.all();
		// for (Request req : requests) {
		// if (req.name.equals("dvd")) {
		// metricId = req.id;
		// break;
		// }
		// }


		// EventDataInsert eventDataInsert = new EventDataInsert("forensic test", -5l, 0l, null,
		// "a");
		// eventData.metric = metricId.intValue();
		// EventData eventData = eventsApi.insertData(event.id, eventDataInsert);

		// eventsApi.uploadBinary(event.id, eventData.id, data)

		// ApplicationsApi appApi = apiFactory.getApplicationsApi();
		// appApi.all();
		// Application app = appApi.getApp(appId);
		// System.out.println(app.name);
		// MetricsApi metricsApi = apiFactory.getMetricsApi();
		// Options options = new Options.Builder().expand("requests").build();
		// List<Metric> metrics = metricsApi.all();
		// for (Metric m : metrics) {
		// System.out.println(m.id);
		// }

		// RequestsApi requestsApi = apiFactory.getRequestsApi();
		//
		// List<Request> requests = requestsApi.all();
		// Request req1 = null;
		// for (Request req : requests) {
		// if (req.name.equals("dvd")) {
		// req1 = req;
		// break;
		// }
		// }
		//
		// Options options = new Options.Builder().expand("metrics").build();
		// requests = requestsApi.all(options);
		// Request req2 = null;
		// for (Request req : requests) {
		// if (req.name.equals("dvd")) {
		// req2 = req;
		// break;
		// }
		// }
		//
		// MetricsApi metricsApi = apiFactory.getMetricsApi();
		// List<Metric> metrics = metricsApi.all();
		// for (Metric m : metrics) {
		// System.out.println(m.name);
		// }
	}
}
