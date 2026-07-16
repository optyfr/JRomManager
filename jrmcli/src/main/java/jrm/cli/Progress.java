package jrm.cli;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.OffsetProvider;

/**
 * A class that implements the ProgressHandler interface to handle progress updates and errors.
 */
public class Progress implements ProgressHandler {
    /**
     * A list to store error messages encountered during processing.
     */
    private List<String> errors = new ArrayList<>();
    /**
     * The maximum value for progress tracking, initialized to null.
     */
    private Integer max = null;
    /**
     * A flag indicating whether the progress output should be quiet (not displayed).
     */
    private boolean quiet = false;

    /**
     * Constructs a Progress instance with the specified quiet mode.
     *
     * @param quiet A boolean indicating whether to suppress progress output.
     */
    @Override
    public void setInfos(int threadCnt, Boolean multipleSubInfos) {
        // not implemented
    }

    /**
     * Clears any stored information or state related to progress tracking.
     */
    @Override
    public void clearInfos() {
        // not implemented
    }

    /**
     * Sets the progress information, including a message, current value, maximum value, and a sub-message.
     *
     * @param msg    The main message to display.
     * @param val    The current progress value.
     * @param max    The maximum progress value.
     * @param submsg A sub-message to display alongside the main message.
     */
    @Override
    public void setProgress(String msg, Integer val, Integer max, String submsg) {
        if (max != null)
            this.max = max;
        if (msg != null && !msg.isEmpty() && !quiet) {
            if (val != null && val > 0)
                System.out.format("%s (%d/%d)%n", msg, val, this.max); // NOSONAR
            else
                System.out.format("%s%n", msg); // NOSONAR
        }
    }

    /**
     * Sets the progress information with a message, current value, and maximum value.
     *
     * @param msg The message to display.
     * @param val The current progress value.
     * @param max The maximum progress value.
     */
    @Override
    public void setProgress2(String msg, Integer val, Integer max) {
        // not implemented
    }

    /**
     * Sets the progress information with a message, current value, and maximum value for a third type of progress.
     *
     * @param msg The message to display.
     * @param val The current progress value.
     * @param max The maximum progress value.
     */
    @Override
    public void setProgress3(String msg, Integer val, Integer max) {
        // not implemented
    }

    /**
     * Retrieves the current progress value.
     *
     * @return The current progress value, or 0 if not set.
     */
    @Override
    public int getCurrent() {
        return 0;
    }

    /**
     * Retrieves the current progress value for the second type of progress.
     *
     * @return The current progress value for the second type, or 0 if not set.
     */
    @Override
    public int getCurrent2() {
        return 0;
    }

    /**
     * Retrieves the current progress value for the third type of progress.
     *
     * @return The current progress value for the third type, or 0 if not set.
     */
    @Override
    public int getCurrent3() {
        return 0;
    }

    /**
     * Retrieves the maximum progress value.
     *
     * @return The maximum progress value, or 0 if not set.
     */
    @Override
    public boolean isCancel() {
        return false;
    }

    /**
     * Retrieves the maximum progress value.
     *
     * @return The maximum progress value, or 0 if not set.
     */
    @Override
    public void doCancel() {
        // not implemented
    }

    /**
     * Retrieves the maximum progress value.
     *
     * @return The maximum progress value, or 0 if not set.
     */
    @Override
    public void canCancel(boolean canCancel) {
        // not implemented
    }

    /**
     * Retrieves the maximum progress value.
     *
     * @return The maximum progress value, or 0 if not set.
     */
    @Override
    public boolean canCancel() {
        return false;
    }

    /**
     * Retrieves the maximum progress value.
     *
     * @return The maximum progress value, or 0 if not set.
     */
    @Override
    public InputStream getInputStream(InputStream in, Integer len) {
        return in;
    }

    /**
     * Closes the progress handler and prints any accumulated error messages to the standard error stream.
     */
    @Override
    public void close() {
        errors.forEach(System.err::println); // NOSONAR
    }

    /**
     * Sets the quiet mode for progress output.
     *
     * @param quiet A boolean indicating whether to suppress progress output.
     */
    public void quiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Toggles the quiet mode for progress output.
     */
    public void quiet() {
        this.quiet = !this.quiet;
    }

    /**
     * Checks if the progress output is in quiet mode.
     *
     * @return true if quiet mode is enabled, false otherwise.
     */
    @Override
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Sets the options for the progress handler. This method is not implemented in this class.
     *
     * @param first The first option to set.
     * @param rest  Additional options to set.
     */
    @Override
    public void setOptions(Option first, Option... rest) {
        // Do nothing
    }

    /**
     * Sets the offset provider for the progress handler. This method is not implemented in this class.
     *
     * @param offset The offset provider to set.
     */
    @Override
    public void setOffsetProvider(OffsetProvider offset) {
        // Do nothing
    }

}
