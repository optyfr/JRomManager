package jrm.server.shared.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.eclipsesource.json.JsonObject;

import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link ReportActions}.
 */
@DisplayName("ReportActions")
class ReportActionsTest {

    
    private ActionsMgr mgr;
    private final List<String> sentMessages = new ArrayList<>();
    private Report report;
    private jrm.aui.profile.report.ReportTreeHandler<Report> handler;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        WebSession webSession;
        webSession = mock(WebSession.class);
        report = mock(Report.class);
        handler = mock(jrm.aui.profile.report.ReportTreeHandler.class);
        when(webSession.getReport()).thenReturn(report);
        when(webSession.getTmpReport()).thenReturn(report);
        when(report.getHandler()).thenReturn(handler);
        when(handler.getFilterOptions()).thenReturn(EnumSet.noneOf(FilterOptions.class));
        lenient().doNothing().when(handler).filter(any(FilterOptions[].class));
        sentMessages.clear();
        mgr = mock(ActionsMgr.class);
        when(mgr.getSession()).thenReturn(webSession);
        when(mgr.isOpen()).thenReturn(true);
        try {
            doAnswer(inv -> {
                sentMessages.add(inv.getArgument(0));
                return null;
            }).when(mgr).send(anyString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    void tearDown() {
        // no static state to reset when using mocks
    }

    @Nested
    @DisplayName("setFilter (full report)")
    class SetFilterFullTest {
        @Test
        @DisplayName("adds SHOWOK and sends Report.applyFilters with full filter state")
        void setFilterAddsShowOk() {
            final JsonObject params = new JsonObject();
            params.add("SHOWOK", true);
            final JsonObject jso = new JsonObject();
            jso.add("cmd", "Report.setFilter");
            jso.add("params", params);

            new ReportActions(mgr).setFilter(jso, false);

            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Report.applyFilters");
            assertThat(sentMessages.get(0)).contains("SHOWOK");
            verify(handler).filter(FilterOptions.SHOWOK);
        }

        @Test
        @DisplayName("removes HIDEMISSING and sends Report.applyFilters")
        void setFilterRemovesHideMissing() {
            when(handler.getFilterOptions()).thenReturn(EnumSet.of(FilterOptions.HIDEMISSING));
            final JsonObject params = new JsonObject();
            params.add("HIDEMISSING", false);
            final JsonObject jso = new JsonObject();
            jso.add("cmd", "Report.setFilter");
            jso.add("params", params);

            new ReportActions(mgr).setFilter(jso, false);

            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Report.applyFilters");
        }

        @Test
        @DisplayName("unknown filter option is silently ignored")
        void setFilterUnknownOption() {
            final JsonObject params = new JsonObject();
            params.add("UNKNOWN_OPTION", true);
            final JsonObject jso = new JsonObject();
            jso.add("cmd", "Report.setFilter");
            jso.add("params", params);

            new ReportActions(mgr).setFilter(jso, false);

            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Report.applyFilters");
        }
    }

    @Nested
    @DisplayName("setFilter (lite report)")
    class SetFilterLiteTest {
        @Test
        @DisplayName("sends ReportLite.applyFilters for lite mode")
        void setFilterLite() {
            final JsonObject params = new JsonObject();
            params.add("SHOWOK", true);
            final JsonObject jso = new JsonObject();
            jso.add("cmd", "ReportLite.setFilter");
            jso.add("params", params);

            new ReportActions(mgr).setFilter(jso, true);

            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("ReportLite.applyFilters");
        }
    }

    @Test
    @DisplayName("does not send when session is not open")
    void setFilterNotOpen() {
        when(mgr.isOpen()).thenReturn(false);
        final JsonObject params = new JsonObject();
        params.add("SHOWOK", true);
        final JsonObject jso = new JsonObject();
        jso.add("cmd", "Report.setFilter");
        jso.add("params", params);

        new ReportActions(mgr).setFilter(jso, false);

        assertThat(sentMessages).isEmpty();
    }
}