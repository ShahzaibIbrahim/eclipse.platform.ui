package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorDefinition is the class that holds onto
 * the label decorator, the name and the name of the
 * class a decorator definition applies to,
 */

public class DecoratorDefinition {

	private String name;
	private String objectClass;
	private ILabelDecorator decorator;
	private boolean enabled = false;
	private boolean adaptable;
	private String id;
	private IConfigurationElement element;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	DecoratorDefinition(
		String identifier,
		String label,
		String className,
		boolean isAdaptable,
		IConfigurationElement configElement) {
		id = identifier;
		name = label;
		objectClass = className;
		adaptable = isAdaptable;
		element = configElement;
	}

	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the objectClass.
	 * @return Returns a String
	 */
	public String getObjectClass() {
		return objectClass;
	}

	/**
	 * Sets the objectClass.
	 * @param objectClass The objectClass to set
	 */
	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * Gets the decorator. Throws a CoreException if there is a problem
	 * creating the decorator.
	 * @return Returns a ILabelDecorator
	 */
	public ILabelDecorator getDecorator() throws CoreException {
		if (decorator == null)
			decorator =
				(ILabelDecorator) WorkbenchPlugin.createExtension(
					element,
					WizardsRegistryReader.ATT_CLASS);

		return decorator;

	}

	/**
	 * Sets the decorator.
	 * @param decorator The decorator to set
	 */
	public void setDecorator(ILabelDecorator decorator) {
		this.decorator = decorator;
	}

	/**
	 * Gets the enabled.
	 * @return Returns a boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag and adds or removes the decorator
	 * manager as a listener as appropriate.
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		DecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();

		try {
			if (enabled)
				getDecorator().addListener(manager);
			else {
				if (decorator != null) {
					ILabelDecorator cached = decorator;
					cached.removeListener(manager);
					//Clear the decorator before disposing
					decorator = null;
					cached.dispose();
				}
			}

		} catch (CoreException exception) {
			handleCoreException(exception);
		}
	}

	/**
	 * Return whether or not this decorator should be 
	 * applied to adapted types.
	 */

	public boolean isAdaptable() {
		return adaptable;
	}
	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

	/** 
	 * A CoreException has occured. Inform the user and disable
	 * the receiver.
	 */

	private void handleCoreException(CoreException exception) {

		//If there is an error then reset the enabling to false
		MessageDialog.openError(
			null,
			WorkbenchMessages.getString("Internal_error"),
			exception.getLocalizedMessage());
		this.enabled = false;
	}

	/**
	 * Decorate the image provided for the element type.
	 * Return null if there is no image or if an error occurs.
	 */
	Image decorateImage(Image image, Object element) {
		try {
			return getDecorator().decorateImage(image, element);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}

	/**
	 * Decorate the text provided for the element type.
	 * Return null if there is no text or if there is an exception.
	 */
	String decorateText(String text, Object element) {
		try {
			return getDecorator().decorateText(text, element);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}

	/**
	 * Decorate the text and the image provided for the element type.
	 * If the decorator for the receiver is an instance of 
	 * <code>ICombinedLabelDecorator</code> then do both at the same
	 * time - otherwise call the decorateText and decorateImage
	 * seperately. 
	 */
	void decorateLabel(
		Object element,
		CombinedLabel decorationResult) {
		try {
			ILabelDecorator decorator = getDecorator();
		} catch (CoreException exception) {
			handleCoreException(exception);
			return;
		}
		if (decorator instanceof ICombinedLabelDecorator)
			((ICombinedLabelDecorator) decorator).decorateLabel(
				element,
				decorationResult);
		else {
			String newText = decorator.decorateText(decorationResult.getText(), element);
			Image newImage = decorator.decorateImage(decorationResult.getImage(), element);
			if (newText != null)
				decorationResult.setText(newText);
			if (newImage != null)
				decorationResult.setImage(newImage);
		}
	}

	/**
	 * Add a listener for the decorator.If there is an exception
	 * then inform the user and disable the receiver.
	 */
	void addListener(ILabelProviderListener listener) {
		try {
			getDecorator().addListener(listener);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name. If there is an 
	* exception disable the receiver and return false;
	*/
	boolean isLabelProperty(Object element, String property) {
		try {
			return getDecorator().isLabelProperty(element, property);
		} catch (CoreException exception) {
			handleCoreException(exception);
			return false;
		}
	}

}