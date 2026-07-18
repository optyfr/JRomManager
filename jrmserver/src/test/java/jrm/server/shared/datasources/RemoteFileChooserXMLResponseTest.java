package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link RemoteFileChooserXMLResponse.Options} context-based configuration.
 */
@DisplayName("RemoteFileChooserXMLResponse.Options")
class RemoteFileChooserXMLResponseTest {

    @Nested
    @DisplayName("directory-only contexts")
    class DirectoryContextsTest {
        @Test
        @DisplayName("tfRomsDest is directory-only with no path matcher")
        void tfRomsDest() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("tfRomsDest");
            assertThat(opts.isDir).isTrue();
            assertThat(opts.pathmatcher).isNull();
        }

        @Test
        @DisplayName("listSrcDir is directory-only")
        void listSrcDir() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("listSrcDir");
            assertThat(opts.isDir).isTrue();
            assertThat(opts.pathmatcher).isNull();
        }
        
        @ParameterizedTest(name = "{0} is directory-only")
        @ValueSource(strings = {"tfSrcDir", "updDat", "updTrnt"})
        void directoryOnlyContexts(String context) {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options(context);
            assertThat(opts.isDir).isTrue();
        }
    }

    @Nested
    @DisplayName("torrent file context")
    class TorrentContextTest {
        @Test
        @DisplayName("addTrnt matches glob:*.torrent and is not directory")
        void addTrnt() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("addTrnt");
            assertThat(opts.isDir).isFalse();
            assertThat(opts.pathmatcher).isEqualTo("glob:*.torrent");
        }
    }

    @Nested
    @DisplayName("DAT file contexts")
    class DatContextsTest {
        private static final String GLOB_XML_DAT = "glob:*.{xml,dat}";

        @Test
        @DisplayName("importDat matches glob:*.{xml,dat}")
        void importDat() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("importDat");
            assertThat(opts.isDir).isFalse();
            assertThat(opts.pathmatcher).isEqualTo(GLOB_XML_DAT);
        }

        @Test
        @DisplayName("addDat matches glob:*.{xml,dat}")
        void addDat() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("addDat");
            assertThat(opts.pathmatcher).isEqualTo(GLOB_XML_DAT);
        }

        @Test
        @DisplayName("tfDstDat matches glob:*.{xml,dat}")
        void tfDstDat() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("tfDstDat");
            assertThat(opts.pathmatcher).isEqualTo(GLOB_XML_DAT);
        }
    }

    @Nested
    @DisplayName("archive context")
    class ArchiveContextTest {
        @Test
        @DisplayName("addArc matches archive extensions")
        void addArc() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("addArc");
            assertThat(opts.isDir).isFalse();
            assertThat(opts.pathmatcher)
                .contains("zip")
                .contains("7z")
                .contains("rar");
        }
    }

    @Nested
    @DisplayName("settings contexts")
    class SettingsContextTest {
        @Test
        @DisplayName("importSettings matches glob:*.properties")
        void importSettings() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("importSettings");
            assertThat(opts.isDir).isFalse();
            assertThat(opts.pathmatcher).isEqualTo("glob:*.properties");
        }

        @Test
        @DisplayName("exportSettings matches glob:*.properties")
        void exportSettings() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("exportSettings");
            assertThat(opts.pathmatcher).isEqualTo("glob:*.properties");
        }
    }

    @Nested
    @DisplayName("default context")
    class DefaultContextTest {
        @Test
        @DisplayName("unknown context defaults to no filter, not directory")
        void unknownContext() {
            final RemoteFileChooserXMLResponse.Options opts = new RemoteFileChooserXMLResponse.Options("unknown");
            assertThat(opts.isDir).isFalse();
            assertThat(opts.pathmatcher).isNull();
        }
    }
}