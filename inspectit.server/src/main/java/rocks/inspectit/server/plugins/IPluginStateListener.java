package rocks.inspectit.server.plugins;

/**
 * This listener observes the state of inspectIT plugins.
 *
 * @author Alexander Wert
 *
 */
public interface IPluginStateListener {

	/**
	 * Notification about a plugin activation.
	 */
	void pluginActivated();

	/**
	 * Notification about a plugin de-activation.
	 */
	void pluginDeactivated();
}
