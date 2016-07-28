package rocks.inspectit.shared.cs.cmr.property.configuration.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.impl.DoublePropertyUpdate;

/**
 * Property holding double Values.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "double-property")
public class DoubleProperty extends SingleProperty<Double> {

	/**
	 * Used value.
	 */
	@XmlAttribute(name = "used-value")
	private Double usedValue;

	/**
	 * Default value.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private Double defaultValue;

	/**
	 * No-arg constructor.
	 */
	public DoubleProperty() {
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
	public DoubleProperty(String name, String description, String logicalName, Double defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPropertyUpdate<Double> createPropertyUpdate(Double updateValue) {
		return new DoublePropertyUpdate(this, updateValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setDefaultValue(Double defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Double getUsedValue() {
		return usedValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUsedValue(Double usedValue) {
		this.usedValue = usedValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double parseLiteral(String literal) {
		return Double.parseDouble(literal);
	}

}
