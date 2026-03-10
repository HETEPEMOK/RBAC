import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("FormatUtils Tests")
class FormatUtilsTest {

    @Nested
    @DisplayName("Table Formatting Tests")
    class TableFormattingTests {
        @Test
        @DisplayName("Should format table with headers and rows")
        void formatTable() {
            String[] headers = {"Name", "Age", "City"};
            List<String[]> rows = List.of(
                    new String[]{"John", "25", "New York"},
                    new String[]{"Jane", "30", "London"},
                    new String[]{"Bob", "35", "Paris"}
            );

            String table = FormatUtils.formatTable(headers, rows);

            assertNotNull(table);
            assertTrue(table.contains("John"));
            assertTrue(table.contains("Jane"));
            assertTrue(table.contains("Bob"));
            assertTrue(table.contains("Name"));
            assertTrue(table.contains("Age"));
            assertTrue(table.contains("City"));
            assertTrue(table.contains("+"));
            assertTrue(table.contains("|"));
        }

        @Test
        @DisplayName("Should handle empty rows")
        void formatTableEmptyRows() {
            String[] headers = {"Name", "Age"};
            List<String[]> rows = List.of();

            String table = FormatUtils.formatTable(headers, rows);

            assertNotNull(table);
            assertTrue(table.contains("Name"));
            assertTrue(table.contains("Age"));
        }

        @Test
        @DisplayName("Should handle null values in rows")
        void formatTableWithNulls() {
            String[] headers = {"Name", "Age"};
            List<String[]> rows = List.of(
                    new String[]{"John", null},
                    new String[]{null, "30"}
            );

            String table = FormatUtils.formatTable(headers, rows);

            assertNotNull(table);
            assertTrue(table.contains("John"));
        }
    }

    @Nested
    @DisplayName("Box Formatting Tests")
    class BoxFormattingTests {
        @Test
        @DisplayName("Should format text in box")
        void formatBox() {
            String text = "Hello\nWorld";
            String box = FormatUtils.formatBox(text);

            assertNotNull(box);
            assertTrue(box.contains("+"));
            assertTrue(box.contains("-"));
            assertTrue(box.contains("|"));
            assertTrue(box.contains("Hello"));
            assertTrue(box.contains("World"));
        }

        @Test
        @DisplayName("Should handle single line text")
        void formatBoxSingleLine() {
            String text = "Hello";
            String box = FormatUtils.formatBox(text);

            assertTrue(box.contains("Hello"));
        }
    }

    @Nested
    @DisplayName("Header Formatting Tests")
    class HeaderFormattingTests {
        @Test
        @DisplayName("Should format header")
        void formatHeader() {
            String header = FormatUtils.formatHeader("Test Header");

            assertNotNull(header);
            assertTrue(header.contains("Test Header"));
            assertTrue(header.contains("==="));
        }

        @Test
        @DisplayName("Should handle empty header")
        void formatEmptyHeader() {
            String header = FormatUtils.formatHeader("");

            assertNotNull(header);
        }
    }

    @Nested
    @DisplayName("String Truncation Tests")
    class TruncationTests {
        @Test
        @DisplayName("Should truncate long strings")
        void truncateLongString() {
            String longStr = "This is a very long string";
            String truncated = FormatUtils.truncate(longStr, 10);

            assertEquals("This is...", truncated);
        }

        @Test
        @DisplayName("Should not truncate short strings")
        void dontTruncateShortString() {
            String shortStr = "Hello";
            String result = FormatUtils.truncate(shortStr, 10);

            assertEquals("Hello", result);
        }

        @Test
        @DisplayName("Should handle null")
        void truncateNull() {
            assertEquals("", FormatUtils.truncate(null, 10));
        }
    }

    @Nested
    @DisplayName("Padding Tests")
    class PaddingTests {
        @Test
        @DisplayName("Should pad right")
        void padRight() {
            assertEquals("Hello     ", FormatUtils.padRight("Hello", 10));
            assertEquals("Hello", FormatUtils.padRight("Hello", 3));
        }

        @Test
        @DisplayName("Should pad left")
        void padLeft() {
            assertEquals("     Hello", FormatUtils.padLeft("Hello", 10));
            assertEquals("Hello", FormatUtils.padLeft("Hello", 3));
        }

        @Test
        @DisplayName("Should handle null")
        void padNull() {
            assertEquals("          ", FormatUtils.padRight(null, 10));
            assertEquals("          ", FormatUtils.padLeft(null, 10));
        }
    }

    @Nested
    @DisplayName("Center Tests")
    class CenterTests {
        @Test
        @DisplayName("Should center text")
        void center() {
            assertEquals("  Hello  ", FormatUtils.center("Hello", 9));
            assertEquals("Hello", FormatUtils.center("Hello", 3));
        }

        @Test
        @DisplayName("Should handle null")
        void centerNull() {
            assertEquals("  ", FormatUtils.center(null, 2));
        }
    }

    @Nested
    @DisplayName("Repeat Tests")
    class RepeatTests {
        @Test
        @DisplayName("Should repeat string")
        void repeat() {
            assertEquals("***", FormatUtils.repeat("*", 3));
            assertEquals("", FormatUtils.repeat("*", 0));
            assertEquals("", FormatUtils.repeat("*", -1));
        }
    }
}