package jrm.misc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ExceptionUtils} safe-execution helpers.
 */
@DisplayName("ExceptionUtils tests")
class ExceptionUtilsTest {

    @Nested
    @DisplayName("test() with default fallback")
    class TestWithDefault {

        @Test
        @DisplayName("should return function result when no exception is thrown")
        void shouldReturnFunctionResultWhenNoException() {
            final var result = ExceptionUtils.test(String::length, "hello", -1);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("should return default when function throws")
        void shouldReturnDefaultWhenFunctionThrows() {
            final var result = ExceptionUtils.test(s -> {
                throw new IllegalStateException("boom");
            }, "anything", "fallback");

            assertThat(result).isEqualTo("fallback");
        }

        @Test
        @DisplayName("should return null default when function throws and default is null")
        void shouldReturnNullDefaultWhenFunctionThrowsAndDefaultIsNull() {
            final var result = ExceptionUtils.test(s -> {
                throw new IllegalStateException("boom");
            }, "anything", (String) null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should propagate null input through function")
        void shouldPropagateNullInputThroughFunction() {
            final var result = ExceptionUtils.test(x -> x == null ? "was-null" : "not-null", null, "default");

            assertThat(result).isEqualTo("was-null");
        }
    }

    @Nested
    @DisplayName("testF() with fallback function")
    class TestWithFallbackFunction {

        @Test
        @DisplayName("should return function result when no exception is thrown")
        void shouldReturnFunctionResultWhenNoException() {
            final var result = ExceptionUtils.testF(String::length, "hello", f -> -1);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("should invoke fallback function when exception is thrown")
        void shouldInvokeFallbackFunctionWhenExceptionThrown() {
            final var result = ExceptionUtils.testF(s -> {
                throw new IllegalStateException("boom");
            }, "anything", f -> -42);

            assertThat(result).isEqualTo(-42);
        }

        @Test
        @DisplayName("should pass the original test function to the fallback")
        void shouldPassOriginalTestFunctionToFallback() {
            final Function<String, Integer> original = s -> {
                if ("ignored".equals(s))
                    throw new IllegalStateException("boom");
                return s.length();
            };
            final var result = ExceptionUtils.testF(original, "ignored", f -> f.apply("meow"));

            assertThat(result).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("unthrow() without default")
    class UnthrowWithoutDefault {

        @Test
        @DisplayName("should accept result when function succeeds")
        void shouldAcceptResultWhenFunctionSucceeds() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.unthrow(captured::add, String::length, "hello");

            assertThat(captured).containsExactly(5);
        }

        @Test
        @DisplayName("should not accept anything when function throws and default is null")
        void shouldNotAcceptAnythingWhenFunctionThrowsAndDefaultIsNull() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.<String, Integer>unthrow(captured::add, (String s) -> {
                throw new IllegalStateException("boom");
            }, "anything");

            assertThat(captured).isEmpty();
        }
    }

    @Nested
    @DisplayName("unthrow() with default value")
    class UnthrowWithDefault {

        @Test
        @DisplayName("should accept function result when no exception is thrown")
        void shouldAcceptFunctionResultWhenNoException() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.unthrow(captured::add, String::length, "hello", -1);

            assertThat(captured).containsExactly(5);
        }

        @Test
        @DisplayName("should accept default value when function throws")
        void shouldAcceptDefaultValueWhenFunctionThrows() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.unthrow(captured::add, s -> {
                throw new IllegalStateException("boom");
            }, "anything", -1);

            assertThat(captured).containsExactly(-1);
        }

        @Test
        @DisplayName("should not accept anything when default is null")
        void shouldNotAcceptAnythingWhenDefaultIsNull() {
            final List<String> captured = new ArrayList<>();

            ExceptionUtils.unthrow(captured::add, s -> {
                throw new IllegalStateException("boom");
            }, "anything", (String) null);

            assertThat(captured).isEmpty();
        }
    }

    @Nested
    @DisplayName("unthrowF() with fallback function")
    class UnthrowFWithFallback {

        @Test
        @DisplayName("should accept function result when no exception is thrown")
        void shouldAcceptFunctionResultWhenNoException() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.unthrowF(captured::add, String::length, "hello", f -> -1);

            assertThat(captured).containsExactly(5);
        }

        @Test
        @DisplayName("should accept fallback result when function throws")
        void shouldAcceptFallbackResultWhenFunctionThrows() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.<String, Integer>unthrowF(captured::add, (String s) -> {
                throw new IllegalStateException("boom");
            }, "anything", f -> -42);

            assertThat(captured).containsExactly(-42);
        }

        @Test
        @DisplayName("should not accept anything when fallback returns null")
        void shouldNotAcceptAnythingWhenFallbackReturnsNull() {
            final List<Integer> captured = new ArrayList<>();

            ExceptionUtils.<String, Integer>unthrowF(captured::add, (String s) -> {
                throw new IllegalStateException("boom");
            }, "anything", f -> null);

            assertThat(captured).isEmpty();
        }
    }

    @Test
    @DisplayName("should not throw when null function result is returned successfully")
    void shouldNotThrowWhenNullFunctionResultIsReturnedSuccessfully() {
        final List<String> captured = new ArrayList<>();

        assertThatCode(() -> ExceptionUtils.unthrow(captured::add, s -> null, "anything", "default")).doesNotThrowAnyException();

        assertThat(captured).isEmpty();
    }
}
