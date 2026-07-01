package jrm.io.chd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Integration tests for {@link CHDInfoReader} using real-world CHD fixture files
 * from various MAME systems (abc1600_hdd, abc800_hdd, archimedes_hdd, bbc_hdd, pet_hdd).
 *
 * <p>These tests validate the parser against actual production CHD files of varying sizes
 * and format versions, ensuring that header validation, version detection, and checksum
 * extraction work correctly on real-world data rather than synthetic fixtures.</p>
 *
 * <p>Tested CHD systems include:</p>
 * <ul>
 *   <li>ABC1600 hard disk images (micr1325a, necd5126a)</li>
 *   <li>ABC800 hard disk images (abc850, abc852, abc856)</li>
 *   <li>Acorn Archimedes hard disk images (ros311_apps)</li>
 *   <li>BBC Micro hard disk images (cf_b106e, cf_b131r, cf_b142, cf_t107, cf_v103, cf_v105)</li>
 *   <li>Commodore PET hard disk images (d9060, d9090, softbox)</li>
 * </ul>
 *
 * @author optyfr
 * @see CHDInfoReader
 * @see CHDInfoReaderTest
 */
@DisplayName("CHDInfoReader - Real-World CHD Fixtures")
class RealWorldCHDTest {

    /** Base path to the directory containing real-world CHD fixture files. */
    private static final String CHD_RESOURCE_BASE = "src/test/resources/chd/";

    /**
     * Tests verifying that all real-world CHD fixture files have valid headers
     * and contain extractable checksums (MD5 or SHA1).
     */
    @Nested
    @DisplayName("Basic Header Validation")
    class BasicValidationTests {

        /**
         * Verifies that real-world CHD files from various MAME systems have valid headers
         * with version and length greater than zero.
         */
        @ParameterizedTest
        @CsvSource({
            "abc1600_hdd/micr1325a/micr1325a.chd",
            "abc1600_hdd/necd5126a/necd5126a.chd",
            "abc800_hdd/abc850/ro202.chd",
            "abc800_hdd/abc852/basf6185.chd",
            "abc800_hdd/abc856/micr1325.chd",
            "archimedes_hdd/ros311_apps/riscos311_apps.chd",
            "pet_hdd/d9060/tm602s.chd",
            "pet_hdd/d9090/tm603s.chd",
            "pet_hdd/softbox/imi5012h.chd",
            "bbc_hdd/cf_b106e/bbc_cf1gb_b106en.chd",
            "bbc_hdd/cf_b131r/bbc_cf_b131r.chd",
            "bbc_hdd/cf_b142/bbc_cf_b142.chd",
            "bbc_hdd/cf_t107/bbc_cf1gb_t107.chd",
            "bbc_hdd/cf_v103/bbc_cf1gb_v103.chd",
            "bbc_hdd/cf_v105/bbc_cf1gb_v105.chd"
        })
        @DisplayName("Should parse real-world CHD files with valid headers")
        void shouldParseRealWorldCHDFiles(String relativePath) throws IOException {
            // Arrange
            File chdFile = new File(CHD_RESOURCE_BASE + relativePath);
            assertThat(chdFile).exists();

            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);

            // Assert
            assertThat(reader.isValidTag())
                .as("CHD file %s should have valid tag", relativePath)
                .isTrue();
            assertThat(reader.getVersion())
                .as("CHD file %s should have version > 0", relativePath)
                .isGreaterThan(0);
            assertThat(reader.getLen())
                .as("CHD file %s should have header length > 0", relativePath)
                .isGreaterThan(0);
        }

        /**
         * Verifies that real-world CHD files contain at least one valid checksum (MD5 or SHA1)
         * with the correct length and format.
         */
        @ParameterizedTest
        @CsvSource({
            "abc1600_hdd/micr1325a/micr1325a.chd",
            "abc1600_hdd/necd5126a/necd5126a.chd",
            "abc800_hdd/abc850/ro202.chd",
            "abc800_hdd/abc852/basf6185.chd",
            "abc800_hdd/abc856/micr1325.chd",
            "archimedes_hdd/ros311_apps/riscos311_apps.chd",
            "pet_hdd/d9060/tm602s.chd",
            "pet_hdd/d9090/tm603s.chd",
            "pet_hdd/softbox/imi5012h.chd",
            "bbc_hdd/cf_b106e/bbc_cf1gb_b106en.chd",
            "bbc_hdd/cf_b131r/bbc_cf_b131r.chd",
            "bbc_hdd/cf_b142/bbc_cf_b142.chd",
            "bbc_hdd/cf_t107/bbc_cf1gb_t107.chd",
            "bbc_hdd/cf_v103/bbc_cf1gb_v103.chd",
            "bbc_hdd/cf_v105/bbc_cf1gb_v105.chd"
        })
        @DisplayName("Should extract at least one checksum (MD5 or SHA1) from real-world CHD files")
        void shouldExtractChecksums(String relativePath) throws IOException {
            // Arrange
            File chdFile = new File(CHD_RESOURCE_BASE + relativePath);

            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);

            // Assert
            String md5 = reader.getMD5();
            String sha1 = reader.getSHA1();

            assertThat(md5 != null || sha1 != null)
                .as("CHD file %s should have at least one checksum", relativePath)
                .isTrue();

            if (md5 != null) {
                assertThat(md5)
                    .as("MD5 for %s should be 32 hex chars", relativePath)
                    .hasSize(32)
                    .matches("[0-9a-f]{32}");
            }

