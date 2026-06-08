package jrm.aui.status;

import lombok.Setter;

/**
 * Factory interface for creating StatusRenderer instances. This interface provides default implementations for the methods defined in the StatusRenderer interface, allowing for easy customization of the rendering behavior by changing the underlying factory instance.
 * 
 * The Factory class within this interface holds a static instance of a StatusRenderer, which can be set to any implementation of the StatusRenderer interface. By default, it is initialized to an Html5Renderer instance.
 * 
 * Implementing classes can override the getFactory() method to provide a different factory instance if needed, allowing for flexible rendering strategies without modifying the core logic of the StatusRenderer methods.
 * 
 * @author optyfr
 */
public interface StatusRendererFactory extends StatusRenderer {
    /** Factory class for managing the StatusRenderer instance. This class holds a static instance of a StatusRenderer, which can be set to any implementation of the StatusRenderer interface. By default, it is initialized to an Html5Renderer instance. The Factory class provides a centralized location for managing the rendering strategy used by the StatusRenderer methods, allowing for easy customization and flexibility in rendering behavior.
     */
    public static class Factory {
        /** The static instance of the StatusRenderer used by the factory. This instance can be set to any implementation of the StatusRenderer interface, allowing for flexible rendering strategies. By default, it is initialized to an Html5Renderer instance, which provides a standard HTML5 rendering behavior. Implementing classes can change this instance to use a different rendering strategy as needed.
         * @param instance the StatusRenderer instance to be used by the factory, initialized to an Html5Renderer by default
         */
        private static @Setter StatusRenderer instance = new Html5Renderer();

        /** Private constructor to prevent instantiation of the Factory class. This class is intended to be used as a static holder for the StatusRenderer instance, and should not be instantiated directly. The private constructor ensures that no instances of the Factory class can be created, enforcing its role as a static utility class for managing the StatusRenderer instance.
         */
        private Factory() {
        }
    }

    /** Retrieves the StatusRenderer instance from the factory. This method returns the current StatusRenderer instance being used by the factory, which can be set to any implementation of the StatusRenderer interface. By default, it will return an instance of Html5Renderer, but it can be changed to return a different implementation if needed. Implementing classes can override this method to provide a different factory instance if necessary, allowing for flexible rendering strategies without modifying the core logic of the StatusRenderer methods.
     * @return the current StatusRenderer instance being used by the factory
     */
    default StatusRenderer getFactory() {
        return Factory.instance;
    }

    /** Default implementation of the toDocument method defined in the StatusRenderer interface. This method delegates the rendering logic to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for rendering, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom rendering behavior if necessary, while still leveraging the underlying factory instance for consistent rendering logic.
     * @param str the input string to be rendered as a document
     * @return the rendered document string based on the current factory instance
     */
    @Override
    default String toDocument(CharSequence str) {
        return getFactory().toDocument(str);
    }

    /** Default implementation of the toNoBR method defined in the StatusRenderer interface. This method delegates the rendering logic to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for rendering, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom rendering behavior if necessary, while still leveraging the underlying factory instance for consistent rendering logic.
     * @param str the input string to be rendered without line breaks
     * @return the rendered string without line breaks based on the current factory instance
     */
    @Override
    default String toNoBR(CharSequence str) {
        return getFactory().toNoBR(str);
    }

    /** Default implementation of the toLabel method defined in the StatusRenderer interface. This method delegates the rendering logic to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for rendering, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom rendering behavior if necessary, while still leveraging the underlying factory instance for consistent rendering logic.
     * @param str the input string to be rendered as a label
     * @param webcolor the web color to be applied to the label (e.g., "#FF0000" for red)
     * @param bold whether the label should be rendered in bold
     * @param italic whether the label should be rendered in italic
     * @return the rendered label string based on the current factory instance and specified styling options
     */
    @Override
    default String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic) {
        return getFactory().toLabel(str, webcolor, bold, italic);
    }

    /** Default implementation of the progress method defined in the StatusRenderer interface. This method delegates the rendering logic to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for rendering, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom rendering behavior if necessary, while still leveraging the underlying factory instance for consistent rendering logic.
     * @param width the width of the progress bar
     * @param i the current progress value
     * @param max the maximum progress value
     * @param msg an optional message to be displayed alongside the progress bar
     * @return the rendered progress string based on the current factory instance and specified parameters
     */
    @Override
    default String progress(int width, int i, int max, String msg) {
        return getFactory().progress(width, i, max, msg);
    }

    /** Default implementation of the escape method defined in the StatusRenderer interface. This method delegates the escaping logic to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for escaping, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom escaping behavior if necessary, while still leveraging the underlying factory instance for consistent escaping logic.
     * @param str the input string to be escaped
     * @return the escaped string based on the current factory instance
     */
    @Override
    default String escape(CharSequence str) {
        return getFactory().escape(str);
    }

    /** Default implementation of the hasProgress method defined in the StatusRenderer interface. This method delegates the logic to determine if progress rendering is supported to the StatusRenderer instance provided by the factory, allowing for flexible rendering strategies based on the current factory instance. By default, it will use the Html5Renderer instance for this logic, but it can be changed to use a different implementation if needed. Implementing classes can override this method to provide custom logic for determining progress rendering support if necessary, while still leveraging the underlying factory instance for consistent behavior.
     * @return true if progress rendering is supported by the current factory instance, false otherwise
     */
    @Override
    default boolean hasProgress() {
        return getFactory().hasProgress();
    }
}
