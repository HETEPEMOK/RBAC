import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.List;

@DisplayName("AuditLog Tests")
class AuditLogTest {
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Nested
    @DisplayName("Logging Tests")
    class LoggingTests {
        @Test
        @DisplayName("Should add log entry")
        void addLogEntry() {
            auditLog.log("CREATE_USER", "admin", "john_doe", "User created");

            assertEquals(1, auditLog.count());
            List<AuditEntry> entries = auditLog.getAll();
            assertEquals(1, entries.size());

            AuditEntry entry = entries.get(0);
            assertEquals("CREATE_USER", entry.action());
            assertEquals("admin", entry.performer());
            assertEquals("john_doe", entry.target());
            assertEquals("User created", entry.details());
            assertNotNull(entry.timestamp());
        }

        @Test
        @DisplayName("Should add multiple log entries")
        void addMultipleEntries() {
            auditLog.log("CREATE_USER", "admin", "john", "User created");
            auditLog.log("CREATE_ROLE", "admin", "Manager", "Role created");
            auditLog.log("ASSIGN_ROLE", "admin", "john", "Role Manager assigned");

            assertEquals(3, auditLog.count());
        }

        @Test
        @DisplayName("Should handle null values")
        void handleNullValues() {
            auditLog.log(null, null, null, null);
            assertEquals(1, auditLog.count());

            AuditEntry entry = auditLog.getAll().get(0);
            assertNull(entry.action());
            assertNull(entry.performer());
            assertNull(entry.target());
            assertNull(entry.details());
        }
    }

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {
        @BeforeEach
        void setUp() {
            auditLog.log("CREATE_USER", "admin", "john", "User created");
            auditLog.log("CREATE_USER", "admin", "jane", "User created");
            auditLog.log("CREATE_ROLE", "admin", "Manager", "Role created");
            auditLog.log("ASSIGN_ROLE", "admin", "john", "Role assigned");
            auditLog.log("ASSIGN_ROLE", "manager", "jane", "Role assigned");
        }

        @Test
        @DisplayName("Should get entries by performer")
        void getByPerformer() {
            List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
            assertEquals(4, adminEntries.size());

            List<AuditEntry> managerEntries = auditLog.getByPerformer("manager");
            assertEquals(1, managerEntries.size());

            List<AuditEntry> nonExistent = auditLog.getByPerformer("nonexistent");
            assertTrue(nonExistent.isEmpty());
        }

        @Test
        @DisplayName("Should get entries by action")
        void getByAction() {
            List<AuditEntry> createUserEntries = auditLog.getByAction("CREATE_USER");
            assertEquals(2, createUserEntries.size());

            List<AuditEntry> assignRoleEntries = auditLog.getByAction("ASSIGN_ROLE");
            assertEquals(2, assignRoleEntries.size());

            List<AuditEntry> nonExistent = auditLog.getByAction("NONEXISTENT");
            assertTrue(nonExistent.isEmpty());
        }

        @Test
        @DisplayName("Should get all entries")
        void getAll() {
            List<AuditEntry> all = auditLog.getAll();
            assertEquals(5, all.size());
        }
    }

    @Nested
    @DisplayName("File Operations Tests")
    class FileOperationsTests {
        private static final String TEST_FILE = "test_audit.log";

        @AfterEach
        void cleanup() {
            new File(TEST_FILE).delete();
        }

        @Test
        @DisplayName("Should save log to file")
        void saveToFile() {
            auditLog.log("CREATE_USER", "admin", "john", "User created");
            auditLog.log("CREATE_ROLE", "admin", "Manager", "Role created");

            auditLog.saveToFile(TEST_FILE);

            File file = new File(TEST_FILE);
            assertTrue(file.exists());
            assertTrue(file.length() > 0);
        }

        @Test
        @DisplayName("Should handle empty log when saving")
        void saveEmptyLog() {
            auditLog.saveToFile(TEST_FILE);

            File file = new File(TEST_FILE);
            assertTrue(file.exists());
            assertEquals(0, file.length());
        }
    }

    @Nested
    @DisplayName("Clear Operations Tests")
    class ClearTests {
        @Test
        @DisplayName("Should clear all entries")
        void clear() {
            auditLog.log("TEST", "user", "target", "details");
            assertEquals(1, auditLog.count());

            auditLog.clear();
            assertEquals(0, auditLog.count());
            assertTrue(auditLog.getAll().isEmpty());
        }
    }
}