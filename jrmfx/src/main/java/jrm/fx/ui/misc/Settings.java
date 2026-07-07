package jrm.fx.ui.misc;

import com.google.gson.Gson;

import javafx.stage.Stage;
import jrm.misc.Log;

/**
 * Utility class for serializing and deserializing window state.
 * <p>
 * Converts {@link WindowState} to/from JSON for persisting window position, size,
 * and state (maximized, full-screen, iconified) across sessions.
 *
 * @since 2.5
 */
public class Settings {
    /**
     * Private constructor to prevent instantiation.
     */
    private Settings() {
        // Do not instantiate
    }

    /** The Gson instance. */
    private static final Gson gson = new Gson();

    /**
     * Serializes the window state to JSON.
     *
     * @param window the window
     * @return the JSON string
     */
    public static String toJson(Stage window) {
        return gson.toJson(WindowState.getInstance(window));
    }

    /**
     * Restores the window state from JSON.
     *
     * @param json   the JSON string
     * @param window the window to restore
     */
    public static void fromJson(String json, Stage window) {
        if (json == null || json.isEmpty())
            return;
        try {
            gson.fromJson(json, WindowState.class).restore(window);
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }
    }
}
