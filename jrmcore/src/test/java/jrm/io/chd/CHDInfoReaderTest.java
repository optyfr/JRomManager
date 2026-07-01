package jrm.io.chd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CHDInfoReader} covering CHD header parsing across versions 1-5,
 * invalid file handling, and checksum extraction.
 *
 * <p>Tests use synthetic CHD files with controlled header data to verify parsing logic
 * for each CHD format version, including MD5 and SHA1 checksum extraction at the correct
 * byte offsets. Each test constructs a minimal CHD file in a temporary directory and
 * validates the reader's version detection, header length, and checksum output.</p>
 *
 * @author optyfr
 * @see CHDInfoReader
 * @see RealWorldCHDTest
 */
@DisplayName("CHDInfoReader - CHD Header Parser")
class CHDInfoReaderTest {

    /** Temporary directory for synthetic CHD test files, created before all tests and cleaned up afterwards. */
    private static Path tempDir;

    /**
     * Creates a temporary directory for synthetic CHD test files before all tests run.
     *
     * @throws IOException if the directory cannot be created
     */
    @BeforeAll
    static void setUp() throws IOException {
        tempDir = Path.of(System.getProperty("java.io.tmpdir"), "chd-test-" + System.currentTimeMillis());
        Files.createDirectories(tempDir);
    }

    /**
     * Removes the temporary directory and all synthetic CHD files after all tests complete.
     *
     * <p>Performs best-effort cleanup; failures are silently ignored on Windows
     * where memory-mapped files may prevent deletion.</p>
     */
    @AfterAll
    static void tearDown() {
        // Best-effort cleanup, ignore failures on Windows due to memory-mapped files
        try {
            if (Files.exists(tempDir)) {
                try (var stream = Files.walk(tempDir)) {
                    stream.sorted((a, b) -> b.compareTo(a))
                          .forEach(p -> {
                              try { Files.deleteIfExists(p); } catch (IOException _) { /* ignore */ }
                          });
                }
            }
        } catch (IOException _) {
            // Ignore cleanup failures on Windows
        }
    }

    /** The 8-byte MComprHD magic tag that identifies a valid CHD file header. */
    private static final byte[] TAG_MCOMPRHD = "MComprHD".getBytes();

