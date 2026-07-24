package jrm.fx.ui.progress;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DurationFormatUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.OffsetProvider;
import lombok.Data;

/**
 * Abstract base class for JavaFX tasks that display progress in a modal dialog.
 * <p>
 * Extends {@link Task} and implements {@link ProgressHandler} to provide a unified
 * progress reporting mechanism. Tracks errors, supports cancellation, and maintains
 * a {@link PData} snapshot for the UI.
 *
 * @param <V> the result type of the task
 * @since 2.5
 */
public abstract class ProgressTask<V> extends Task<V> implements ProgressHandler {
    /** The time format pattern. */
    private static final String HH_MM_SS = "HH:mm:ss";
    /** The format pattern for current/total values. */
    private static final String S_OF_S = "%s / %s";
    /** The default time display used when no progress has been made. */
    private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";

    /** The progress dialog. */
    private final Progress progress;

    /** Collected error messages. */
    private final List<String> errors = new ArrayList<>();

    /** Whether the task has been cancelled. */
    private boolean cancel = false;
    /** Whether the task can be cancelled. */
    private boolean canCancel = true;

    /**
     * Progress data snapshot for the UI.
     */
    static final @Data class PData {
        /**
         * Progress bar state.
         */
        static final @Data class PB {
            /**
             * Whether the progress bar is visible.
             * @param visibility whether the progress bar is visible
             * @return whether the progress bar is visible
             */
            boolean visibility = false;
            /**
             * Whether the progress bar shows a string.
             * @param stringPainted whether the progress bar shows a string
             * @return whether the progress bar shows a string
             */
            boolean stringPainted = false;
            /**
             * Whether the progress bar is indeterminate.
             * @param indeterminate whether the progress bar is indeterminate
             * @return whether the progress bar is indeterminate
             */
            boolean indeterminate = false;
            /**
             * The maximum value.
             * @param max the maximum value
             * @return the maximum value
             */
            int max = 100;
            /**
             * The current value.
             * @param val the current value
             * @return the current value
             */
            int val = 0;
            /**
             * The percentage.
             * @param perc the percentage
             * @return the percentage
             */
            double perc = 0;
            /**
             * The progress message.
             * @param msg the progress message
             * @return the progress message
             */
            String msg = null;
            /**
             * The time remaining.
             * @param timeleft the time remaining
             * @return the time remaining
             */
            String timeleft;

            /**
             * The time at which the current progress tracking began.
             * @param startTime the time at which the current progress tracking began
             * @return the time at which the current progress tracking began
             */
            transient long startTime = System.currentTimeMillis(); // NOSONAR

            PB() {

            }

            /**
             * Copy constructor.
             *
             * @param pb the progress bar data to copy from
             */
            PB(PB pb) {
                visibility = pb.visibility;
                stringPainted = pb.stringPainted;
                indeterminate = pb.indeterminate;
                max = pb.max;
                val = pb.val;
                perc = pb.perc;
                msg = pb.msg;
                timeleft = pb.timeleft;
            }
        }

        /**
         * Current thread count.
         * @param threadCnt the thread count
         * @return the thread count
         */
        int threadCnt = 1;

        /**
         * Whether to show multiple sub-info panels.
         * @param multipleSubInfos whether to show multiple sub-info panels
         * @return whether to show multiple sub-info panels
         */
        Boolean multipleSubInfos = false;

        /**
         * The info messages.
         * @param infos the info messages
         * @return the info messages
         */
        String[] infos = { null };
        /**
         * The sub-info messages.
         * @param subinfos the sub-info messages
         * @return the sub-info messages
         */
        String[] subinfos = { null };

        /**
         * The primary progress bar state.
         * @return the primary progress bar state
         */
        final PB pb1;
        /**
         * The secondary progress bar state.
         * @return the secondary progress bar state
         */
        final PB pb2;
        /**
         * The tertiary progress bar state.
         * @return the tertiary progress bar state
         */
        final PB pb3;

        PData() {
            pb1 = new PB();
            pb2 = new PB();
            pb3 = new PB();
        }

        /**
         * Copy constructor.
         *
         * @param data the progress data snapshot to copy from
         */
        PData(PData data) {
            threadCnt = data.threadCnt;
            multipleSubInfos = data.multipleSubInfos;
            infos = Arrays.copyOf(data.infos, data.infos.length);
            subinfos = Arrays.copyOf(data.subinfos, data.subinfos.length);
            pb1 = new PB(data.pb1);
            pb2 = new PB(data.pb2);
            pb3 = new PB(data.pb3);
        }
    }

