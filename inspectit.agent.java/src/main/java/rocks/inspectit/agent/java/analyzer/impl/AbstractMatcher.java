package rocks.inspectit.agent.java.analyzer.impl;

import rocks.inspectit.agent.java.analyzer.IClassPoolAnalyzer;
import rocks.inspectit.agent.java.analyzer.IMatcher;
import rocks.inspectit.agent.java.config.impl.UnregisteredSensorConfig;

/**
 * The abstract matcher class used to store the reference to the sensor configuration.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractMatcher implements IMatcher {

	/**
	 * The link to the class pool analyzer which is used by some Matcher implementations.
	 */
	protected final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The {@link UnregisteredSensorConfig} object to retrieve all the needed sensor informations
	 * from.
	 */
	protected final UnregisteredSensorConfig unregisteredSensorConfig;

	/**
	 * The only constructor which needs a reference to the {@link UnregisteredSensorConfig} instance
	 * of the corresponding configuration.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 */
	public AbstractMatcher(IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig) {
		this.classPoolAnalyzer = classPoolAnalyzer;
		this.unregisteredSensorConfig = unregisteredSensorConfig;
	}

}
