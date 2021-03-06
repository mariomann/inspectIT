package rocks.inspectit.shared.cs.cmr.service;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;

/**
 * 
 * @author Alfred Krauss
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IJmxDataAccessService {

	/**
	 * Returns a list of captured jmx data for a given template. In the template, only the platform
	 * id is extracted.
	 * 
	 * @param jmxSensorValueData
	 *            The template containing the platform id.
	 * @return The list of jmx value data.
	 */
	List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData);

	/**
	 * Returns a list of captured jmx data for a given template in a time frame. In the template,
	 * only the platform id is extracted. Only the latest data of a sensor will be returned.
	 * 
	 * @param jmxSensorValueData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of jmx value data.
	 */
	List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData jmxSensorValueData, Date fromDate, Date toDate);
}