    /** The progress data snapshot. */
    private final PData data = new PData();

    /**
     * Constructs a progress task and shows the progress dialog.
     *
     * @param owner the owner stage for the dialog
     * @throws IOException        if the FXML cannot be loaded
     * @throws URISyntaxException if the FXML resource URI is invalid
     */
    protected ProgressTask(Stage owner) throws IOException, URISyntaxException {
        super();
        progress = new Progress(owner, this);
    }

    /**
     * Initializes the info arrays for the given thread count and sub-info mode.
     *
     * @param threadCnt        the number of concurrent threads
     * @param multipleSubInfos whether multiple sub-info panels are shown
     */
    @Override
    public synchronized void setInfos(int threadCnt, Boolean multipleSubInfos) {
        this.data.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
        this.data.multipleSubInfos = multipleSubInfos;
        this.data.infos = new String[this.data.threadCnt];
        if (multipleSubInfos == null)
            this.data.subinfos = new String[0];
        else
            this.data.subinfos = new String[multipleSubInfos.booleanValue() ? this.data.threadCnt : 1];
        Platform.runLater(() -> progress.getController().setInfos(data.threadCnt, data.multipleSubInfos));
    }

    /**
     * Extends the info arrays to accommodate the specified thread count.
     *
     * @param threadCnt the new thread count
     */
    private synchronized void extendInfos(int threadCnt) {
        this.data.threadCnt = threadCnt;
        this.data.infos = Arrays.copyOf(this.data.infos, this.data.threadCnt);
        if (Boolean.TRUE.equals(data.multipleSubInfos))
            this.data.subinfos = Arrays.copyOf(this.data.subinfos, this.data.threadCnt);
        Platform.runLater(() -> progress.getController().extendInfos(data.threadCnt, data.multipleSubInfos));
    }

    /**
     * Clears all info entries and secondary progress bar message.
     */
    @Override
    public void clearInfos() {
        for (var i = 0; i < data.infos.length; i++)
            data.infos[i] = null;
        for (var i = 0; i < data.subinfos.length; i++)
            data.subinfos[i] = null;
        data.pb2.msg = null;
        Platform.runLater(() -> progress.getController().clearInfos());
    }

    /**
     * Clears info entries for freed thread offsets.
     */
    private synchronized void cleanup() {
        if (offsetProvider != null) {
            for (final var offset : offsetProvider.freeOffsets()) {
                if (offset < data.infos.length) {
                    data.infos[offset] = "";
                    if (data.infos.length == data.subinfos.length)
                        data.subinfos[offset] = "";
                }
            }
        }
    }

    /**
     * Returns the current thread offset, extending info arrays if needed.
     *
     * @return the thread offset, or {@code 0} if no offset provider is set
     */
    private synchronized int getOffset() {
        if (offsetProvider != null) {
            int offset = offsetProvider.getOffset();
            if (offset < 0)
                return 0;
            if (offset >= data.threadCnt)
                extendInfos(offset + 1);
            return offset;
        }
        return 0;
    }

