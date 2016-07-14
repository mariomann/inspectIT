package rocks.inspectit.agent.java.sensor.platform.provider.def;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;

/**
 * Uses the {@link java.lang.management.MemoryMXBean} in order to retrieve all information that are
 * provided here.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class DefaultMemoryInfoProvider implements MemoryInfoProvider {

	/**
	 * The MXBean used to retrieve heap memory information.
	 */
	private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

	/**
	 * The MXBean to retrieve memory informations of available memory pools.
	 */
	private List<MemoryPoolMXBean> memoryPoolBean = ManagementFactory.getMemoryPoolMXBeans();

	/**
	 * {@inheritDoc}
	 */
	public MemoryUsage getHeapMemoryUsage() {
		return memoryBean.getHeapMemoryUsage();
	}

	/**
	 * {@inheritDoc}
	 */
	public MemoryUsage getNonHeapMemoryUsage() {
		return memoryBean.getNonHeapMemoryUsage();
	}

	/**
	 * {@inheritDoc}}
	 */
	public MemoryUsage getCodeCacheUsage() {
		for (MemoryPoolMXBean item : memoryPoolBean) {
			if (item.getName().matches("^[cC]ode.[cC]ache$") && item.isValid()) {
				return item.getUsage();
			}
		}
		return null;
	}
}