            if (sha1 != null) {
                assertThat(sha1)
                    .as("SHA1 for %s should be 40 hex chars", relativePath)
                    .hasSize(40)
                    .matches("[0-9a-f]{40}");
            }
        }
    }

    /**
     * Tests validating parsing of specific CHD files with known characteristics,
     * including small, medium, and large files from different MAME systems.
     */
    @Nested
    @DisplayName("Specific CHD File Tests")
    class SpecificFileTests {

        /**
         * Verifies that a small CHD file (312 bytes) from the abc1600_hdd system is parsed correctly.
         */
        @Test
        @DisplayName("Should parse small abc1600_hdd CHD file (micr1325a.chd, 312 bytes)")
        void shouldParseSmallCHDFile() throws IOException {
            File chdFile = new File(CHD_RESOURCE_BASE + "abc1600_hdd/micr1325a/micr1325a.chd");

            CHDInfoReader reader = new CHDInfoReader(chdFile);

            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isBetween(1, 5);
            assertThat(reader.getLen()).isGreaterThan(0);
            long fileSize = chdFile.length();
            assertThat(fileSize).isEqualTo(312);
        }

        /**
         * Verifies that a medium-sized CHD file (~580KB) from the Archimedes system is parsed correctly.
         */
        @Test
        @DisplayName("Should parse medium-sized archimedes CHD file (riscos311_apps.chd, ~580KB)")
        void shouldParseMediumCHDFile() throws IOException {
            File chdFile = new File(CHD_RESOURCE_BASE + "archimedes_hdd/ros311_apps/riscos311_apps.chd");

            CHDInfoReader reader = new CHDInfoReader(chdFile);

            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isBetween(1, 5);
            assertThat(reader.getLen()).isGreaterThan(0);
            assertThat(chdFile.length()).isGreaterThan(500000);
        }

        /**
         * Verifies that a large CHD file (~10MB) from the BBC Micro system is parsed correctly.
         */
        @Test
        @DisplayName("Should parse large BBC Micro CHD file (bbc_cf1gb_b106en.chd, ~10MB)")
        void shouldParseLargeCHDFile() throws IOException {
            File chdFile = new File(CHD_RESOURCE_BASE + "bbc_hdd/cf_b106e/bbc_cf1gb_b106en.chd");

            CHDInfoReader reader = new CHDInfoReader(chdFile);

            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isBetween(1, 5);
            assertThat(reader.getLen()).isGreaterThan(0);
            assertThat(chdFile.length()).isGreaterThan(9000000);
        }

        /**
         * Verifies that CHD files from Commodore PET hard disk systems are parsed correctly.
         */
        @Test
        @DisplayName("Should parse PET HDD CHD files from Commodore PET systems")
        void shouldParsePETHDDFiles() throws IOException {
            File[] petFiles = {
                new File(CHD_RESOURCE_BASE + "pet_hdd/d9060/tm602s.chd"),
                new File(CHD_RESOURCE_BASE + "pet_hdd/d9090/tm603s.chd"),
                new File(CHD_RESOURCE_BASE + "pet_hdd/softbox/imi5012h.chd")
            };

            for (File chdFile : petFiles) {
                CHDInfoReader reader = new CHDInfoReader(chdFile);
                assertThat(reader.isValidTag())
                    .as("PET CHD %s should be valid", chdFile.getName())
                    .isTrue();
                assertThat(reader.getVersion())
                    .as("PET CHD %s version", chdFile.getName())
                    .isBetween(1, 5);
            }
        }
    }

    /**
     * Tests verifying that {@link CHDInfoReader} correctly detects CHD format versions
     * across different real-world fixtures and extracts the appropriate checksum types
     * for each version.
     */
    @Nested
    @DisplayName("Version Detection Across Fixtures")
    class VersionDetectionTests {

        /**
         * Verifies that CHD version detection works correctly across different real-world fixtures
         * and extracts the appropriate checksum types for each version.
         */
        @ParameterizedTest
        @CsvSource({
            "abc1600_hdd/micr1325a/micr1325a.chd",
            "abc800_hdd/abc850/ro202.chd",
            "bbc_hdd/cf_b106e/bbc_cf1gb_b106en.chd"
        })
        @DisplayName("Should detect version and extract appropriate checksums")
        void shouldDetectVersionAndExtractChecksums(String relativePath) throws IOException {
            File chdFile = new File(CHD_RESOURCE_BASE + relativePath);
            CHDInfoReader reader = new CHDInfoReader(chdFile);

            int version = reader.getVersion();
            assertThat(version).isBetween(1, 5);

            // Version-specific checksum expectations
            switch (version) {
                case 1, 2:
                    // V1 and V2 have MD5 only
                    assertThat(reader.getMD5()).isNotNull();
                    assertThat(reader.getSHA1()).isNull();
                    break;
                case 3, 4:
                    // V3 and V4 have both MD5 and SHA1
                    assertThat(reader.getMD5()).isNotNull();
                    assertThat(reader.getSHA1()).isNotNull();
                    break;
                case 5:
                    // V5 has SHA1 only
                    assertThat(reader.getSHA1()).isNotNull();
                    assertThat(reader.getMD5()).isNull();
                    break;
                default:
                    throw new IllegalStateException("Unexpected CHD version: " + version);
            }
        }
    }
}
