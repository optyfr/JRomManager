package jrm.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jrm.digest.MDigest.Algo;

/**
 * Tests for {@link MDigest} digest framework: algorithm enumeration, factory dispatch,
 * {@link CRCDigest}/{@link MsgDigest} hashing behavior, and stream-based {@code computeHash}.
 */
@DisplayName("MDigest tests")
class MDigestTest {

    @Nested
    @DisplayName("Algo enum")
    class AlgoEnum {

        @ParameterizedTest(name = "fromName(\"{0}\") should return {1}")
        @CsvSource({ "CRC, CRC32", "crc, CRC32", "MD5, MD5", "md5, MD5", "SHA-1, SHA1", "sha-1, SHA1" })
        @DisplayName("fromName should be case-insensitive")
        void fromNameShouldBeCaseInsensitive(String input, String expected) {
            assertThat(Algo.fromName(input)).isEqualTo(Algo.valueOf(expected));
        }

        @Test
        @DisplayName("fromName should return null for unknown algorithm")
        void fromNameShouldReturnNullForUnknownAlgorithm() {
            assertThat(Algo.fromName("UNKNOWN")).isNull();
        }

        @Test
        @DisplayName("fromName should return null for null input")
        void fromNameShouldReturnNullForNullInput() {
            assertThat(Algo.fromName(null)).isNull();
        }

        @Test
        @DisplayName("getName should return standard algorithm identifier")
        void getNameShouldReturnStandardAlgorithmIdentifier() {
            assertThat(Algo.CRC32.getName()).isEqualTo("CRC");
            assertThat(Algo.MD5.getName()).isEqualTo("MD5");
            assertThat(Algo.SHA1.getName()).isEqualTo("SHA-1");
        }
    }

    @Nested
    @DisplayName("getAlgorithm() factory")
    class GetAlgorithmFactory {

        @Test
        @DisplayName("should return CRCDigest for CRC32 algorithm")
        void shouldReturnCrcDigestForCrc32Algorithm() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.CRC32);

