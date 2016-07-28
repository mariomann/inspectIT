package rocks.inspectit.ui.rcp.property.control.impl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.shared.cs.cmr.property.configuration.impl.DoubleProperty;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.property.IPropertyUpdateListener;
import rocks.inspectit.ui.rcp.property.control.AbstractPropertyControl;

/**
 * {@link AbstractPropertyControl} for the double property.
 *
 * @author Alexander Wert
 *
 */
public class DoublePropertyControl extends AbstractPropertyControl<DoubleProperty, Double> {

	/**
	 * Text to display long value.
	 */
	private Text text;

	/**
	 * Default constructor.
	 *
	 * @param property
	 *            Property.
	 * @param propertyUpdateListener
	 *            Property update listener to report updates to.
	 */
	public DoublePropertyControl(DoubleProperty property, IPropertyUpdateListener propertyUpdateListener) {
		super(property, propertyUpdateListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createControl(Composite parent) {
		text = new Text(parent, SWT.BORDER | SWT.RIGHT);
		text.setText(NumberFormatter.formatDouble(property.getValue()));
		text.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String oldText = text.getText();
				String update = e.text;
				String newText = oldText.substring(0, e.start) + update + oldText.substring(e.end, oldText.length());

				// allow blank text
				if (StringUtils.isNotBlank(newText)) {
					// allow minus to be specified only
					if ((1 == newText.length()) && ('-' == newText.charAt(0))) {
						return;
					}

					// otherwise prove we have a valid long number
					try {
						Double.parseDouble(newText);
					} catch (NumberFormatException exception) {
						e.doit = false;
						return;
					}
				}
			}
		});
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String valueText = text.getText();
				if (!valueText.isEmpty() && ((valueText.charAt(0) != '-') || (valueText.length() > 1))) {
					Double value = Double.parseDouble(valueText);
					sendPropertyUpdateEvent(value);
				}
			}
		});
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String valueText = text.getText();
				if (valueText.isEmpty()) {
					text.setText(NumberFormatter.formatDouble(getLastCorrectValue().doubleValue()));
				}
			}
		});
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showDefaultValue() {
		text.setText(NumberFormatter.formatDouble(property.getDefaultValue().doubleValue()));
	}

}
