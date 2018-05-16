package jrm;

import java.beans.Beans;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private Messages() {
		// do not instantiate
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Bundle access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String BUNDLE_NAME = "jrm.resources.Messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = Messages.loadBundle();
	private static ResourceBundle loadBundle() {
		return ResourceBundle.getBundle(Messages.BUNDLE_NAME);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Strings access
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getString(final String key) {
		try {
			final ResourceBundle bundle = Beans.isDesignTime() ? Messages.loadBundle() : Messages.RESOURCE_BUNDLE;
			return bundle.getString(key);
		} catch (final MissingResourceException e) {
			return "!" + key + "!";
		}
	}
}
