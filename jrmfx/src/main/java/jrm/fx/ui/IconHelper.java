package jrm.fx.ui;

import javafx.scene.image.ImageView;

/**
 * Utility class for creating styled icon ImageViews.
 */
final class IconHelper {
	private IconHelper() {
		// utility class
	}

	/**
	 * Creates a styled ImageView icon from the given resource path.
	 *
	 * @param iconPath the resource path to the icon image
	 * @return a configured ImageView with preserved ratio and "icon" style class
	 */
	static ImageView createIcon(String iconPath) {
		final var iv = new ImageView(MainFrame.getIcon(iconPath));
		iv.setPreserveRatio(true);
		iv.getStyleClass().add("icon"); //$NON-NLS-1$
		return iv;
	}
}
