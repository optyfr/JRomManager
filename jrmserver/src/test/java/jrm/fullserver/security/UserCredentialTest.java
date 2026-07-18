package jrm.fullserver.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Lombok {@code @Data} bean {@link UserCredential}.
 */
@DisplayName("UserCredential bean")
class UserCredentialTest {

    @Nested
    @DisplayName("constructors and getters")
    class ConstructorTest {
        @Test
        @DisplayName("default constructor leaves fields null")
        void defaultConstructor() {
            final UserCredential uc = new UserCredential();
            assertThat(uc.getLogin()).isNull();
            assertThat(uc.getPassword()).isNull();
            assertThat(uc.getRoles()).isNull();
        }

        @Test
        @DisplayName("parameterized constructor sets all fields")
        void parameterizedConstructor() {
            final UserCredential uc = new UserCredential("admin", "hashed", "admin,user");
            assertThat(uc.getLogin()).isEqualTo("admin");
            assertThat(uc.getPassword()).isEqualTo("hashed");
            assertThat(uc.getRoles()).isEqualTo("admin,user");
        }
    }

    @Nested
    @DisplayName("setters")
    class SetterTest {
        @Test
        @DisplayName("setters update fields")
        void setters() {
            final UserCredential uc = new UserCredential();
            uc.setLogin("user1");
            uc.setPassword("secret");
            uc.setRoles("user");
            assertThat(uc.getLogin()).isEqualTo("user1");
            assertThat(uc.getPassword()).isEqualTo("secret");
            assertThat(uc.getRoles()).isEqualTo("user");
        }
    }

    @Nested
    @DisplayName("equals / hashCode / toString")
    class EqualsHashCodeTest {
        @Test
        @DisplayName("equal instances have same equals and hashCode")
        void equalsHashCode() {
            final UserCredential a = new UserCredential("admin", "pw", "admin");
            final UserCredential b = new UserCredential("admin", "pw", "admin");
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        }

        @Test
        @DisplayName("different instances are not equal")
        void notEqual() {
            final UserCredential a = new UserCredential("admin", "pw", "admin");
            final UserCredential b = new UserCredential("user", "pw", "user");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString contains field names")
        void toStringContainsFields() {
            final UserCredential uc = new UserCredential("admin", "pw", "admin");
            final String str = uc.toString();
            assertThat(str).contains("admin").contains("pw");
        }
    }
}