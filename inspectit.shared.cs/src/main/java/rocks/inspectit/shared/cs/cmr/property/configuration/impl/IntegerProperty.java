package rocks.inspectit.shared.cs.cmr.property.configuration.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.impl.IntegerPropertyUpdate;

/**
 * Property holding long values.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "integer-property")
public class IntegerProperty extends SingleProperty<Integer> {

	/**
	 * Used value.
	 */
	@XmlAttribute(name = "used-value")
	private Integer usedValue;

	/**
	 * Default value.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private Integer defaultValue;

	/**
	 * No-arg constructor.
	 */
	public IntegerProperty() {
	}

	/**
	 *
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @param logicalName
	 *            The logical name of the property that is used in the configuration.
	 * @param defaultValue
	 *            Default value.
	 * @param advanced
	 *            If the property is advanced, thus should be available only to expert users.
	 * @param serverRestartRequired
	 *            If the change of this property should trigger server restart.
	 * @throws IllegalArgumentException
	 *             If name, section, logical name or default value are <code>null</code>.
	 * @see SingleProperty#SingleProperty(String, String, String, Object, boolean, boolean)
	 */
	public IntegerProperty(String name, String description, String logicalName, Integer defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	@Override
	protected AbstractPropertyUpdate<Integer> createPropertyUpdate(Integer updateValue) {
		return new IntegerPropertyUpdate(this, updateValue);
	}

	/**
	 * Gets {@link #usedValue}.
	 *
	 * @return {@link #usedValue}
	 */
	@Override
	protected Integer getUsedValue() {
		return usedValue;
	}

	/**
	 * Sets {@link #usedValue}.
	 *
	 * @param usedValue
	 *            New value for {@link #usedValue}
	 */
	@Override
	protected void setUsedValue(Integer usedValue) {
		this.usedValue = usedValue;
	}

	/**
	 * Gets {@link #defaultValue}.
	 *
	 * @return {@link #defaultValue}
	 */
	@Override
	public Integer getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets {@link #defaultValue}.
	 *
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	@Override
	protected void setDefaultValue(Integer defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer parseLiteral(String literal) {
		try {
			return Integer.parseInt(literal);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
