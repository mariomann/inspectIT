package rocks.inspectit.server.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class holding common functionality of all inspectIT plugins.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractPlugin {
	/**
	 * Listeners that are notified when the plugin state changes.
	 */
	private List<IPluginStateListener> pluginStateListeners;

	/**
	 * Indicates whether the plugin is active or not.
	 *
	 * @return Returns true if plugin is active.
	 */
	public abstract boolean isActive();

	/**
	 * Adds a {@link IPluginStateListener}. The listener is notified if the state of this plugin
	 * changes.
	 *
	 * @param listener
	 *            {@link IPluginStateListener} instance to add.
	 */
	public void addPluginStateListener(IPluginStateListener listener) {
		getPluginStateListeners().add(listener);
	}

	/**
	 * Returns a list of all {@link IPluginStateListener} instances listening to this plugin's
	 * state.
	 *
	 * @return Returns list of {@link IPluginStateListener} instances.
	 */
	public List<IPluginStateListener> getPluginStateListeners() {
		if (null == pluginStateListeners) {
			pluginStateListeners = new ArrayList<IPluginStateListener>();
		}
		return pluginStateListeners;
	}

	/**
	 * Inner method to be called when {@link IPluginStateListener} instances should be notified
	 * about a plugin activation.
	 */
	protected void notifyActivated() {
		for (IPluginStateListener listener : getPluginStateListeners()) {
			listener.pluginActivated();
		}
	}

	/**
	 * Inner method to be called when {@link IPluginStateListener} instances should be notified
	 * about a plugin de-activation.
	 */
	protected void notifyDeactivated() {
		for (IPluginStateListener listener : getPluginStateListeners()) {
			listener.pluginDeactivated();
		}
	}
}
