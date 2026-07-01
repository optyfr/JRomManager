package jrm.profile.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.profile.Profile;
import jrm.security.Session;

/**
 * Unit tests for {@link CatVer}, the parser for {@code catver.ini} files that
 * maps game codes to hierarchical category/subcategory pairs (e.g., "Shooter / Flying Vertical").
 * 
 * <p>Tests include both synthetic INI fixtures and real-world MAME 0.288 catver.ini data.</p>
 *
 * @author optyfr
 * @see CatVer
 * @see IniProcessorTest
 */
@DisplayName("CatVer - catver.ini Parser")
class CatVerTest {

    /** Temporary directory for synthetic INI test files, automatically cleaned up after each test. */
    @TempDir
    Path tempDir;

    /** Mocked profile object providing session context for CatVer parsing. */
    private Profile profile;
    /** Mocked session object providing message bundles for localized output. */
    private Session session;

    /**
     * Initializes mocked dependencies before each test.
     *
     * <p>The session mock returns a real {@link ResourceBundle} for message localization.
     * The profile mock returns the session when {@code getSession()} is called.</p>
     */
    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        ResourceBundle msgs = ResourceBundle.getBundle("jrm.resources.Messages");
        when(session.getMsgs()).thenReturn(msgs);

        profile = mock(Profile.class);
        when(profile.getSession()).thenReturn(session);
    }

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
     * Searches for a category by name in a CatVer instance.
     *
     * @param catver the CatVer instance to search
     * @param name   the category name to find
     * @return an Optional containing the category if found, or empty if not found
     */
    private Optional<CatVer.Category> findCategory(CatVer catver, String name) {
        for (CatVer.Category cat : catver) {
            if (cat.name.equals(name)) {
                return Optional.of(cat);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches for a subcategory by parent category name and subcategory name.
     *
     * @param catver     the CatVer instance to search
     * @param catName    the parent category name
     * @param subcatName the subcategory name to find
     * @return an Optional containing the subcategory if found, or empty if not found
     */
    private Optional<CatVer.Category.SubCategory> findSubCategory(CatVer catver, String catName, String subcatName) {
        return findCategory(catver, catName)
            .map(cat -> cat.get(subcatName));
    }

    /**
     * Tests verifying correct parsing of valid catver.ini data including multiple categories,
     * alphabetical sorting, source file storage, section names, iteration, whitespace trimming,
     * game grouping, property names, toString output, and parent references.
     */
    @Nested
    @DisplayName("Happy Path - Parsing Valid Data")
    class HappyPathTests {

        /**
         * Verifies that multiple categories with subcategories and games are parsed correctly.
         */
        @Test
        @DisplayName("Should parse multiple categories with subcategories and games")
        void shouldParseMultipleCategories() throws IOException {
            File ini = writeIni("catver.ini",
                "[Category]",
                "pacman=Maze / Classic",
                "galaga=Shooter / Gallery",
                "1942=Shooter / Flying Vertical",
                "tbowl=Sports / Football",
                "sf2=Fighter / Versus"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(4);
            assertThat(findCategory(catver, "Maze")).isPresent()
                .get().satisfies(cat -> {
                    assertThat(cat.name).isEqualTo("Maze");
                    assertThat((Map<String, ?>) cat).containsKey("Classic");
                    assertThat(cat.get("Classic")).contains("pacman");
                });
            assertThat(findCategory(catver, "Shooter")).isPresent()
                .get().satisfies(cat -> {
                    assertThat((Map<?, ?>) cat).hasSize(2);
                    assertThat(cat.get("Gallery")).contains("galaga");
                    assertThat(cat.get("Flying Vertical")).contains("1942");
                });
        }

        /**
         * Verifies that categories are returned sorted alphabetically by name.
         */
        @Test
        @DisplayName("Should return categories sorted alphabetically by name")
        void shouldSortCategoriesByName() throws IOException {
            File ini = writeIni("sorted.ini",
                "[Category]",
                "z=Zebra / Type",
                "a=Alpha / Type",
                "b=Beta / Type"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories().stream().map(c -> c.name))
                .containsExactly("Alpha", "Beta", "Zebra");
        }

        /**
         * Verifies that CatVer stores the source INI file reference.
         */
        @Test
        @DisplayName("Should store the source file reference")
        void shouldStoreSourceFile() throws IOException {
            File ini = writeIni("source.ini",
                "[Category]",
                "game=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.file).isEqualTo(ini);
        }

        /**
         * Verifies that CatVer reports the correct INI section name.
         */
        @Test
        @DisplayName("Should report correct INI section name")
        void shouldReportCorrectSection() throws IOException {
            File ini = writeIni("section.ini",
                "[Category]",
                "game=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getSection()).isEqualTo("[Category]");
        }

        /**
         * Verifies that CatVer supports iteration over all categories.
         */
        @Test
        @DisplayName("Should iterate over all categories")
        void shouldIterateOverAllCategories() throws IOException {
            File ini = writeIni("iter.ini",
                "[Category]",
                "g1=Maze / Classic",
                "g2=Shooter / Gallery",
                "g3=Sports / Football"
            );

            CatVer catver = CatVer.read(profile, ini);
            int count = 0;
            for (@SuppressWarnings("unused") CatVer.Category cat : catver) {
                count++;
            }

            assertThat(count).isEqualTo(3);
        }

        /**
         * Verifies that whitespace around keys and category values is trimmed correctly.
         */
        @Test
        @DisplayName("Should trim whitespace around keys and category values")
        void shouldTrimWhitespace() throws IOException {
            File ini = writeIni("trim.ini",
                "[Category]",
                "  pacman  =  Maze / Classic  "
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(findSubCategory(catver, "Maze", "Classic")).isPresent()
                .get().satisfies(sub -> assertThat(sub).contains("pacman"));
        }

        /**
         * Verifies that games with the same category/subcategory are grouped correctly.
         */
        @Test
        @DisplayName("Should group games with same category/subcategory correctly")
        void shouldGroupSameCategoryGames() throws IOException {
            File ini = writeIni("group.ini",
                "[Category]",
                "1941=Shooter / Flying Vertical",
                "1942=Shooter / Flying Vertical",
                "1943=Shooter / Flying Vertical",
                "1944=Shooter / Flying Vertical"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(1);
            assertThat(findSubCategory(catver, "Shooter", "Flying Vertical")).isPresent()
                .get().satisfies(sub -> assertThat(sub)
                    .hasSize(4)
                    .containsExactlyInAnyOrder("1941", "1942", "1943", "1944"));
        }

        /**
         * Verifies that Category produces the correct property name for filtering.
         */
        @Test
        @DisplayName("Should produce correct property name for Category")
        void shouldProduceCorrectPropertyNameForCategory() throws IOException {
            File ini = writeIni("prop.ini",
                "[Category]",
                "game=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getPropertyName()).isEqualTo("filter.cat");
            Optional<CatVer.Category> shooter = findCategory(catver, "Shooter");
            assertThat(shooter).isPresent();
            assertThat(shooter.get().getPropertyName()).isEqualTo("filter.cat.Shooter");
        }

        /**
         * Verifies that SubCategory produces the correct property name for filtering.
         */
        @Test
        @DisplayName("Should produce correct property name for SubCategory")
        void shouldProduceCorrectPropertyNameForSubCategory() throws IOException {
            File ini = writeIni("subprop.ini",
                "[Category]",
                "game=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(findSubCategory(catver, "Shooter", "Gallery")).isPresent()
                .get().satisfies(sub -> assertThat(sub.getPropertyName()).isEqualTo("filter.cat.Shooter.Gallery"));
        }

        /**
         * Verifies that SubCategory produces a toString with the game count.
         */
        @Test
        @DisplayName("Should provide correct toString for SubCategory with game count")
        void shouldProduceCorrectToStringForSubCategory() throws IOException {
            File ini = writeIni("tostring.ini",
                "[Category]",
                "g1=Shooter / Gallery",
                "g2=Shooter / Gallery",
                "g3=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(findSubCategory(catver, "Shooter", "Gallery")).isPresent()
                .get().satisfies(sub -> assertThat(sub).hasToString("Gallery (3)"));
        }

        /**
         * Verifies that SubCategory references its parent Category.
         */
        @Test
        @DisplayName("SubCategory should reference its parent Category")
        void subCategoryShouldReferenceParent() throws IOException {
            File ini = writeIni("parent.ini",
                "[Category]",
                "game=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(findSubCategory(catver, "Shooter", "Gallery")).isPresent()
                .get().satisfies(sub -> {
                    assertThat((Object) sub.getParent()).isNotNull();
                    assertThat(sub.getParent().name).isEqualTo("Shooter");
                });
        }
    }

    /**
     * Tests using the real-world MAME 0.288 catver.ini fixture file, verifying parsing
     * of production data with many categories, well-known games, and complex subcategory structures.
     */
    @Nested
    @DisplayName("Real-World Fixture - MAME 0.288 catver.ini")
    class RealWorldFixtureTests {

        /**
         * Returns the real-world MAME 0.288 catver.ini fixture file.
         *
         * @return the catver.ini file from test resources
         */
        private File getRealCatVerIni() {
            return Path.of("src/test/resources/ini/catver.ini").toFile();
        }

        /**
         * Verifies that the real MAME 0.288 catver.ini is parsed with many categories.
         */
        @Test
        @DisplayName("Should parse real catver.ini with many categories")
        void shouldParseRealCatVerIni() throws IOException {
            File ini = getRealCatVerIni();

            assertThat(ini).exists();

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver).isNotNull();
            assertThat(catver.getListCategories()).isNotEmpty();
            assertThat(catver.getListCategories()).hasSizeGreaterThan(10);
        }

        /**
         * Verifies that well-known MAME categories are present in the real data.
         */
        @Test
        @DisplayName("Should contain well-known MAME categories from real data")
        void shouldContainWellKnownCategories() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            assertThat(findCategory(catver, "Shooter")).isPresent();
            assertThat(findCategory(catver, "Maze")).isPresent();
            assertThat(findCategory(catver, "Sports")).isPresent();
            assertThat(findCategory(catver, "Driving")).isPresent();
            assertThat(findCategory(catver, "Platform")).isPresent();
        }

        /**
         * Verifies that known games are mapped to correct categories from real data.
         */
        @Test
        @DisplayName("Should map known games to correct categories from real data")
        void shouldMapKnownGamesToCorrectCategories() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            // 1941 = Shooter / Flying Vertical
            assertThat(findSubCategory(catver, "Shooter", "Flying Vertical")).isPresent()
                .get().satisfies(sub -> assertThat(sub).contains("1941"));

            // 10yard = Sports / Football
            assertThat(findSubCategory(catver, "Sports", "Football")).isPresent()
                .get().satisfies(sub -> assertThat(sub).contains("10yard"));

            // 005 = Maze / Shooter Small
            assertThat(findSubCategory(catver, "Maze", "Shooter Small")).isPresent()
                .get().satisfies(sub -> assertThat(sub).contains("005"));
        }

        /**
         * Verifies that subcategories are properly linked to parent categories in the real data.
         */
        @Test
        @DisplayName("Should have subcategories properly linked to parent categories in real data")
        void shouldHaveSubCategoriesLinkedToParents() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            for (CatVer.Category cat : catver) {
                assertThat(cat.getListSubCategories()).isNotEmpty();
                for (CatVer.Category.SubCategory sub : cat) {
                    assertThat((Object) sub.getParent()).isSameAs(cat);
                    assertThat(sub.name).isNotBlank();
                }
            }
        }

        /**
         * Verifies that all subcategories have non-empty game lists in the real data.
         */
        @Test
        @DisplayName("Should have non-empty game lists in all subcategories from real data")
        void shouldHaveNonEmptyGameLists() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            for (CatVer.Category cat : catver) {
                for (CatVer.Category.SubCategory sub : cat) {
                    assertThat(sub).as("SubCategory '%s/%s' should have games", cat.name, sub.name)
                        .isNotEmpty();
                }
            }
        }

        /**
         * Verifies that entries with System/Device category are parsed from the real data.
         */
        @Test
        @DisplayName("Should parse entries with System/Device category from real data")
        void shouldParseSystemDeviceCategory() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            assertThat(findCategory(catver, "System")).isPresent()
                .get().satisfies(cat -> {
                    assertThat((Map<String, ?>) cat).containsKey("Device");
                    assertThat(cat.get("Device")).contains("09825_67907");
                });
        }

        /**
         * Verifies that all categories can be iterated from real data without errors.
         */
        @Test
        @DisplayName("Should iterate all categories from real data without errors")
        void shouldIterateAllCategoriesFromRealData() throws IOException {
            CatVer catver = CatVer.read(profile, getRealCatVerIni());

            int totalCategories = 0;
            int totalSubCategories = 0;
            int totalGames = 0;

            for (CatVer.Category cat : catver) {
                totalCategories++;
                for (CatVer.Category.SubCategory sub : cat) {
                    totalSubCategories++;
                    totalGames += sub.size();
                }
            }

            assertThat(totalCategories).isGreaterThan(0);
            assertThat(totalSubCategories).isGreaterThan(totalCategories);
            assertThat(totalGames).isGreaterThan(totalSubCategories);
        }
    }

    /**
     * Tests verifying that CatVer raises appropriate IOExceptions when given missing files,
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

            assertThatThrownBy(() -> CatVer.read(profile, missing))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the required [Category] section is missing.
         */
        @Test
        @DisplayName("Should throw IOException when no [Category] section exists")
        void shouldThrowWhenNoSection() throws IOException {
            File ini = writeIni("nosection.ini",
                "[Other]",
                "pacman=Maze / Classic"
            );

            assertThatThrownBy(() -> CatVer.read(profile, ini))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the [Category] section is empty.
         */
        @Test
        @DisplayName("Should throw IOException when section is empty")
        void shouldThrowWhenSectionEmpty() throws IOException {
            File ini = writeIni("empty.ini",
                "[Category]",
                "[Other]",
                "game=value"
            );

            assertThatThrownBy(() -> CatVer.read(profile, ini))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that IOException is thrown when the file is empty.
         */
        @Test
        @DisplayName("Should throw IOException on empty file")
        void shouldThrowOnEmptyFile() throws IOException {
            File ini = writeIni("blank.ini");

            assertThatThrownBy(() -> CatVer.read(profile, ini))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that entries without a '/' separator (no subcategory) are skipped.
         */
        @Test
        @DisplayName("Should skip entries without '/' separator (no subcategory)")
        void shouldSkipEntriesWithoutSeparator() throws IOException {
            File ini = writeIni("noslash.ini",
                "[Category]",
                "game1=SingleCategory",
                "game2=Maze / Classic"
            );

            CatVer catver = CatVer.read(profile, ini);

            // Only game2 should be parsed since it has category/subcategory format
            assertThat(catver.getListCategories()).hasSize(1);
            assertThat(findCategory(catver, "Maze")).isPresent();
        }
    }

    /**
     * Tests verifying CatVer behavior with edge cases including other INI sections,
     * comment lines, many subcategories, and case-insensitive section headers.
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should ignore entries in other sections")
        void shouldIgnoreOtherSections() throws IOException {
            File ini = writeIni("multi.ini",
                "[FOLDER_SETTINGS]",
                "RootFolderIcon=mame",
                "[ROOT_FOLDER]",
                "[Category]",
                "pacman=Maze / Classic",
                "[Other]",
                "ignored=Shooter / Gallery"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(1);
            assertThat(findSubCategory(catver, "Maze", "Classic")).isPresent()
                .get().satisfies(sub -> assertThat(sub).contains("pacman"));
        }

        @Test
        @DisplayName("Should handle header-style comment lines before section")
        void shouldHandleCommentLinesBeforeSection() throws IOException {
            File ini = writeIni("comments.ini",
                "[FOLDER_SETTINGS]",
                "RootFolderIcon mame",
                "SubFolderIcon folder",
                "",
                ";; catver.ini 0.288 / 09-Jun-26 / MAME 0.288 ;;",
                "",
                "[ROOT_FOLDER]",
                "",
                "[Category]",
                "pacman=Maze / Classic"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(1);
            assertThat(findSubCategory(catver, "Maze", "Classic")).isPresent();
        }

        @Test
        @DisplayName("Should handle category with many subcategories")
        void shouldHandleManySubcategories() throws IOException {
            File ini = writeIni("manysubs.ini",
                "[Category]",
                "g1=Shooter / Gallery",
                "g2=Shooter / Flying Vertical",
                "g3=Shooter / Flying Horizontal",
                "g4=Shooter / Walking",
                "g5=Shooter / Driving",
                "g6=Shooter / Underwater"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(1);
            assertThat(findCategory(catver, "Shooter")).isPresent()
                .get().satisfies(cat -> assertThat((Map<?, ?>) cat).hasSize(6));
        }

        @Test
        @DisplayName("Should handle case-insensitive section header")
        void shouldHandleCaseInsensitiveSection() throws IOException {
            File ini = writeIni("case.ini",
                "[category]",
                "game=Maze / Classic"
            );

            CatVer catver = CatVer.read(profile, ini);

            assertThat(catver.getListCategories()).hasSize(1);
        }
    }
}
