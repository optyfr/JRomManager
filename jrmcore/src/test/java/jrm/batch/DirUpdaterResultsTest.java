package jrm.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.profile.report.Report;
import jrm.security.Session;

/**
 * Unit tests for {@link DirUpdaterResults}, covering construction, adding results, save/load round-trip,
 * and the {@link DirUpdaterResults.DirUpdaterResult} inner class.
 */
@DisplayName("DirUpdaterResults tests")
class DirUpdaterResultsTest {

    /** System property used by {@code GlobalSettings} to locate the work directory in server mode. */
    private static final String JRM_DIR_PROP = "jrommanager.dir";

    @TempDir
    Path tempDir;

    private Session session;

    @BeforeEach
    void setUp() throws IOException {
        System.setProperty(JRM_DIR_PROP, tempDir.toString());
        Files.createDirectories(tempDir.resolve("users").resolve("JRomManager"));
        session = new Session("dirupdater-results-test");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(JRM_DIR_PROP);
    }

    // ──────────────────────────────────────────────────────────────
    //  Construction
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("construction")
    class ConstructionTests {

        @Test
        @DisplayName("default constructor should initialize empty results list")
        void defaultConstructorShouldInitializeEmptyResultsList() {
            var results = new DirUpdaterResults();

            assertThat(results.getResults()).isEmpty();
            assertThat(results.getDat()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  add()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("add()")
    class AddTests {

        @Test
        @DisplayName("add should append result with dat and stats")
        void addShouldAppendResultWithDatAndStats() {
            var results = new DirUpdaterResults();
            var datFile = new File("test.dat");
            var stats = new Report.Stats();

            results.add(datFile, stats);

            assertThat(results.getResults()).hasSize(1);
            assertThat(results.getResults().get(0).getDat()).isEqualTo(datFile);
            assertThat(results.getResults().get(0).getStats()).isSameAs(stats);
        }

        @Test
        @DisplayName("add multiple results should preserve order")
        void addMultipleResultsShouldPreserveOrder() {
            var results = new DirUpdaterResults();

            results.add(new File("a.dat"), new Report.Stats());
            results.add(new File("b.dat"), new Report.Stats());
            results.add(new File("c.dat"), new Report.Stats());

            assertThat(results.getResults()).hasSize(3);
            assertThat(results.getResults().get(0).getDat()).isEqualTo(new File("a.dat"));
            assertThat(results.getResults().get(1).getDat()).isEqualTo(new File("b.dat"));
            assertThat(results.getResults().get(2).getDat()).isEqualTo(new File("c.dat"));
        }

        @Test
        @DisplayName("add with null stats should store null stats")
        void addWithNullStatsShouldStoreNullStats() {
            var results = new DirUpdaterResults();

            results.add(new File("test.dat"), null);

            assertThat(results.getResults()).hasSize(1);
            assertThat(results.getResults().get(0).getStats()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Setter / Getter for dat
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("dat property")
    class DatPropertyTests {

        @Test
        @DisplayName("setDat should update dat field")
        void setDatShouldUpdateDatField() {
            var results = new DirUpdaterResults();
            var datFile = new File("my.dat");

            results.setDat(datFile);

            assertThat(results.getDat()).isEqualTo(datFile);
        }

        @Test
        @DisplayName("setDat with null should clear dat field")
        void setDatWithNullShouldClearDatField() {
            var results = new DirUpdaterResults();
            results.setDat(new File("my.dat"));

            results.setDat(null);

            assertThat(results.getDat()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  DirUpdaterResult inner class
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DirUpdaterResult inner class")
    class DirUpdaterResultTests {

        @Test
        @DisplayName("default constructor should create instance with null fields")
        void defaultConstructorShouldCreateInstanceWithNullFields() {
            var results = new DirUpdaterResults();
            var result = results.new DirUpdaterResult();

            assertThat(result.getDat()).isNull();
            assertThat(result.getStats()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Save / Load
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save and load")
    class SaveLoadTests {

        @Test
        @DisplayName("save and load should preserve dat and results")
        void saveAndLoadShouldPreserveDatAndResults() {
            var results = new DirUpdaterResults();
            var datFile = new File("test.dat");
            results.setDat(datFile);
            var stats = new Report.Stats();
            results.add(datFile, stats);

            results.save(session);

            var loaded = DirUpdaterResults.load(session, datFile);

            assertThat(loaded).isNotNull();
            assertThat(loaded.getDat()).isEqualTo(datFile);
            assertThat(loaded.getResults()).hasSize(1);
            assertThat(loaded.getResults().get(0).getDat()).isEqualTo(datFile);
        }

        @Test
        @DisplayName("save and load with multiple results should preserve all entries")
        void saveAndLoadWithMultipleResultsShouldPreserveAllEntries() {
            var results = new DirUpdaterResults();
            var datFile = new File("multi.dat");
            results.setDat(datFile);
            results.add(new File("a.dat"), new Report.Stats());
            results.add(new File("b.dat"), new Report.Stats());

            results.save(session);

            var loaded = DirUpdaterResults.load(session, datFile);

            assertThat(loaded).isNotNull();
            assertThat(loaded.getResults()).hasSize(2);
            assertThat(loaded.getResults().get(0).getDat()).isEqualTo(new File("a.dat"));
            assertThat(loaded.getResults().get(1).getDat()).isEqualTo(new File("b.dat"));
        }

        @Test
        @DisplayName("load non-existent results should return null")
        void loadNonExistentResultsShouldReturnNull() {
            var result = DirUpdaterResults.load(session, new File("nonexistent.dat"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("load without progress should preserve results")
        void loadWithoutProgressShouldPreserveResults() {
            var results = new DirUpdaterResults();
            var datFile = new File("noprogress.dat");
            results.setDat(datFile);
            results.add(datFile, new Report.Stats());

            results.save(session);

            var loaded = DirUpdaterResults.load(session, datFile);

            assertThat(loaded).isNotNull();
            assertThat(loaded.getResults()).hasSize(1);
        }

        @Test
        @DisplayName("save should create results file in work directory")
        void saveShouldCreateResultsFileInWorkDirectory() {
            var results = new DirUpdaterResults();
            var datFile = new File("save-test.dat");
            results.setDat(datFile);

            results.save(session);

            var workDir = session.getUser().getSettings().getWorkPath().resolve("work").toFile();
            assertThat(workDir).exists();
            assertThat(workDir.listFiles()).anyMatch(File::isFile);
        }
    }
}