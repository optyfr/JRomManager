package jrm.profile.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link NPlayers}, the parser for {@code nplayers.ini} files that
 * maps game codes to their multiplayer mode categories (e.g., "2 Players", "4 Players").
 *
 * @author optyfr
 * @see NPlayers
 * @see IniProcessorTest
 */
@DisplayName("NPlayers - nplayers.ini Parser")
class NPlayersTest {

    /** Temporary directory for synthetic INI test files, automatically cleaned up after each test. */
    @TempDir
    Path tempDir;

    /**
     * Creates a temporary INI file with the specified lines of content.
     *
     * @param filename the name of the INI file to create
     * @param lines    the lines to write to the file
     * @return the created INI file
     * @throws IOException if the file cannot be written
     */
    private File writeIni(String filename, String... lines) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        Files.write(file.toPath(), List.of(lines));
        return file;
    }

    /**
     * Searches for an NPlayer by name in a NPlayers instance.
     *
     * @param np   the NPlayers instance to search
     * @param name the player mode name to find
     * @return an Optional containing the NPlayer if found, or empty if not found
     */
    private Optional<NPlayer> findByName(NPlayers np, String name) {
        for (NPlayer p : np) {
            if (p.name.equals(name)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    /**
     * Tests verifying correct parsing of valid nplayers.ini data including multiple player modes,
     * alphabetical sorting, source file storage, section names, iteration, whitespace trimming,
     * toString output, and parent references.
     */
    @Nested
    @DisplayName("Happy Path - Parsing Valid Data")
    class HappyPathTests {

        /**
         * Verifies that multiple player modes are parsed correctly and games are mapped
         * to their respective modes.
         */
        @Test
        @DisplayName("Should parse multiple player modes with games mapped correctly")
        void shouldParseMultiplePlayerModes() throws IOException {
            File ini = writeIni("nplayers.ini",
                "[NPlayers]",
                "pacman=1 Player",
                "gauntlet=4 Players",
                "sf2=2 Players",
                "bublbobl=2 Players",
                "tetris=2 Players Simultaneous"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers()).hasSize(4);
            assertThat(findByName(np, "1 Player")).isPresent()
                .get().satisfies(p -> assertThat(p).hasSize(1).contains("pacman"));
            assertThat(findByName(np, "2 Players")).isPresent()
                .get().satisfies(p -> assertThat(p).hasSize(2).contains("sf2", "bublbobl"));
            assertThat(findByName(np, "4 Players")).isPresent()
                .get().satisfies(p -> assertThat(p).hasSize(1).contains("gauntlet"));
            assertThat(findByName(np, "2 Players Simultaneous")).isPresent()
                .get().satisfies(p -> assertThat(p).hasSize(1).contains("tetris"));
        }

        /**
         * Verifies that NPlayers returns player modes sorted alphabetically by name.
         */
        @Test
        @DisplayName("Should return NPlayers sorted alphabetically by mode name")
        void shouldSortByModeName() throws IOException {
            File ini = writeIni("sorted.ini",
                "[NPlayers]",
                "z=2 Players",
                "a=4 Players",
                "b=1 Player"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers().stream().map(p -> p.name))
                .containsExactly("1 Player", "2 Players", "4 Players");
        }

        /**
         * Verifies that NPlayers stores the source INI file reference.
         */
        @Test
        @DisplayName("Should store the source file reference")
        void shouldStoreSourceFile() throws IOException {
            File ini = writeIni("source.ini",
                "[NPlayers]",
                "game=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.file).isEqualTo(ini);
        }

        /**
         * Verifies that NPlayers reports the correct INI section name.
         */
        @Test
        @DisplayName("Should report correct INI section name")
        void shouldReportCorrectSection() throws IOException {
            File ini = writeIni("section.ini",
                "[NPlayers]",
                "game=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getSection()).isEqualTo("[NPlayers]");
        }

        /**
         * Verifies that NPlayers supports iteration over all player modes.
         */
        @Test
        @DisplayName("Should iterate over all player modes")
        void shouldIterateOverAllModes() throws IOException {
            File ini = writeIni("iter.ini",
                "[NPlayers]",
                "g1=1 Player",
                "g2=2 Players",
                "g3=4 Players"
            );

            NPlayers np = NPlayers.read(ini);
            int count = 0;
            for (@SuppressWarnings("unused") NPlayer p : np) {
                count++;
            }

            assertThat(count).isEqualTo(3);
        }

        /**
         * Verifies that whitespace around keys and values is trimmed correctly.
         */
        @Test
        @DisplayName("Should trim whitespace around keys and values")
        void shouldTrimWhitespace() throws IOException {
            File ini = writeIni("trim.ini",
                "[NPlayers]",
                "  pacman  =  1 Player  "
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(findByName(np, "1 Player")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("pacman"));
        }

        /**
         * Verifies that NPlayer produces a toString with the game count per mode.
         */
        @Test
        @DisplayName("Should produce correct toString with game count per mode")
        void shouldProduceCorrectToString() throws IOException {
            File ini = writeIni("tostring.ini",
                "[NPlayers]",
                "g1=2 Players",
                "g2=2 Players",
                "g3=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            Optional<NPlayer> twoP = findByName(np, "2 Players");
            assertThat(twoP).isPresent();
            assertThat(twoP.get()).hasToString("2 Players (3)");
        }

        /**
         * Verifies that NPlayer produces the correct property name for filtering.
         */
        @Test
        @DisplayName("Should produce correct property name for NPlayer")
        void shouldProduceCorrectPropertyName() throws IOException {
            File ini = writeIni("prop.ini",
                "[NPlayers]",
                "g=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            Optional<NPlayer> twoP = findByName(np, "2 Players");
            assertThat(twoP).isPresent();
            assertThat(twoP.get().getPropertyName()).isEqualTo("filter.nplayer.2 Players");
        }
    }

    /**
     * Tests verifying that NPlayers raises appropriate IOExceptions when given missing files,
     * files without the required section, empty sections, or blank files.
     */
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        /**
         * Verifies that IOException is thrown when the INI file is missing.
         */
        @Test
        @DisplayName("Should throw IOException when file is missing")
        void shouldThrowWhenFileMissing() {
            File missing = tempDir.resolve("missing.ini").toFile();

            assertThatThrownBy(() -> NPlayers.read(missing))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the required [NPlayers] section is missing.
         */
        @Test
        @DisplayName("Should throw IOException when no [NPlayers] section exists")
        void shouldThrowWhenNoSection() throws IOException {
            File ini = writeIni("nosection.ini",
                "[Other]",
                "pacman=1 Player"
            );

            assertThatThrownBy(() -> NPlayers.read(ini))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the [NPlayers] section is empty.
         */
        @Test
        @DisplayName("Should throw IOException when section is empty")
        void shouldThrowWhenSectionEmpty() throws IOException {
            File ini = writeIni("empty.ini",
                "[NPlayers]",
                "[Other]",
                "game=value"
            );

            assertThatThrownBy(() -> NPlayers.read(ini))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the file is empty.
         */
        @Test
        @DisplayName("Should throw IOException on empty file")
        void shouldThrowOnEmptyFile() throws IOException {
            File ini = writeIni("blank.ini");

            assertThatThrownBy(() -> NPlayers.read(ini))
                .isInstanceOf(IOException.class);
        }
    }

    /**
     * Tests verifying NPlayers behavior with edge cases including other INI sections,
     * comment lines, many player modes, and case-insensitive section headers.
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        /**
         * Verifies that lines without an equals sign are skipped within the section.
         */
        @Test
        @DisplayName("Should skip lines without equals sign within section")
        void shouldSkipInvalidLines() throws IOException {
            File ini = writeIni("skip.ini",
                "[NPlayers]",
                "pacman=1 Player",
                "this line has no equals",
                "galaga=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers()).hasSize(2);
            assertThat(findByName(np, "1 Player")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("pacman"));
            assertThat(findByName(np, "2 Players")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("galaga"));
        }

        /**
         * Verifies that entries in other sections are ignored.
         */
        @Test
        @DisplayName("Should ignore entries in other sections")
        void shouldIgnoreOtherSections() throws IOException {
            File ini = writeIni("multi.ini",
                "[NPlayers]",
                "pacman=1 Player",
                "[Category]",
                "ignored=2 Players",
                "[Other]",
                "alsoignored=4 Players"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers()).hasSize(1);
            assertThat(findByName(np, "1 Player")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("pacman"));
        }

        /**
         * Verifies that the section header is matched case-insensitively.
         */
        @Test
        @DisplayName("Should handle case-insensitive section header")
        void shouldHandleCaseInsensitiveSection() throws IOException {
            File ini = writeIni("case.ini",
                "[nplayers]",
                "game=1 Player"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers()).hasSize(1);
        }

        /**
         * Verifies that games with the same player mode are grouped into a single NPlayer.
         */
        @Test
        @DisplayName("Should group same-mode games into a single NPlayer")
        void shouldGroupSameModeGames() throws IOException {
            File ini = writeIni("group.ini",
                "[NPlayers]",
                "g1=2 Players",
                "g2=2 Players",
                "g3=2 Players",
                "g4=2 Players"
            );

            NPlayers np = NPlayers.read(ini);

            assertThat(np.getListNPlayers()).hasSize(1);
            Optional<NPlayer> twoP = findByName(np, "2 Players");
            assertThat(twoP).isPresent()
                .get().satisfies(p -> assertThat(p)
                    .hasSize(4)
                    .containsExactlyInAnyOrder("g1", "g2", "g3", "g4"));
        }
    }

    /**
     * Tests using the real-world MAME 0.288 nplayers.ini fixture file, verifying parsing
     * of production data with many player modes, well-known games, and complex mode structures.
     */
    @Nested
    @DisplayName("Real-World Fixture - MAME 0.288 nplayers.ini")
    class RealWorldFixtureTests {

        /**
         * Returns the real-world MAME 0.288 nplayers.ini fixture file.
         *
         * @return the nplayers.ini file from test resources
         */
        private File getRealNPlayersIni() {
            return Path.of("src/test/resources/ini/nplayers.ini").toFile();
        }

        /**
         * Verifies that the real MAME 0.288 nplayers.ini is parsed with many player modes.
         */
        @Test
        @DisplayName("Should parse real nplayers.ini with many player modes")
        void shouldParseRealNPlayersIni() throws IOException {
            File ini = getRealNPlayersIni();

            assertThat(ini).exists();

            NPlayers np = NPlayers.read(ini);

            assertThat(np).isNotNull();
            assertThat(np.getListNPlayers()).isNotEmpty();
            assertThat(np.getListNPlayers()).hasSizeGreaterThan(10);
        }

        /**
         * Verifies that well-known player modes are present in the real data.
         */
        @Test
        @DisplayName("Should contain well-known player modes from real data")
        void shouldContainWellKnownPlayerModes() throws IOException {
            NPlayers np = NPlayers.read(getRealNPlayersIni());

            assertThat(findByName(np, "1P")).isPresent();
            assertThat(findByName(np, "2P sim")).isPresent();
            assertThat(findByName(np, "2P alt")).isPresent();
            assertThat(findByName(np, "Device")).isPresent();
            assertThat(findByName(np, "Non-arcade")).isPresent();
        }

        /**
         * Verifies that known games are mapped to correct player modes from real data.
         */
        @Test
        @DisplayName("Should map known games to correct player modes from real data")
        void shouldMapKnownGamesToCorrectPlayerModes() throws IOException {
            NPlayers np = NPlayers.read(getRealNPlayersIni());

            // 18w = 1P
            assertThat(findByName(np, "1P")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("18w"));

            // 1941 = 2P sim
            assertThat(findByName(np, "2P sim")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("1941"));

            // 10yard = 2P alt
            assertThat(findByName(np, "2P alt")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("10yard"));

            // 110dance = Non-arcade
            assertThat(findByName(np, "Non-arcade")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("110dance"));
        }

        /**
         * Verifies that all player modes have non-empty game lists in the real data.
         */
        @Test
        @DisplayName("Should have non-empty game lists in all player modes from real data")
        void shouldHaveNonEmptyGameLists() throws IOException {
            NPlayers np = NPlayers.read(getRealNPlayersIni());

            for (NPlayer p : np) {
                assertThat(p).as("NPlayer '%s' should have games", p.name)
                    .isNotEmpty();
            }
        }

        /**
         * Verifies that all player modes can be iterated from real data without errors.
         */
        @Test
        @DisplayName("Should iterate all player modes from real data without errors")
        void shouldIterateAllPlayerModesFromRealData() throws IOException {
            NPlayers np = NPlayers.read(getRealNPlayersIni());

            int totalModes = 0;
            int totalGames = 0;

            for (NPlayer p : np) {
                totalModes++;
                totalGames += p.size();
            }

            assertThat(totalModes).isGreaterThan(0);
            assertThat(totalGames).isGreaterThan(totalModes);
        }

        /**
         * Verifies that special player mode values (e.g., "???", "Device") are handled correctly.
         */
        @Test
        @DisplayName("Should handle special player mode values from real data")
        void shouldHandleSpecialPlayerModeValues() throws IOException {
            NPlayers np = NPlayers.read(getRealNPlayersIni());

            // Real data includes "???" for unknown player modes
            assertThat(findByName(np, "???")).isPresent();

            // Real data includes "Device" for non-game hardware
            assertThat(findByName(np, "Device")).isPresent()
                .get().satisfies(p -> assertThat(p).contains("09825_67907"));
        }
    }
}
