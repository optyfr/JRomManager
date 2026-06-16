package jrm.aui.status;

/**
 * HTML5 renderer for progress bars, utilizing the &lt;progress&gt; element for modern browsers. This class extends the
 * Html4Renderer to provide an updated implementation of the internalProgress method.
 * 
 * @author optyfr
 */
public class Html5Renderer extends Html4Renderer {

    /**
     * Constructs a new Html5Renderer instance with default settings. This constructor does not perform any specific initialization
     * and can be used to create a renderer for HTML5 progress bars.
     */
    public Html5Renderer() {
        /* default constructor */ }

    /**
     * Generates an HTML5 progress bar using the &lt;progress&gt; element.
     *
     * @param width the width of the progress bar in pixels
     * @param i the current progress value
     * @param max the maximum progress value
     * 
     * @return a string containing the HTML representation of the progress bar
     */
    @Override
    protected String internalProgress(final int width, final long i, final long max) {
        return String.format("<progress style=\"width:%dpx\" value=\"%d\" max=\"%d\"></progress>", width, i, max);
    }
}
