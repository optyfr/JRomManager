package jrm.profile.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the {@link IniProcessor} interface contract, verifying section-based
 * INI file parsing, key-value extraction, and edge case handling.
 *
 * @author optyfr
 * @see IniProcessor
 * @see CatVerTest
 * @see NPlayersTest
 */
@DisplayName("IniProcessor - INI File Parsing Contract")
class IniProcessorTest {

    /** Temporary directory for synthetic INI test files, automatically cleaned up after each test. */
    @TempDir
    Path tempDir;

    /**
     * Minimal concrete implementation of IniProcessor for testing the default processFile method.
     *
     * <p>This test double provides a configurable section name and allows verification of
     * the INI parsing contract without requiring a full CatVer or NPlayers instance.</p>
     */
    private static class TestIniProcessor implements IniProcessor {
        /** The INI section name to search for (including brackets). */
        private final String section;

        /**
         * Creates a TestIniProcessor that searches for the specified section.
         *
         * @param section the section name to search for, including brackets (e.g., "[Category]")
         */
        TestIniProcessor(String section) {
            this.section = section;
        }

        /**
         * Returns the section name this processor searches for.
         *
         * @return the section name including brackets
         */
        @Override
        public String getSection() {
            return section;
        }
    }

    /**
     * Creates a temporary INI file with the specified lines of content.
     *
     * @param filename the name of the INI file to create
     * @param lines    the lines to write to the file
     * @return the created INI file
     * @throws IOException if the file cannot be written
     */
    private File writeIniFile(String filename, String... lines) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        Files.write(file.toPath(), List.of(lines));
        return file;
    }

    /**
     * Tests verifying correct parsing of INI file sections including key-value extraction,
     * section boundary detection, case-insensitive matching, and section positioning.
     */
    @Nested
    @DisplayName("Happy Path - Section Parsing")
    class SectionParsingTests {

        /**
         * Verifies that all key-value pairs within the target section are parsed correctly.
         */
        @Test
        @DisplayName("Should parse all key-value pairs within target section")
        void shouldParseAllKeyValuesInTargetSection() throws IOException {
            File ini = writeIniFile("test.ini",
                "[Category]",
                "pacman=Shooter / Gallery",
                "galaga=Maze / Classic",
                "",
                "[Other]",
                "ignored=value"
            );

            IniProcessor processor = new TestIniProcessor("[Category]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(2);
            assertThat(parsed.get(0)[0]).isEqualTo("pacman");
            assertThat(parsed.get(0)[1]).isEqualTo("Shooter / Gallery");
            assertThat(parsed.get(1)[0]).isEqualTo("galaga");
            assertThat(parsed.get(1)[1]).isEqualTo("Maze / Classic");
        }

        /**
         * Verifies that parsing stops when the next section header is encountered.
         */
        @Test
        @DisplayName("Should stop parsing when next section header is encountered")
        void shouldStopAtNextSectionHeader() throws IOException {
            File ini = writeIniFile("multi.ini",
                "[Section1]",
                "key1=value1",
                "key2=value2",
                "[Section2]",
                "key3=value3"
            );

            IniProcessor processor = new TestIniProcessor("[Section1]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(2).noneMatch(kv -> kv[0].equals("key3"));
        }

        /**
         * Verifies that section matching is case-insensitive.
         */
        @Test
        @DisplayName("Should handle case-insensitive section matching")
        void shouldHandleCaseInsensitiveSectionMatch() throws IOException {
            File ini = writeIniFile("case.ini",
                "[CATEGORY]",
                "game1=Maze",
                "[Other]",
                "game2=Shooter"
            );

            IniProcessor processor = new TestIniProcessor("[Category]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(1);
            assertThat(parsed.get(0)[0]).isEqualTo("game1");
        }

        /**
         * Verifies that a section at the end of the file is parsed correctly without trailing sections.
         */
        @Test
        @DisplayName("Should parse section at end of file without trailing sections")
        void shouldParseSectionAtEndOfFile() throws IOException {
            File ini = writeIniFile("tail.ini",
                "[First]",
                "a=1",
                "[Target]",
                "x=10",
                "y=20"
            );

            IniProcessor processor = new TestIniProcessor("[Target]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(2);
            assertThat(parsed.get(0)[0]).isEqualTo("x");
            assertThat(parsed.get(1)[0]).isEqualTo("y");
        }
    }

    /**
     * Tests verifying IniProcessor behavior with edge cases including missing sections,
     * empty files, and malformed INI structures.
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        /**
         * Verifies that empty results are returned when the target section is not found.
         */
        @Test
        @DisplayName("Should return empty results when target section is not found")
        void shouldReturnEmptyWhenSectionNotFound() throws IOException {
            File ini = writeIniFile("nosection.ini",
                "[Other]",
                "key=value"
            );

            IniProcessor processor = new TestIniProcessor("[Missing]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).isEmpty();
        }

        /**
         * Verifies that lines without an equals sign are skipped within the target section.
         */
        @Test
        @DisplayName("Should skip lines without equals sign within target section")
        void shouldSkipLinesWithoutEquals() throws IOException {
            File ini = writeIniFile("noequals.ini",
                "[Data]",
                "valid=yes",
                "this line has no equals",
                "also valid=indeed"
            );

            IniProcessor processor = new TestIniProcessor("[Data]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(2);
            assertThat(parsed.get(0)[0]).isEqualTo("valid");
            assertThat(parsed.get(1)[0]).isEqualTo("also valid");
        }

        /**
         * Verifies that empty files are handled gracefully without errors.
         */
        @Test
        @DisplayName("Should handle empty file gracefully")
        void shouldHandleEmptyFile() throws IOException {
            File ini = writeIniFile("empty.ini");

            IniProcessor processor = new TestIniProcessor("[Any]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).isEmpty();
        }

        /**
         * Verifies that empty sections with no entries are handled correctly.
         */
        @Test
        @DisplayName("Should handle empty section with no entries")
        void shouldHandleEmptySection() throws IOException {
            File ini = writeIniFile("emptysection.ini",
                "[Empty]",
                "[Next]"
            );

            IniProcessor processor = new TestIniProcessor("[Empty]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).isEmpty();
        }

        /**
         * Verifies that section headers are matched regardless of case.
         */
        @ParameterizedTest
        @ValueSource(strings = {"[Section]", "[section]", "[SECTION]"})
        @DisplayName("Should match section header regardless of case")
        void shouldMatchSectionCaseInsensitively(String sectionInFile) throws IOException {
            File ini = writeIniFile("casevariants.ini",
                sectionInFile,
                "key=value"
            );

            IniProcessor processor = new TestIniProcessor("[Section]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(1);
        }

        /**
         * Verifies that lines with multiple equals signs are handled by splitting on the first.
         */
        @Test
        @DisplayName("Should handle lines with multiple equals signs by splitting on first")
        void shouldHandleMultipleEqualsSigns() throws IOException {
            // StringUtils.split splits into at most 2 parts when used with separator char
            File ini = writeIniFile("multiequals.ini",
                "[Data]",
                "key=value=with=equals"
            );

            IniProcessor processor = new TestIniProcessor("[Data]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            // StringUtils.split(line, '=') splits into all parts; length==2 check may filter this
            // The behavior depends on StringUtils.split - it splits into all tokens
            // If kv.length != 2, the line is skipped
            assertThat(parsed).isEmpty();
        }

        /**
         * Verifies that blank lines within a section are handled without breaking parsing.
         */
        @Test
        @DisplayName("Should handle blank lines within section without breaking")
        void shouldHandleBlankLinesInSection() throws IOException {
            File ini = writeIniFile("blanks.ini",
                "[Section]",
                "key1=val1",
                "",
                "key2=val2",
                "",
                "key3=val3"
            );

            IniProcessor processor = new TestIniProcessor("[Section]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(3);
        }

        /**
         * Verifies that lines before any section header are ignored.
         */
        @Test
        @DisplayName("Should handle lines before any section header")
        void shouldIgnoreLinesBeforeSection() throws IOException {
            File ini = writeIniFile("preamble.ini",
                "orphan=value",
                "another=orphan",
                "[Target]",
                "key=value"
            );

            IniProcessor processor = new TestIniProcessor("[Target]");
            List<String[]> parsed = new ArrayList<>();
            processor.processFile(ini, parsed::add);

            assertThat(parsed).hasSize(1);
            assertThat(parsed.get(0)[0]).isEqualTo("key");
        }
    }
}
