import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateTime Tests")
class DateTimeTest {

    @Nested
    @DisplayName("Current Date/Time Tests")
    class CurrentDateTimeTests {
        @Test
        @DisplayName("Should return current date")
        void getCurrentDate() {
            String date = DateTime.getCurrentDate();
            assertNotNull(date);
            assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
        }

        @Test
        @DisplayName("Should return current datetime")
        void getCurrentDateTime() {
            String datetime = DateTime.getCurrentDateTime();
            assertNotNull(datetime);
            assertTrue(datetime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        }

        @Test
        @DisplayName("Should return current simple datetime")
        void getCurrentSimpleDateTime() {
            String datetime = DateTime.getCurrentSimpleDateTime();
            assertNotNull(datetime);
            assertTrue(datetime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
        }
    }

    @Nested
    @DisplayName("Date Comparison Tests")
    class DateComparisonTests {
        @Test
        @DisplayName("Should compare dates correctly")
        void compareDates() {
            assertTrue(DateTime.isBefore("2024-01-01", "2024-12-31"));
            assertFalse(DateTime.isBefore("2024-12-31", "2024-01-01"));

            assertTrue(DateTime.isAfter("2024-12-31", "2024-01-01"));
            assertFalse(DateTime.isAfter("2024-01-01", "2024-12-31"));

            assertTrue(DateTime.isEqual("2024-01-01", "2024-01-01"));
            assertFalse(DateTime.isEqual("2024-01-01", "2024-12-31"));
        }

        @Test
        @DisplayName("Should compare datetimes correctly")
        void compareDateTimes() {
            assertTrue(DateTime.isBefore("2024-01-01 10:00", "2024-01-01 11:00"));
            assertTrue(DateTime.isAfter("2024-01-01 11:00", "2024-01-01 10:00"));
            assertTrue(DateTime.isEqual("2024-01-01 10:00", "2024-01-01 10:00"));
        }
    }

    @Nested
    @DisplayName("Date Arithmetic Tests")
    class DateArithmeticTests {
        @Test
        @DisplayName("Should add days to date")
        void addDays() {
            assertEquals("2024-01-06", DateTime.addDays("2024-01-01", 5));
            assertEquals("2023-12-27", DateTime.addDays("2024-01-01", -5));
        }

        @Test
        @DisplayName("Should add days to datetime")
        void addDaysToDateTime() {
            assertEquals("2024-01-06 10:30", DateTime.addDays("2024-01-01 10:30", 5));
            assertEquals("2023-12-27 10:30", DateTime.addDays("2024-01-01 10:30", -5));
        }

        @Test
        @DisplayName("Should throw on invalid date")
        void addDaysInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> DateTime.addDays("invalid", 5));
        }
    }

    @Nested
    @DisplayName("Relative Time Tests")
    class RelativeTimeTests {
        @Test
        @DisplayName("Should format past dates")
        void formatPastDates() {
            String past = "2020-01-01";
            String relative = DateTime.formatRelativeTime(past);
            assertTrue(relative.contains("ago"));
        }

        @Test
        @DisplayName("Should format future dates")
        void formatFutureDates() {
            String future = "2030-01-01";
            String relative = DateTime.formatRelativeTime(future);
            assertTrue(relative.contains("in"));
        }

        @Test
        @DisplayName("Should handle invalid dates")
        void formatInvalidDate() {
            assertEquals("unknown date", DateTime.formatRelativeTime("invalid"));
        }
    }

    @Nested
    @DisplayName("Days Between Tests")
    class DaysBetweenTests {
        @Test
        @DisplayName("Should calculate days between")
        void daysBetween() {
            assertEquals(5, DateTime.daysBetween("2024-01-01", "2024-01-06"));
            assertEquals(-5, DateTime.daysBetween("2024-01-06", "2024-01-01"));
            assertEquals(0, DateTime.daysBetween("2024-01-01", "2024-01-01"));
        }

        @Test
        @DisplayName("Should handle datetimes")
        void daysBetweenDateTimes() {
            assertEquals(5, DateTime.daysBetween("2024-01-01 10:00", "2024-01-06 15:30"));
        }
    }

    @Nested
    @DisplayName("Expiration Tests")
    class ExpirationTests {
        @Test
        @DisplayName("Should detect expired dates")
        void isExpired() {
            String past = "2020-01-01 00:00";
            assertTrue(DateTime.isExpired(past));

            String future = "2030-01-01 00:00";
            assertFalse(DateTime.isExpired(future));
        }

        @Test
        @DisplayName("Should calculate remaining time")
        void getRemainingTime() {
            String future = "2030-01-01 00:00";
            String remaining = DateTime.getRemainingTime(future);
            assertNotNull(remaining);

            String past = "2020-01-01 00:00";
            assertEquals("Expired", DateTime.getRemainingTime(past));
        }
    }
}