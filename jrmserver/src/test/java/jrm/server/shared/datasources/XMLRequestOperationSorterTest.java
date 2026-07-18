package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jrm.server.shared.datasources.XMLRequest.Operation.Sorter;

/**
 * Unit tests for {@link XMLRequest.Operation.Sorter} parsing logic.
 */
@DisplayName("XMLRequest.Operation.Sorter")
class XMLRequestOperationSorterTest {

    @Nested
    @DisplayName("constructor parsing")
    class ConstructorTest {
        @ParameterizedTest
        @CsvSource({ "name,false,name", "-date,true,date", "field1,false,field1" })
        @DisplayName("parses name and descending flag from directive")
        void parsesDirective(final String input, final boolean expectedDesc, final String expectedName) {
            final Sorter sorter = new Sorter(input);
            assertThat(sorter.getName()).isEqualTo(expectedName);
            assertThat(sorter.isDesc()).isEqualTo(expectedDesc);
        }

        @Test
        @DisplayName("empty string results in empty name and ascending")
        void emptyString() {
            final Sorter sorter = new Sorter("");
            assertThat(sorter.getName()).isEmpty();
            assertThat(sorter.isDesc()).isFalse();
        }
    }

    @Nested
    @DisplayName("default constructor")
    class DefaultConstructorTest {
        @Test
        @DisplayName("default constructor leaves name null and desc false")
        void defaultConstructor() {
            final Sorter sorter = new Sorter();
            assertThat(sorter.getName()).isNull();
            assertThat(sorter.isDesc()).isFalse();
        }
    }
}