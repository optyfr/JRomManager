package jrm.server.shared.actions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import jrm.misc.Log;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

/**
 * Action handler for managing report filter operations in the ROM manager web server.
 * <p>
 * This class processes incoming JSON action commands related to applying filter options to scan reports. It supports two report
 * modes:
 * </p>
 * <ul>
 * <li><b>Full report</b> ({@code Report.applyFilters}) - Filters applied to the session's primary report</li>
 * <li><b>Lite report</b> ({@code ReportLite.applyFilters}) - Filters applied to the session's temporary report</li>
 * </ul>
 * <p>
 * Filter options are defined in the {@link FilterOptions} enumeration and control which ROM entries are visible in the report
 * output (e.g., missing ROMs, unneeded files, fixable entries).
 * </p>
 * <p>
 * <b>Thread Safety:</b> This class is not thread-safe. All operations should be performed on the WebSocket message handling thread.
 * The underlying {@link ActionsMgr} and {@link Report} instances are shared across the session and should not be accessed
 * concurrently from multiple threads.
 * </p>
 */
public class ReportActions {

    /**
     * The {@link ActionsMgr} instance used for managing session interactions and WebSocket communications.
     */
    private final ActionsMgr ws;

    /**
     * Constructs a new {@code ReportActions} instance with the specified actions manager.
     *
     * @param ws the {@link ActionsMgr} instance to use for managing session interactions and communications, must not be null
     */
    public ReportActions(ActionsMgr ws) {
        this.ws = ws;
    }

    /**
     * Applies filter options to the appropriate report and sends the updated filter state back to the client.
     * <p>
     * This method processes a JSON object containing filter option settings. Each key in the "params" object corresponds to a
     * {@link FilterOptions} enum value, and the boolean value determines whether that filter should be enabled ({@code true}) or
     * disabled ({@code false}). The method:
     * </p>
     * <ol>
     * <li>Clones the current filter options from the report handler</li>
     * <li>Adds or removes options based on the JSON parameters</li>
     * <li>Applies the updated filter set to the report handler</li>
     * <li>Sends a WebSocket message with the complete filter state back to the client</li>
     * </ol>
     * <p>
     * Unknown filter option names are silently ignored to maintain forward compatibility.
     * </p>
     *
     * @param jso the JSON object containing filter parameters, expected to have a structure like:
     * 
     *        <pre>
     *             <code class='language-json'>
     *             {
     *                 "params": {
     *                     "MISSING": true,
     *                     "UNNEEDED": false,
     *                     "FIXABLE": true
     *                 }
     *             }
     *             </code>
     *        </pre>
     * 
     * @param lite if {@code true}, applies filters to the temporary report ({@link jrm.server.shared.WebSession#getTmpReport()});
     *        if {@code false}, applies filters to the primary report ({@link jrm.server.shared.WebSession#getReport()})
     */
    public void setFilter(JsonObject jso, boolean lite) {
        final JsonObject pjso = jso.get("params").asObject();
        final Report report = lite ? ws.getSession().getTmpReport() : ws.getSession().getReport();
        Set<FilterOptions> options = ((EnumSet<FilterOptions>) report.getHandler().getFilterOptions()).clone();
        for (Member m : pjso) {
            try {
                final var option = FilterOptions.valueOf(m.getName());
                final var value = m.getValue();
                if (value.asBoolean())
                    options.add(option);
                else
                    options.remove(option);

            } catch (IllegalArgumentException ex) {
                // is it even possible?
            }
        }
        report.getHandler().filter(options.toArray(new FilterOptions[0]));
        try {
            if (ws.isOpen()) {
                final var params = new JsonObject();
                EnumSet.allOf(FilterOptions.class).forEach(f -> params.add(f.toString(), options.contains(f)));
                ws.send(Json.object().add("cmd", lite ? "ReportLite.applyFilters" : "Report.applyFilters").add("params", params).toString());
            }
        } catch (IOException e) {
            Log.err(e.getMessage(), e);
        }
    }
}
