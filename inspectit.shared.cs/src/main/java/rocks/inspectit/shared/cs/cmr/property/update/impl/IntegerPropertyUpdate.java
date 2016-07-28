package rocks.inspectit.shared.cs.cmr.property.update.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;

/**
 * {@link AbstractPropertyUpdate} for long property.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "integer-property-update")
public class IntegerPropertyUpdate extends AbstractPropertyUpdate<Integer> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private Integer updateValue;

	/**
	 * No-arg constructor.
	 */
	protected IntegerPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 *
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public IntegerPropertyUpdate(SingleProperty<Integer> property, Integer updateValue) {
		super(property, updateValue);
	}

	/**
	 * Gets {@link #updateValue}.
	 *
	 * @return {@link #updateValue}
	 */
	@Override
	public Integer getUpdateValue() {
		return updateValue;
	}

	/**
	 * Sets {@link #updateValue}.
	 *
	 * @param updateValue
	 *            New value for {@link #updateValue}
	 */
	@Override
	protected void setUpdateValue(Integer updateValue) {
		this.updateValue = updateValue;
	}

}