            assertThat(digest).isNotNull();
            assertThat(digest.getAlgorithm()).isEqualTo(Algo.CRC32);
        }

        @Test
        @DisplayName("should return MsgDigest for MD5 algorithm")
        void shouldReturnMsgDigestForMd5Algorithm() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.MD5);

            assertThat(digest).isNotNull();
            assertThat(digest.getAlgorithm()).isEqualTo(Algo.MD5);
        }

        @Test
        @DisplayName("should return MsgDigest for SHA1 algorithm")
        void shouldReturnMsgDigestForSha1Algorithm() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.SHA1);

            assertThat(digest).isNotNull();
            assertThat(digest.getAlgorithm()).isEqualTo(Algo.SHA1);
        }

        @Test
        @DisplayName("should throw NullPointerException for null algorithm")
        void shouldThrowNullPointerExceptionForNullAlgorithm() {
            assertThatThrownBy(() -> MDigest.getAlgorithm(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("CRC32 digest")
    class Crc32Digest {

        @Test
        @DisplayName("should compute CRC32 of empty input as 00000000")
        void shouldComputeCrc32OfEmptyInputAsZero() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.CRC32);

            assertThat(digest).hasToString("00000000");
        }

        @Test
        @DisplayName("should compute CRC32 matching JDK CRC32")
        void shouldComputeCrc32MatchingJdkCrc32() throws NoSuchAlgorithmException {
            final var bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
            final var expected = new CRC32();
            expected.update(bytes);

            final var digest = MDigest.getAlgorithm(Algo.CRC32);
            digest.update(bytes);

            assertThat(digest).hasToString(String.format("%08x", expected.getValue()));
        }

        @Test
        @DisplayName("should format as 8-char lowercase hex")
        void shouldFormatAs8CharLowercaseHex() throws NoSuchAlgorithmException {
            final var bytes = "test".getBytes(StandardCharsets.UTF_8);
            final var expected = new CRC32();
            expected.update(bytes);

            final var digest = MDigest.getAlgorithm(Algo.CRC32);
            digest.update(bytes);

            final var result = digest.toString();
            assertThat(result).hasSize(8).isEqualTo(result.toLowerCase());
        }

        @Test
        @DisplayName("should support update with offset and length")
        void shouldSupportUpdateWithOffsetAndLength() throws NoSuchAlgorithmException {
            final var bytes = "XXHello WorldYY".getBytes(StandardCharsets.UTF_8);
            final var expected = new CRC32();
            expected.update("Hello World".getBytes(StandardCharsets.UTF_8));

            final var digest = MDigest.getAlgorithm(Algo.CRC32);
            digest.update(bytes, 2, 11);

            assertThat(digest).hasToString(String.format("%08x", expected.getValue()));
        }

        @Test
        @DisplayName("update(byte[]) should delegate to update with offset 0")
        void updateByteArrayShouldDelegateToUpdateWithOffsetZero() throws NoSuchAlgorithmException {
            final var bytes = "data".getBytes(StandardCharsets.UTF_8);
            final var expected = new CRC32();
            expected.update(bytes);

            final var digest = MDigest.getAlgorithm(Algo.CRC32);
            digest.update(bytes);

            assertThat(digest).hasToString(String.format("%08x", expected.getValue()));
        }

        @Test
        @DisplayName("reset should clear state for reuse")
        void resetShouldClearStateForReuse() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.CRC32);
            digest.update("first".getBytes(StandardCharsets.UTF_8));
            digest.reset();

            assertThat(digest).hasToString("00000000");

            digest.update("second".getBytes(StandardCharsets.UTF_8));
            final var expected = new CRC32();
            expected.update("second".getBytes(StandardCharsets.UTF_8));

            assertThat(digest).hasToString(String.format("%08x", expected.getValue()));
        }
    }

    @Nested
    @DisplayName("Message digest (MD5/SHA1)")
    class MessageDigestVariants {

        @Test
        @DisplayName("should compute MD5 matching JDK MessageDigest")
        void shouldComputeMd5MatchingJdkMessageDigest() throws NoSuchAlgorithmException {
            final var bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
            final var jdk = MessageDigest.getInstance("MD5");
            final var expected = Hex.encodeHexString(jdk.digest(bytes));

            final var digest = MDigest.getAlgorithm(Algo.MD5);
            digest.update(bytes);

            assertThat(digest).hasToString(expected);
        }

        @Test
        @DisplayName("should compute SHA1 matching JDK MessageDigest")
        void shouldComputeSha1MatchingJdkMessageDigest() throws NoSuchAlgorithmException {
            final var bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
            final var jdk = MessageDigest.getInstance("SHA-1");
            final var expected = Hex.encodeHexString(jdk.digest(bytes));

            final var digest = MDigest.getAlgorithm(Algo.SHA1);
            digest.update(bytes);

            assertThat(digest).hasToString(expected);
        }

        @Test
        @DisplayName("should produce lowercase hex for MD5")
        void shouldProduceLowercaseHexForMd5() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.MD5);
            digest.update("abc".getBytes(StandardCharsets.UTF_8));

            final var result = digest.toString();
            assertThat(result).hasSize(32).isEqualTo(result.toLowerCase());
        }

        @Test
        @DisplayName("should produce lowercase hex for SHA1")
        void shouldProduceLowercaseHexForSha1() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.SHA1);
            digest.update("abc".getBytes(StandardCharsets.UTF_8));

            final var result = digest.toString();
            assertThat(result).hasSize(40).isEqualTo(result.toLowerCase());
        }

        @Test
        @DisplayName("should compute empty input MD5")
        void shouldComputeEmptyInputMd5() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.MD5);
            final var jdk = MessageDigest.getInstance("MD5");
            final var expected = Hex.encodeHexString(jdk.digest());

            assertThat(digest).hasToString(expected);
        }

        @Test
        @DisplayName("reset should clear state for MD5 reuse")
        void resetShouldClearStateForMd5Reuse() throws NoSuchAlgorithmException {
            final var digest = MDigest.getAlgorithm(Algo.MD5);
            digest.update("first".getBytes(StandardCharsets.UTF_8));
            digest.reset();
            digest.update("second".getBytes(StandardCharsets.UTF_8));

            final var jdk = MessageDigest.getInstance("MD5");
            jdk.update("second".getBytes(StandardCharsets.UTF_8));

            assertThat(digest).hasToString(Hex.encodeHexString(jdk.digest()));
        }
    }

    @Nested
    @DisplayName("computeHash()")
    class ComputeHash {

        @Test
        @DisplayName("should compute all digests from input stream in parallel")
        void shouldComputeAllDigestsFromInputStream() throws NoSuchAlgorithmException, IOException {
            final var data = "Hello World".getBytes(StandardCharsets.UTF_8);
            final var digests = new MDigest[] {
                MDigest.getAlgorithm(Algo.CRC32),
                MDigest.getAlgorithm(Algo.MD5),
                MDigest.getAlgorithm(Algo.SHA1)
            };

            try (final var in = new ByteArrayInputStream(data)) {
                MDigest.computeHash(in, digests);
            }

            final var crc = new CRC32();
            crc.update(data);
            final var md5 = MessageDigest.getInstance("MD5");
            final var sha1 = MessageDigest.getInstance("SHA-1");

            assertThat(digests[0]).hasToString(String.format("%08x", crc.getValue()));
            assertThat(digests[1]).hasToString(Hex.encodeHexString(md5.digest(data)));
            assertThat(digests[2]).hasToString(Hex.encodeHexString(sha1.digest(data)));
        }

        @Test
        @DisplayName("should return the same array instance passed in")
        void shouldReturnSameArrayInstancePassedIn() throws NoSuchAlgorithmException, IOException {
            final var digests = new MDigest[] { MDigest.getAlgorithm(Algo.CRC32) };

            try (final var in = new ByteArrayInputStream(new byte[0])) {
                final var result = MDigest.computeHash(in, digests);

                assertThat(result).isSameAs(digests);
            }
        }

        @Test
        @DisplayName("should compute empty input stream leaving digests at initial state")
        void shouldComputeEmptyInputStreamLeavingDigestsAtInitialState() throws NoSuchAlgorithmException, IOException {
            final var digests = new MDigest[] {
                MDigest.getAlgorithm(Algo.CRC32),
                MDigest.getAlgorithm(Algo.MD5)
            };

            try (final var in = new ByteArrayInputStream(new byte[0])) {
                MDigest.computeHash(in, digests);
            }

            assertThat(digests[0]).hasToString("00000000");
            final var md5 = MessageDigest.getInstance("MD5");
            assertThat(digests[1]).hasToString(Hex.encodeHexString(md5.digest()));
        }
    }
}