    /**
     * Creates a synthetic CHD file with the specified version and header data.
     *
     * <p>The generated file contains the standard CHD tag ({@code MComprHD}), followed by
     * the header length, version number, and the supplied header data bytes.</p>
     *
     * @param filename     the filename to use within the temporary directory
     * @param version      the CHD format version (1-5)
     * @param headerLength the declared header length in bytes
     * @param headerData   the raw header bytes appended after the tag, length, and version fields
     * @return the created {@link File} representing the synthetic CHD
     * @throws IOException if the file cannot be written
     */
    private File createSyntheticCHD(String filename, int version, int headerLength, byte[] headerData) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            ByteBuffer bb = ByteBuffer.allocate(1024);
            bb.put(TAG_MCOMPRHD);
            bb.putInt(headerLength);
            bb.putInt(version);
            if (headerData != null) {
                bb.put(headerData);
            }
            raf.write(bb.array());
        }
        return file;
    }

    /**
     * Creates a CHD file with an invalid (non-MComprHD) tag signature.
     *
     * <p>The generated file uses {@code INVALIDX} as its 8-byte tag, which causes
     * {@link CHDInfoReader#isValidTag()} to return {@code false}.</p>
     *
     * @param filename the filename to use within the temporary directory
     * @return the created {@link File} with an invalid CHD tag
     * @throws IOException if the file cannot be written
     */
    private File createInvalidTagCHD(String filename) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] invalidTag = "INVALIDX".getBytes();
            ByteBuffer bb = ByteBuffer.allocate(1024);
            bb.put(invalidTag);
            bb.putInt(76);
            bb.putInt(1);
            raf.write(bb.array());
        }
        return file;
    }

    /**
     * Tests verifying that {@link CHDInfoReader} correctly detects CHD format versions 1-5
     * and extracts the appropriate checksums (MD5, SHA1, or both) at the expected byte offsets.
     */
    @Nested
    @DisplayName("Happy Path - Version Detection")
    class VersionDetectionTests {

        /**
         * Verifies that CHD version 1 is detected correctly and the MD5 checksum is extracted
         * from the expected header offset.
         */
        @Test
        @DisplayName("Should detect CHD version 1 and extract MD5 checksum")
        void shouldDetectVersion1AndExtractMD5() throws IOException {
            // Arrange - V1 header: 76 bytes, MD5 at offset 48
            byte[] headerData = new byte[60];
            byte[] md5Bytes = new byte[]{
                0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
                (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98, 0x76, 0x54, 0x32, 0x10
            };
            System.arraycopy(md5Bytes, 0, headerData, 32, 16); // offset 48 - 16 (after tag+len+ver)
            
            File chdFile = createSyntheticCHD("test_v1.chd", 1, 76, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(1);
            assertThat(reader.getLen()).isEqualTo(76);
            assertThat(reader.getMD5()).isEqualTo("0123456789abcdeffedcba9876543210");
        }

        /**
         * Verifies that CHD version 2 is detected correctly and the MD5 checksum is extracted
         * from the expected header offset.
         */
        @Test
        @DisplayName("Should detect CHD version 2 and extract MD5 checksum")
        void shouldDetectVersion2AndExtractMD5() throws IOException {
            // Arrange - V2 header: 80 bytes, MD5 at offset 48
            byte[] headerData = new byte[64];
            byte[] md5Bytes = new byte[]{
                (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, 0x11, 0x22, 0x33, 0x44,
                0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, 0x00, (byte)0xaa, (byte)0xbb
            };
            System.arraycopy(md5Bytes, 0, headerData, 32, 16);
            
            File chdFile = createSyntheticCHD("test_v2.chd", 2, 80, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(2);
            assertThat(reader.getLen()).isEqualTo(80);
            assertThat(reader.getMD5()).isEqualTo("aabbccdd11223344556677889900aabb");
        }

        /**
         * Verifies that CHD version 3 is detected correctly and both MD5 and SHA1 checksums
         * are extracted from the expected header offsets.
         */
        @Test
        @DisplayName("Should detect CHD version 3 and extract both MD5 and SHA1")
        void shouldDetectVersion3AndExtractMD5AndSHA1() throws IOException {
            // Arrange - V3 header: 120 bytes, MD5 at offset 56, SHA1 at offset 72
            byte[] headerData = new byte[104];
            byte[] md5Bytes = new byte[]{
                0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
                (byte)0x88, (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff
            };
            byte[] sha1Bytes = new byte[]{
                0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
                0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
                0x01, 0x23, 0x45, 0x67
            };
            System.arraycopy(md5Bytes, 0, headerData, 40, 16);  // offset 56 - 16
            System.arraycopy(sha1Bytes, 0, headerData, 56, 20); // offset 72 - 16
            
            File chdFile = createSyntheticCHD("test_v3.chd", 3, 120, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(3);
            assertThat(reader.getLen()).isEqualTo(120);
            assertThat(reader.getMD5()).isEqualTo("00112233445566778899aabbccddeeff");
            assertThat(reader.getSHA1()).isEqualTo("0123456789abcdef0123456789abcdef01234567");
        }

        /**
         * Verifies that CHD version 4 is detected correctly and both MD5 and SHA1 checksums
         * are extracted from the expected header offsets.
         */
        @Test
        @DisplayName("Should detect CHD version 4 and extract both MD5 and SHA1")
        void shouldDetectVersion4AndExtractMD5AndSHA1() throws IOException {
            // Arrange - V4 header: 108 bytes, MD5 at offset 48, SHA1 at offset 64
            byte[] headerData = new byte[92];
            byte[] md5Bytes = new byte[]{
                (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef, 0x12, 0x34, 0x56, 0x78,
                (byte)0x9a, (byte)0xbc, (byte)0xde, (byte)0xf0, 0x11, 0x22, 0x33, 0x44
            };
            byte[] sha1Bytes = new byte[]{
                (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, 0x12, 0x34, 0x56, 0x78,
                (byte)0x9a, (byte)0xbc, (byte)0xde, (byte)0xf0, 0x11, 0x22, 0x33, 0x44,
                0x55, 0x66, 0x77, (byte)0x88
            };
            System.arraycopy(md5Bytes, 0, headerData, 32, 16);  // offset 48 - 16
            System.arraycopy(sha1Bytes, 0, headerData, 48, 20); // offset 64 - 16
            
            File chdFile = createSyntheticCHD("test_v4.chd", 4, 108, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(4);
            assertThat(reader.getLen()).isEqualTo(108);
            assertThat(reader.getMD5()).isEqualTo("deadbeef123456789abcdef011223344");
            assertThat(reader.getSHA1()).isEqualTo("cafebabe123456789abcdef01122334455667788");
        }

        /**
         * Verifies that CHD version 5 is detected correctly and only the SHA1 checksum
         * is extracted, with MD5 returning null.
         */
        @Test
        @DisplayName("Should detect CHD version 5 and extract SHA1 only")
        void shouldDetectVersion5AndExtractSHA1Only() throws IOException {
            // Arrange - V5 header: 124 bytes, SHA1 at offset 84
            byte[] headerData = new byte[108];
            byte[] sha1Bytes = new byte[]{
                (byte)0xab, (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67, (byte)0x89,
                (byte)0xab, (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67, (byte)0x89,
                (byte)0xab, (byte)0xcd, (byte)0xef, 0x01
            };
            System.arraycopy(sha1Bytes, 0, headerData, 68, 20); // offset 84 - 16
            
            File chdFile = createSyntheticCHD("test_v5.chd", 5, 124, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(5);
            assertThat(reader.getLen()).isEqualTo(124);
            assertThat(reader.getSHA1()).isEqualTo("abcdef0123456789abcdef0123456789abcdef01");
            assertThat(reader.getMD5()).isNull();
        }
    }

    /**
     * Tests verifying that {@link CHDInfoReader} correctly handles CHD files
     * with an invalid or unrecognised tag signature.
     */
    @Nested
    @DisplayName("Invalid Tag Handling")
    class InvalidTagTests {

        /**
         * Verifies that isValidTag returns false when the CHD signature is invalid.
         */
        @Test
        @DisplayName("Should return false for isValidTag when CHD signature is invalid")
        void shouldReturnFalseForInvalidTag() throws IOException {
            // Arrange
            File invalidFile = createInvalidTagCHD("invalid_tag.chd");
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(invalidFile);
            
            // Assert
            assertThat(reader.isValidTag()).isFalse();
        }

        /**
         * Verifies that checksums return null when the CHD tag is invalid.
         */
        @Test
        @DisplayName("Should return null checksums when tag is invalid")
        void shouldReturnNullChecksumsWhenTagInvalid() throws IOException {
            // Arrange
            File invalidFile = createInvalidTagCHD("invalid_checksums.chd");
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(invalidFile);
            
            // Assert
            assertThat(reader.getMD5()).isNull();
            assertThat(reader.getSHA1()).isNull();
        }
    }

    /**
     * Tests verifying that {@link CHDInfoReader} raises appropriate exceptions or
     * degrades gracefully when given missing, empty, or truncated input files.
     */
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        /**
         * Verifies that IOException is thrown when the CHD file does not exist.
         */
        @Test
        @DisplayName("Should throw IOException when file does not exist")
        void shouldThrowIOExceptionWhenFileMissing() {
            // Arrange
            File missingFile = tempDir.resolve("nonexistent.chd").toFile();
            
            // Act & Assert
            assertThatThrownBy(() -> new CHDInfoReader(missingFile))
                .isInstanceOf(IOException.class);
        }

        /**
         * Verifies that an empty file is handled gracefully with null checksums and zero version.
         */
        @Test
        @DisplayName("Should handle empty file gracefully")
        void shouldHandleEmptyFileGracefully() throws IOException {
            // Arrange
            File emptyFile = tempDir.resolve("empty.chd").toFile();
            emptyFile.createNewFile();
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(emptyFile);
            
            // Assert - should not throw, but checksums should be null
            assertThat(reader.getVersion()).isZero();
            assertThat(reader.getMD5()).isNull();
            assertThat(reader.getSHA1()).isNull();
        }

        /**
         * Verifies that a truncated file is handled gracefully with null checksums and zero version.
         */
        @Test
        @DisplayName("Should handle truncated file gracefully")
        void shouldHandleTruncatedFileGracefully() throws IOException {
            // Arrange - file smaller than minimum header size
            File truncatedFile = tempDir.resolve("truncated.chd").toFile();
            try (RandomAccessFile raf = new RandomAccessFile(truncatedFile, "rw")) {
                raf.write(new byte[8]); // Only write tag, no length/version
            }
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(truncatedFile);
            
            // Assert - should not throw, but checksums should be null
            assertThat(reader.getVersion()).isZero();
            assertThat(reader.getMD5()).isNull();
            assertThat(reader.getSHA1()).isNull();
        }
    }

    /**
     * Tests verifying that {@link CHDInfoReader} handles edge cases such as
     * unsupported version numbers and byte-to-hex conversion edge values.
     */
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        /**
         * Verifies that an unknown CHD version is handled gracefully with null checksums.
         */
        @Test
        @DisplayName("Should handle unknown version gracefully")
        void shouldHandleUnknownVersionGracefully() throws IOException {
            // Arrange - valid tag but unsupported version
            File file = tempDir.resolve("unknown_version.chd").toFile();
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                ByteBuffer bb = ByteBuffer.allocate(1024);
                bb.put(TAG_MCOMPRHD);
                bb.putInt(100);
                bb.putInt(99); // Unknown version
                raf.write(bb.array());
            }
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(file);
            
            // Assert
            assertThat(reader.isValidTag()).isTrue();
            assertThat(reader.getVersion()).isEqualTo(99);
            assertThat(reader.getMD5()).isNull();
            assertThat(reader.getSHA1()).isNull();
        }

        /**
         * Verifies that byte-to-hex conversion handles edge values (0xFF, 0x00) correctly.
         */
        @Test
        @DisplayName("Should correctly convert bytes to hex string")
        void shouldCorrectlyConvertBytesToHex() throws IOException {
            // Arrange - V1 with known MD5 pattern
            byte[] headerData = new byte[60];
            byte[] md5Bytes = new byte[]{
                (byte)0xFF, (byte)0x00, (byte)0xAB, (byte)0xCD,
                0x12, 0x34, 0x56, 0x78, (byte)0x9A, (byte)0xBC,
                (byte)0xDE, (byte)0xF0, 0x11, 0x22, 0x33, 0x44
            };
            System.arraycopy(md5Bytes, 0, headerData, 32, 16);
            
            File chdFile = createSyntheticCHD("hex_test.chd", 1, 76, headerData);
            
            // Act
            CHDInfoReader reader = new CHDInfoReader(chdFile);
            
            // Assert
            assertThat(reader.getMD5())
                .isEqualTo("ff00abcd123456789abcdef011223344")
                .hasSize(32)
                .matches("[0-9a-f]{32}");
        }
    }
}
