package rocks.inspectit.shared.cs.cmr.property.update.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;

/**
 * {@link AbstractPropertyUpdate} for double property.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "double-property-update")
public class DoublePropertyUpdate extends AbstractPropertyUpdate<Double> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private Double updateValue;

	/**
	 * No-arg constructor.
	 */
	protected DoublePropertyUpdate() {
	}

	/**
	 * Default constructor.
	 *
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public DoublePropertyUpdate(SingleProperty<Double> property, Double updateValue) {
		super(property, updateValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double getUpdateValue() {
		return updateValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUpdateValue(Double updateValue) {
		this.updateValue = updateValue;
	}

}
