import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {
        @Test
        @DisplayName("Should accept valid usernames")
        void validUsernames() {
            assertTrue(ValidationUtils.isValidUsername("john_doe"));
            assertTrue(ValidationUtils.isValidUsername("admin123"));
            assertTrue(ValidationUtils.isValidUsername("user_123"));
            assertTrue(ValidationUtils.isValidUsername("abc"));
            assertTrue(ValidationUtils.isValidUsername("a".repeat(20)));
        }

        @Test
        @DisplayName("Should reject invalid usernames")
        void invalidUsernames() {
            assertFalse(ValidationUtils.isValidUsername("jo")); // too short
            assertFalse(ValidationUtils.isValidUsername("a".repeat(21))); // too long
            assertFalse(ValidationUtils.isValidUsername("user@name")); // special char
            assertFalse(ValidationUtils.isValidUsername("user name")); // space
            assertFalse(ValidationUtils.isValidUsername("user-name")); // hyphen
            assertFalse(ValidationUtils.isValidUsername("")); // empty
            assertFalse(ValidationUtils.isValidUsername(null)); // null
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {
        @Test
        @DisplayName("Should accept valid emails")
        void validEmails() {
            assertTrue(ValidationUtils.isValidEmail("user@example.com"));
            assertTrue(ValidationUtils.isValidEmail("john.doe@company.co.uk"));
            assertTrue(ValidationUtils.isValidEmail("user+label@gmail.com"));
            assertTrue(ValidationUtils.isValidEmail("123@test.org"));
            assertTrue(ValidationUtils.isValidEmail("user@sub.domain.com"));
        }

        @Test
        @DisplayName("Should reject invalid emails")
        void invalidEmails() {
            assertFalse(ValidationUtils.isValidEmail("user@"));
            assertFalse(ValidationUtils.isValidEmail("@domain.com"));
            assertFalse(ValidationUtils.isValidEmail("user@.com"));
            assertFalse(ValidationUtils.isValidEmail("user@domain"));
            assertFalse(ValidationUtils.isValidEmail("user domain.com"));
            assertFalse(ValidationUtils.isValidEmail(""));
            assertFalse(ValidationUtils.isValidEmail(null));
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {
        @Test
        @DisplayName("Should accept valid dates")
        void validDates() {
            assertTrue(ValidationUtils.isValidDate("2024-12-25"));
            assertTrue(ValidationUtils.isValidDate("2024-12-25 14:30"));
            assertTrue(ValidationUtils.isValidDate("2024-12-25 14:30:00"));
        }
    }

    @Nested
    @DisplayName("String Normalization Tests")
    class StringNormalizationTests {
        @Test
        @DisplayName("Should normalize strings correctly")
        void normalizeString() {
            assertEquals("hello world", ValidationUtils.normalizeString("  hello   world  "));
            assertEquals("test", ValidationUtils.normalizeString("test"));
            assertEquals("", ValidationUtils.normalizeString("   "));
            assertEquals("", ValidationUtils.normalizeString(null));
        }

        @Test
        @DisplayName("Should normalize username correctly")
        void normalizeUsername() {
            assertEquals("john_doe", ValidationUtils.normalizeUsername("  JOHN_DOE  "));
            assertEquals("", ValidationUtils.normalizeUsername(null));
        }

        @Test
        @DisplayName("Should normalize email correctly")
        void normalizeEmail() {
            assertEquals("user@example.com", ValidationUtils.normalizeEmail("  USER@EXAMPLE.COM  "));
            assertEquals("", ValidationUtils.normalizeEmail(null));
        }
    }

    @Nested
    @DisplayName("Required Field Validation Tests")
    class RequiredFieldTests {
        @Test
        @DisplayName("Should accept non-empty strings")
        void requireNonNullEmptyValid() {
            assertDoesNotThrow(() -> ValidationUtils.requireNonNullEmpty("test", "field"));
            assertDoesNotThrow(() -> ValidationUtils.requireNonNullEmpty("  test  ", "field"));
        }

        @Test
        @DisplayName("Should throw exception for empty strings")
        void requireNonNullEmptyInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonNullEmpty("", "field"));
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonNullEmpty("   ", "field"));
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonNullEmpty(null, "field"));
        }
    }
}