    /**
     * Updates the primary progress bar with a message, current value, maximum, and sub-message.
     *
     * @param msg     the progress message, or {@code null} to leave unchanged
     * @param val     the current value, or {@code null} to leave unchanged
     * @param max     the maximum value, or {@code null} to leave unchanged
     * @param submsg  the sub-message, or {@code null} to leave unchanged
     */
    @Override
    public void setProgress(String msg, Integer val, Integer max, String submsg) {
        int offset = getOffset();
        if (msg != null)
            data.infos[offset] = msg;
        var force = false;
        if (val != null) {
            if (val < 0 && data.pb1.visibility) {
                data.pb1.visibility = false;
            } else if (val >= 0 && !data.pb1.visibility) {
                data.pb1.visibility = true;
            }
            data.pb1.stringPainted = val != 0;
            data.pb1.indeterminate = val == 0;
            force = computeProgress(data.pb1, val, max, force);
            showDuration(data.pb1, val);
        }
        if (submsg != null || (val != null && val == -1)) {
            if (data.subinfos.length == 1)
                data.subinfos[0] = submsg;
            else if (data.subinfos.length > 1)
                data.subinfos[offset] = submsg;
        }
        sendSetProgress(1, force);
    }

    /** Timestamp of the last UI update event. */
    private long lastEvent = 0;
    /** The last progress data snapshot sent to the UI. */
    private PData lastPData = null;

    /**
     * Sends a progress update to the UI if enough time has passed or the state has changed.
     *
     * @param pb    the progress bar index (1, 2, or 3)
     * @param force whether to force the update regardless of throttling
     */
    private synchronized void sendSetProgress(final int pb, final boolean force) {
        if (pb == 1)
            cleanup();
        final PData.PB pbObj = switch (pb) {
            case 1 -> data.pb1;
            case 2 -> data.pb2;
            case 3 -> data.pb3;
            default -> null;
        };
        final boolean doit = force
            || (pbObj != null && pbObj.visibility && !pbObj.indeterminate && pbObj.val > 0 && pbObj.max == pbObj.val)
            || (lastPData == null || (lastPData.infos.length == 1 && lastPData.infos[0] != null && !lastPData.infos[0].equals(this.data.infos[0])))
            || (System.currentTimeMillis() - lastEvent > 100)
            || (!data.pb1.visibility && !data.pb2.visibility && !data.pb3.visibility && !options.contains(Option.LAZY));
        if (doit) {
            lastPData = new PData(this.data);
            Platform.runLater(() -> progress.getController().setFullProgress(lastPData));
            lastEvent = System.currentTimeMillis();
        }
        data.pb1.msg = null;
    }

    /**
     * Computes progress based on current and maximum values.
     *
     * @param pb    the progress bar data to update
     * @param val   the current value, or {@code null} to leave unchanged
     * @param max   the maximum value, or {@code null} to leave unchanged
     * @param force the current force-refresh flag
     * @return {@code true} if the UI should be force-refreshed
     */
    private boolean computeProgress(final PData.PB pb, final Integer val, final Integer max, boolean force) {
        if (max != null)
            pb.max = max;
        if (val >= 0)
            pb.val = val;
        if (val == 0)
            pb.startTime = System.currentTimeMillis();
        if (pb.val >= 0 && pb.max > 0) {
            final var perc = pb.val * 100.0f / pb.max;
            force = (int) pb.perc != (int) perc;
            pb.perc = perc;
        }
        return force;
    }

