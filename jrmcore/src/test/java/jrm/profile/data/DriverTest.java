package jrm.profile.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.profile.data.Driver.SaveStateType;
import jrm.profile.data.Driver.StatusType;

/**
 * Tests for {@link Driver} string-to-enum status parsing and defaults.
 * <p>
 * The {@code Driver} constructor is package-private, so this test lives in the same package.
 */
@DisplayName("Driver tests")
class DriverTest {

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
    }

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("should default all status fields to preliminary")
        void shouldDefaultAllStatusFieldsToPreliminary() {
            assertThat(driver.getStatus()).isEqualTo(StatusType.preliminary);
            assertThat(driver.getEmulation()).isEqualTo(StatusType.preliminary);
            assertThat(driver.getCocktail()).isEqualTo(StatusType.preliminary);
        }

        @Test
        @DisplayName("should default save state to unsupported")
        void shouldDefaultSaveStateToUnsupported() {
            assertThat(driver.getSaveState()).isEqualTo(SaveStateType.unsupported);
        }
    }

    @Nested
    @DisplayName("setStatus(String)")
    class SetStatusString {

        @Test
        @DisplayName("should set status from exact value name")
        void shouldSetStatusFromExactValueName() {
            driver.setStatus("good");

            assertThat(driver.getStatus()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should be case-insensitive")
        void shouldBeCaseInsensitive() {
            driver.setStatus("GOOD");

            assertThat(driver.getStatus()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should trim surrounding whitespace")
        void shouldTrimSurroundingWhitespace() {
            driver.setStatus("  imperfect  ");

            assertThat(driver.getStatus()).isEqualTo(StatusType.imperfect);
        }

        @Test
        @DisplayName("should ignore unknown status and keep previous value")
        void shouldIgnoreUnknownStatusAndKeepPreviousValue() {
            driver.setStatus("good");
            driver.setStatus("bogus");

            assertThat(driver.getStatus()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should ignore null and keep previous value")
        void shouldIgnoreNullAndKeepPreviousValue() {
            driver.setStatus("good");
            driver.setStatus(null);

            assertThat(driver.getStatus()).isEqualTo(StatusType.good);
        }
    }

    @Nested
    @DisplayName("setEmulation(String)")
    class SetEmulationString {

        @Test
        @DisplayName("should set emulation from value name")
        void shouldSetEmulationFromValueName() {
            driver.setEmulation("imperfect");

            assertThat(driver.getEmulation()).isEqualTo(StatusType.imperfect);
        }

        @Test
        @DisplayName("should be case-insensitive with trim")
        void shouldBeCaseInsensitiveWithTrim() {
            driver.setEmulation("  GOOD ");

            assertThat(driver.getEmulation()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should ignore unknown value")
        void shouldIgnoreUnknownValue() {
            driver.setEmulation("good");
            driver.setEmulation("nope");

            assertThat(driver.getEmulation()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should ignore null")
        void shouldIgnoreNull() {
            driver.setEmulation("imperfect");
            driver.setEmulation(null);

            assertThat(driver.getEmulation()).isEqualTo(StatusType.imperfect);
        }
    }

    @Nested
    @DisplayName("setCocktail(String)")
    class SetCocktailString {

        @Test
        @DisplayName("should set cocktail from value name")
        void shouldSetCocktailFromValueName() {
            driver.setCocktail("good");

            assertThat(driver.getCocktail()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should be case-insensitive with trim")
        void shouldBeCaseInsensitiveWithTrim() {
            driver.setCocktail("  PRELIMINARY ");

            assertThat(driver.getCocktail()).isEqualTo(StatusType.preliminary);
        }

        @Test
        @DisplayName("should ignore unknown value")
        void shouldIgnoreUnknownValue() {
            driver.setCocktail("good");
            driver.setCocktail("xyz");

            assertThat(driver.getCocktail()).isEqualTo(StatusType.good);
        }

        @Test
        @DisplayName("should ignore null")
        void shouldIgnoreNull() {
            driver.setCocktail("good");
            driver.setCocktail(null);

            assertThat(driver.getCocktail()).isEqualTo(StatusType.good);
        }
    }

    @Nested
    @DisplayName("setSaveState(String)")
    class SetSaveStateString {

        @Test
        @DisplayName("should set save state from value name")
        void shouldSetSaveStateFromValueName() {
            driver.setSaveState("supported");

            assertThat(driver.getSaveState()).isEqualTo(SaveStateType.supported);
        }

        @Test
        @DisplayName("should be case-insensitive with trim")
        void shouldBeCaseInsensitiveWithTrim() {
            driver.setSaveState("  UNSUPPORTED ");

            assertThat(driver.getSaveState()).isEqualTo(SaveStateType.unsupported);
        }

        @Test
        @DisplayName("should ignore unknown value and keep previous")
        void shouldIgnoreUnknownValueAndKeepPrevious() {
            driver.setSaveState("supported");
            driver.setSaveState("maybe");

            assertThat(driver.getSaveState()).isEqualTo(SaveStateType.supported);
        }

        @Test
        @DisplayName("should ignore null and keep previous")
        void shouldIgnoreNullAndKeepPrevious() {
            driver.setSaveState("supported");
            driver.setSaveState(null);

            assertThat(driver.getSaveState()).isEqualTo(SaveStateType.supported);
        }
    }

    @Test
    @DisplayName("should set all fields independently")
    void shouldSetAllFieldsIndependently() {
        driver.setStatus("good");
        driver.setEmulation("imperfect");
        driver.setCocktail("preliminary");
        driver.setSaveState("supported");

        assertThat(driver.getStatus()).isEqualTo(StatusType.good);
        assertThat(driver.getEmulation()).isEqualTo(StatusType.imperfect);
        assertThat(driver.getCocktail()).isEqualTo(StatusType.preliminary);
        assertThat(driver.getSaveState()).isEqualTo(SaveStateType.supported);
    }
}
