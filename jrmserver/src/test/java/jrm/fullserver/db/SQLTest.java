package jrm.fullserver.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.fullserver.ServerSettings;
import jrm.server.shared.TestWebSessions;

/**
 * Unit tests for {@link SQL} using an in-memory H2 database.
 */
@DisplayName("SQL operations")
class SQLTest {

    
    private TestSQL sql;

    /** Concrete subclass of SQL for testing protected methods. */
    private static final class TestSQL extends SQL {
        TestSQL(final Connection db, final boolean shouldClose, final ServerSettings settings) {
            super(db, shouldClose, settings);
        }

        @Override
        public String getContext() {
            return "PUBLIC";
        }
    }

    @BeforeEach
    void setUp(@TempDir final Path tempDir) throws Exception {
        Connection connection;
        TestWebSessions.setWorkPath(tempDir);
        final H2 h2 = new H2(new ServerSettings());
        connection = h2.connectToDB("testdb", false, true, false);
        sql = new TestSQL(connection, true, new ServerSettings());
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
        sql.close();
    }

    @Nested
    @DisplayName("createSchema / dropSchema")
    class SchemaTest {
        @Test
        @DisplayName("createSchema creates and dropSchema drops")
        void createAndDropSchema() throws SQLException {
            sql.createSchema("test_schema", true);
            assertThat(sql.count("SELECT * FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'TEST_SCHEMA'")).isEqualTo(1L);
            sql.dropSchema("test_schema");
            assertThat(sql.count("SELECT * FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'TEST_SCHEMA'")).isZero();
        }
    }

    @Nested
    @DisplayName("update / query round-trip")
    class UpdateQueryTest {
        @Test
        @DisplayName("creates table, inserts, queries back")
        void roundTrip() throws SQLException {
            sql.update("CREATE TABLE test_table(id INT PRIMARY KEY, name VARCHAR(255))");
            sql.update("INSERT INTO test_table VALUES(?, ?)", 1, "hello");
            final var result = sql.queryFirst("SELECT * FROM test_table WHERE id=?", 1);
            assertThat(result).containsEntry("ID", 1).containsEntry("NAME", "hello");
        }

        @Test
        @DisplayName("count returns correct row count")
        void count() throws SQLException {
            sql.update("CREATE TABLE count_table(id INT PRIMARY KEY)");
            sql.update("INSERT INTO count_table VALUES(1)");
            sql.update("INSERT INTO count_table VALUES(2)");
            final Long count = sql.count("SELECT * FROM count_table");
            assertThat(count).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findArrayParam")
    class FindArrayParamTest {
        @Test
        @DisplayName("returns -1 when no array parameter")
        void noArray() {
            assertThat(sql.findArrayParam(new Object[] { "a", 1, true })).isEqualTo(-1);
        }

        @Test
        @DisplayName("returns index of array parameter")
        void arrayWithParam() {
            assertThat(sql.findArrayParam(new Object[] { "a", new int[] { 1, 2 }, "b" })).isEqualTo(1);
        }

        @Test
        @DisplayName("returns -1 for null args")
        void nullArgs() {
            assertThat(sql.findArrayParam(null)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("convertArrayParams")
    class ConvertArrayParamsTest {
        @Test
        @DisplayName("expands array param and replaces ANY(?) with IN(...)")
        void convertsArrayParam() {
            final AtomicReference<String> queryRef = new AtomicReference<>("SELECT * FROM t WHERE id = ANY(?)");
            final AtomicReference<Object[]> argsRef = new AtomicReference<>(new Object[] { new int[] { 1, 2, 3 } });
            sql.convertArrayParams(queryRef, argsRef);
            assertThat(queryRef.get()).contains("IN(");
            assertThat(argsRef.get()).hasSize(3);
        }

        @Test
        @DisplayName("no array param leaves query unchanged")
        void noArrayParam() {
            final AtomicReference<String> queryRef = new AtomicReference<>("SELECT * FROM t WHERE id = ?");
            final AtomicReference<Object[]> argsRef = new AtomicReference<>(new Object[] { 1 });
            sql.convertArrayParams(queryRef, argsRef);
            assertThat(queryRef.get()).isEqualTo("SELECT * FROM t WHERE id = ?");
            assertThat(argsRef.get()).hasSize(1);
        }
    }
}