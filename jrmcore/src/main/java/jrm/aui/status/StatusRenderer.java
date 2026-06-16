package jrm.aui.status;

import java.util.Optional;

/**
 * Interface defining methods for rendering status messages in various formats, such as colored labels, progress bars, and escaped
 * strings. Implementations of this interface can provide different rendering styles for status messages, allowing for flexible and
 * customizable output in user interfaces or logs.
 * 
 * @author optyfr
 */
interface StatusRenderer {

    /**
     * Converts the given character sequence into a document format suitable for rendering. The specific format and styling of the
     * resulting string will depend on the implementation of this method, allowing for different visual representations of the input
     * text.
     * 
     * @param str the character sequence to be converted into a document format
     * 
     * @return a string representing the input character sequence in a document format suitable for rendering
     */
    public String toDocument(final CharSequence str);

    /**
     * Converts the given object into a string representation. If the input object is null, an empty string is returned. This method
     * provides a default implementation that can be overridden by implementing classes to provide custom string representations for
     * specific types of objects.
     * 
     * @param any the object to be converted into a string representation
     * 
     * @return a string representation of the input object, or an empty string if the input is null
     */
    public default String toStr(Object any) {
        return Optional.ofNullable(any).map(Object::toString).orElse("");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering without line breaks. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it does not contain any line breaks.
     * 
     * @param str the character sequence to be converted into a format without line breaks
     * 
     * @return a string representing the input character sequence in a format suitable for rendering without line breaks
     */
    public String toNoBR(final CharSequence str);

    /**
     * Converts the given character sequence into a format suitable for rendering with line breaks. The specific formatting applied
     * to the input string will depend on the implementation of this method, allowing for different visual representations of the
     * text while ensuring that it can contain line breaks as needed.
     * 
     * @param str the character sequence to be converted into a format with line breaks
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with line breaks
     */
    public default String toBlue(CharSequence str) {
        return toLabel(str, "blue");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold blue styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold blue formatting.
     * 
     * @param str the character sequence to be converted into a format with bold blue styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold blue styling
     */
    public default String toBoldBlue(CharSequence str) {
        return toLabel(str, "blue", true);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with italic blue styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with italic blue formatting.
     * 
     * @param str the character sequence to be converted into a format with italic blue styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with italic blue styling
     */
    public default String toRed(CharSequence str) {
        return toLabel(str, "red");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold red styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold red formatting.
     * 
     * @param str the character sequence to be converted into a format with bold red styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold red styling
     */
    public default String toGreen(CharSequence str) {
        return toLabel(str, "green");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold green styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold green formatting.
     * 
     * @param str the character sequence to be converted into a format with bold green styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold green styling
     */
    public default String toBoldGreen(CharSequence str) {
        return toLabel(str, "green", true);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with italic green styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with italic green formatting.
     * 
     * @param str the character sequence to be converted into a format with italic green styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with italic green styling
     */
    public default String toGray(CharSequence str) {
        return toLabel(str, "gray");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold gray styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold gray formatting.
     * 
     * @param str the character sequence to be converted into a format with bold gray styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold gray styling
     */
    public default String toOrange(CharSequence str) {
        return toLabel(str, "orange");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold orange styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold orange formatting.
     * 
     * @param str the character sequence to be converted into a format with bold orange styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold orange styling
     */
    public default String toPurple(CharSequence str) {
        return toLabel(str, "purple");
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with bold purple styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with bold purple formatting.
     * 
     * @param str the character sequence to be converted into a format with bold purple styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with bold purple styling
     */
    public default String toBoldBlack(CharSequence str) {
        return toLabel(str, "black", true);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with italic black styling. The specific formatting
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that it is styled with italic black formatting.
     * 
     * @param str the character sequence to be converted into a format with italic black styling
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with italic black styling
     */
    public default String toItalicBlack(CharSequence str) {
        return toLabel(str, "black", false, true);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with the specified web color. The specific
     * formatting applied to the input string will depend on the implementation of this method, allowing for different visual
     * representations of the text while ensuring that it is styled with the specified web color.
     * 
     * @param str the character sequence to be converted into a format with the specified web color
     * @param webcolor the web color to be applied to the input character sequence (e.g., "blue", "red", "green")
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with the specified web color
     */
    public default String toLabel(CharSequence str, String webcolor) {
        return toLabel(str, webcolor, false, false);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with the specified web color and bold styling. The
     * specific formatting applied to the input string will depend on the implementation of this method, allowing for different
     * visual representations of the text while ensuring that it is styled with the specified web color and bold formatting.
     * 
     * @param str the character sequence to be converted into a format with the specified web color and bold styling
     * @param webcolor the web color to be applied to the input character sequence (e.g., "blue", "red", "green")
     * @param bold a boolean indicating whether to apply bold styling to the input character sequence
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with the specified web color
     *         and bold styling
     */
    public default String toLabel(CharSequence str, String webcolor, boolean bold) {
        return toLabel(str, webcolor, bold, false);
    }

    /**
     * Converts the given character sequence into a format suitable for rendering with the specified web color, bold styling, and
     * italic styling. The specific formatting applied to the input string will depend on the implementation of this method,
     * allowing for different visual representations of the text while ensuring that it is styled with the specified web color, bold
     * formatting, and italic formatting as needed.
     * 
     * @param str the character sequence to be converted into a format with the specified web color, bold styling, and italic
     *        styling
     * @param webcolor the web color to be applied to the input character sequence (e.g., "blue", "red", "green")
     * @param bold a boolean indicating whether to apply bold styling to the input character sequence
     * @param italic a boolean indicating whether to apply italic styling to the input character sequence
     * 
     * @return a string representing the input character sequence in a format suitable for rendering with the specified web color,
     *         bold styling, and italic styling
     */
    public String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic);

    /**
     * Converts the given progress information into a string representation suitable for rendering. The specific formatting applied
     * to the progress information will depend on the implementation of this method, allowing for different visual representations
     * of the progress while ensuring that it conveys the current progress status effectively.
     * 
     * @param i the current progress value
     * @param max the maximum progress value
     * @param msg an optional message to be included in the progress representation
     * 
     * @return a string representing the progress information in a format suitable for rendering
     */
    public default String progress(int i, int max, String msg) {
        return progress(100, i, max, msg);
    }

    /**
     * Converts the given progress information into a string representation suitable for rendering, with a specified width for the
     * progress bar. The specific formatting applied to the progress information will depend on the implementation of this method,
     * allowing for different visual representations of the progress while ensuring that it conveys the current progress status
     * effectively within the specified width.
     * 
     * @param width the width of the progress bar representation
     * @param i the current progress value
     * @param max the maximum progress value
     * @param msg an optional message to be included in the progress representation
     * 
     * @return a string representing the progress information in a format suitable for rendering, with a specified width for the
     *         progress bar
     */
    public String progress(int width, int i, int max, String msg);

    /**
     * Converts the given character sequence into an escaped string representation suitable for rendering. The specific escaping
     * applied to the input string will depend on the implementation of this method, allowing for different visual representations
     * of the text while ensuring that any special characters are properly escaped for safe rendering.
     * 
     * @param str the character sequence to be converted into an escaped string representation
     * 
     * @return a string representing the input character sequence in an escaped format suitable for rendering
     */
    public String escape(CharSequence str);

    /**
     * Retrieves the current document representation of the status. The specific format and content of the document will depend on
     * the implementation of this method, allowing for different visual representations of the status information in a document
     * format suitable for rendering.
     * 
     * @return a string representing the current document representation of the status
     */
    public default String getDocument() {
        return toString();
    }

    /**
     * Checks whether the current status has progress information available. The specific criteria for determining whether progress
     * information is present will depend on the implementation of this method, allowing for different ways to indicate the presence
     * of progress information in the status representation.
     * 
     * @return true if progress information is available in the current status, false otherwise
     */
    public boolean hasProgress();
}