    /**
     * Updates the duration display for the progress bar.
     *
     * @param pb  the progress bar data to update
     * @param val the current progress value
     */
    private void showDuration(PData.PB pb, int val) {
        if (val > 0) {
            if (pb.msg == null)
                pb.msg = String.format("%.02f%%", pb.perc);
            final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - pb.startTime) * (pb.max - val) / val, HH_MM_SS); // $NON-NLS-1$
            final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - pb.startTime) * pb.max / val, HH_MM_SS); // $NON-NLS-1$
            pb.timeleft = String.format(S_OF_S, left, total); // $NON-NLS-1$
        } else
            pb.timeleft = HH_MM_SS_OF_HH_MM_SS_NONE; // $NON-NLS-1$
    }

    /**
     * Updates the secondary progress bar with a message, current value, and maximum.
     *
     * @param msg the progress message, or {@code null} to clear
     * @param val the current value, or {@code null} to leave unchanged
     * @param max the maximum value, or {@code null} to leave unchanged
     */
    @Override
    public void setProgress2(String msg, Integer val, Integer max) {
        var force = false;
        if (msg != null && val != null) {
            if (!data.pb2.visibility)
                data.pb2.visibility = true;
            data.pb2.stringPainted = true/* msg != null || val > 0 */;
            data.pb2.msg = msg;
            data.pb2.indeterminate = val == 0 && msg == null;
            force = computeProgress(data.pb2, val, max, force);
            showDuration(data.pb2, val);
        } else if (data.pb2.visibility)
            data.pb2.visibility = false;
        sendSetProgress(2, force);
    }

    /**
     * Updates the tertiary progress bar with a message, current value, and maximum.
     *
     * @param msg the progress message, or {@code null} to clear
     * @param val the current value, or {@code null} to leave unchanged
     * @param max the maximum value, or {@code null} to leave unchanged
     */
    @Override
    public void setProgress3(String msg, Integer val, Integer max) {
        var force = false;
        if (msg != null && val != null) {
            if (!data.pb3.visibility)
                data.pb3.visibility = true;
            data.pb3.stringPainted = true/* msg != null || val > 0 */;
            data.pb3.msg = msg;
            data.pb3.indeterminate = val == 0 && msg == null;
            force = computeProgress(data.pb3, val, max, force);
            showDuration(data.pb3, val);
        } else if (data.pb3.visibility)
            data.pb3.visibility = false;
        sendSetProgress(3, force);
    }

    /**
     * Returns the current value of the primary progress bar.
     *
     * @return the primary progress value
     */
    @Override
    public int getCurrent() {
        return data.pb1.val;
    }

    /**
     * Returns the current value of the secondary progress bar.
     *
     * @return the secondary progress value
     */
    @Override
    public int getCurrent2() {
        return data.pb2.val;
    }

    /**
     * Returns the current value of the tertiary progress bar.
     *
     * @return the tertiary progress value
     */
    @Override
    public int getCurrent3() {
        return data.pb3.val;
    }

    /**
     * Returns whether the task has been cancelled.
     *
     * @return {@code true} if cancelled
     */
    @Override
    public boolean isCancel() {
        return cancel;
    }

    /**
     * Marks the task as cancelled.
     */
    @Override
    public void doCancel() {
        cancel = true;
    }

    /**
     * Returns whether the task can be cancelled by the user.
     *
     * @return {@code true} if the task can be cancelled
     */
    public boolean canCancel() {
        return canCancel;
    }

    /**
     * Sets whether the task can be cancelled and updates the UI accordingly.
     *
     * @param canCancel whether the task can be cancelled
     */
    public void canCancel(boolean canCancel) {
        this.canCancel = canCancel;
        Platform.runLater(() -> progress.getController().canCancel(canCancel));
    }

    /**
     * Wraps the given input stream with progress tracking.
     *
     * @param in  the input stream to wrap
     * @param len the total length of the stream, or {@code null} if unknown
     * @return a {@link ProgressInputStream} wrapping the given stream
     */
    @Override
    public InputStream getInputStream(InputStream in, Integer len) {
        return new ProgressInputStream(in, len, this);
    }

    /**
     * Closes the progress dialog.
     */
    @Override
    public void close() {
        progress.close();
    }

    /**
     * Appends an error message to the error list.
     *
     * @param error the error message to add
     */
    @Override
    public void addError(String error) {
        errors.add(error);
    }

    /** The progress handler option flags. */
    private Set<Option> options = EnumSet.noneOf(Option.class);

    /**
     * Sets the progress handler option flags.
     *
     * @param first the first option
     * @param rest  additional options
     */
    @Override
    public void setOptions(Option first, Option... rest) {
        options = EnumSet.of(first, rest);

    }

    /** The offset provider for multi-threaded progress reporting. */
    private OffsetProvider offsetProvider = null;

    /**
     * Sets the offset provider for multi-threaded progress reporting.
     *
     * @param offsetProvider the offset provider to use
     */
    @Override
    public void setOffsetProvider(OffsetProvider offsetProvider) {
        this.offsetProvider = offsetProvider;
    }
}
