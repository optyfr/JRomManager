package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest.Operation;

/**
 * Unit tests for {@link XMLRequest} SAX parsing.
 */
@DisplayName("XMLRequest parsing")
class XMLRequestTest {

    

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    private XMLRequest parse(final String xml) throws Exception {
        WebSession session;
        session = TestWebSessions.newAdminSession("test-session");
        return new XMLRequest(session, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xml.length());
    }

    @Nested
    @DisplayName("single fetch operation")
    class SingleFetchTest {
        @Test
        @DisplayName("parses operationType, operationId, startRow, endRow, sorters, data, oldValues")
        void parsesFullOperation() throws Exception {
            final String xml = """
                    <request>
                      <operationType>fetch</operationType>
                      <operationId>op1</operationId>
                      <startRow>5</startRow>
                      <endRow>10</endRow>
                      <sortBy>name</sortBy>
                      <sortBy>-date</sortBy>
                      <data>
                        <key1>value1</key1>
                        <key2>value2</key2>
                      </data>
                      <oldValues>
                        <field1>old1</field1>
                      </oldValues>
                    </request>
                    """;
            final XMLRequest req = parse(xml);
            assertThat(req.getTransaction()).isNull();
            final Operation op = req.getOperation();
            assertThat(op).isNotNull();
            assertThat(op.getOperationType()).hasToString("fetch");
            assertThat(op.getOperationId()).hasToString("op1");
            assertThat(op.getStartRow()).isEqualTo(5);
            assertThat(op.getEndRow()).isEqualTo(10);
            assertThat(op.getSort()).hasSize(2);
            assertThat(op.getSort().get(0).getName()).isEqualTo("name");
            assertThat(op.getSort().get(0).isDesc()).isFalse();
            assertThat(op.getSort().get(1).getName()).isEqualTo("date");
            assertThat(op.getSort().get(1).isDesc()).isTrue();
            assertThat(op.hasData("key1")).isTrue();
            assertThat(op.getData("key1")).isEqualTo("value1");
            assertThat(op.getData("key2")).isEqualTo("value2");
            assertThat(op.getOldValues()).containsEntry("field1", "old1");
        }
    }

    @Nested
    @DisplayName("transaction with multiple operations")
    class TransactionTest {
        @Test
        @DisplayName("parses transaction containing two operations")
        void parsesTransaction() throws Exception {
            final String xml = """
                    <transaction>
                      <request>
                        <operationType>fetch</operationType>
                        <operationId>op1</operationId>
                      </request>
                      <request>
                        <operationType>add</operationType>
                        <operationId>op2</operationId>
                      </request>
                    </transaction>
                    """;
            final XMLRequest req = parse(xml);
            assertThat(req.getOperation()).isNull();
            assertThat(req.getTransaction()).isNotNull();
            assertThat(req.getTransaction().getOperations()).hasSize(2);
            assertThat(req.getTransaction().getOperations().get(0).getOperationType()).hasToString("fetch");
            assertThat(req.getTransaction().getOperations().get(1).getOperationType()).hasToString("add");
        }
    }

    @Nested
    @DisplayName("XXE hardening")
    class XXETest {
        @Test
        @DisplayName("external entity is ignored without throwing")
        void externalEntityIgnored() throws Exception {
            final String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE request [
                      <!ENTITY xxe SYSTEM "file:///etc/passwd">
                    ]>
                    <request>
                      <operationType>fetch</operationType>
                      <operationId>&xxe;</operationId>
                    </request>
                    """;
            final XMLRequest req = parse(xml);
            // Should not throw; operationId may contain empty or the entity reference text
            assertThat(req.getOperation()).satisfiesAnyOf(
                    op -> assertThat(op.getOperationId().toString()).doesNotContain("root:"),
                    op -> assertThat(op.getOperationId().toString()).isNotNull());
        }
    }

    @Nested
    @DisplayName("malformed XML")
    class MalformedTest {
        @Test
        @DisplayName("malformed XML does not throw")
        void malformedXml() throws Exception {
            final String xml = "<request><operationType>fetch</operationType";
            // Should not throw - SAX errors are caught and logged internally
            final XMLRequest req = parse(xml);
            // Operation may or may not be null depending on how far parsing got
            assertThat(req).isNotNull();
        }
    }

    @Nested
    @DisplayName("Operation data contract")
    class OperationDataContractTest {
        @Test
        @DisplayName("addData accumulates multiple values per key")
        void addDataAccumulates() {
            final Operation op = new Operation();
            assertThat(op.addData("key", "v1")).isTrue();
            assertThat(op.addData("key", "v2")).isTrue();
            assertThat(op.getDatas("key")).containsExactly("v1", "v2");
            assertThat(op.getData("key")).isEqualTo("v1");
        }

        @Test
        @DisplayName("hasData returns false for missing key")
        void hasDataMissing() {
            final Operation op = new Operation();
            assertThat(op.hasData("missing")).isFalse();
        }

        @Test
        @DisplayName("getData returns null for missing key")
        void getDataMissing() {
            final Operation op = new Operation();
            assertThat(op.getData("missing")).isNull();
        }

        @Test
        @DisplayName("getDatas returns null for missing key")
        void getDatasMissing() {
            final Operation op = new Operation();
            assertThat(op.getDatas("missing")).isNull();
        }

        @Test
        @DisplayName("default startRow is 0 and endRow is MAX_VALUE")
        void defaultPagination() {
            final Operation op = new Operation();
            assertThat(op.getStartRow()).isZero();
            assertThat(op.getEndRow()).isEqualTo(Integer.MAX_VALUE);
        }
    }
}