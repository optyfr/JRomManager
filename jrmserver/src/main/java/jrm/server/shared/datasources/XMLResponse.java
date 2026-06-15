package jrm.server.shared.datasources;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.Range;

import jrm.misc.IOUtils;
import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * Abstract base class for generating structured XML responses in the server datasources subsystem.
 * Provides common infrastructure for XML serialization including temporary file handling,
 * stream management, pagination support, and standardized error response formatting.
 * <p>
 * Subclasses extend this implementation to define specific XML output structures for different
 * datasource operations such as fetching data lists, adding/removing records, or executing
 * custom business logic operations. The class manages an internal buffered output stream
 * writing to a temporary file, allowing full XML document construction before transmission.
 * </p>
 *
 * @see XMLRequest
 * @see EnhancedXMLStreamWriter
 * @see TempFileInputStream
 */
public abstract class XMLResponse implements Closeable {
    /** Constant representing the total rows element name in paginated responses. */
    private static final String TOTAL_ROWS = "totalRows";
    /** Constant representing the end row element name in paginated responses. */
    private static final String END_ROW = "endRow";
    /** Constant representing the start row element name in paginated responses. */
    private static final String START_ROW = "startRow";
    /** Constant representing the status element name in response envelopes. */
    private static final String STATUS = "status";
    /** Constant representing the response root element name. */
    protected static final String RESPONSE = "response";
    /** The parsed XML request containing session context and operation parameters. */
    protected XMLRequest request;
    /** Temporary file used to buffer the complete XML response before transmission. */
    private final Path tmpfile;
    /** Buffered output stream wrapping the temporary file for efficient XML writing. */
    private final OutputStream out;
    /** Enhanced XML stream writer providing formatted XML output capabilities. */
    protected final EnhancedXMLStreamWriter writer;
    /** Path abstraction layer for translating filesystem paths within the security sandbox context. */
    protected PathAbstractor pathAbstractor;

    /**
     * Constructs an XML response bound to the provided request.
     * Initializes the underlying XML writer with UTF-8 encoding, creates a temporary file buffer,
     * establishes the path abstraction layer, and writes the XML declaration header.
     *
     * @param request the incoming XML request containing session and operation metadata
     * @throws IOException if an I/O error occurs during temporary file creation or output stream initialization
     * @throws XMLStreamException if an error occurs while initializing the XML stream writer or writing the document declaration
     */
    protected XMLResponse(XMLRequest request) throws IOException, XMLStreamException {
        this.request = request;
        pathAbstractor = new PathAbstractor(request.getSession());
        tmpfile = IOUtils.createTempFile("JRM", null);
        out = new BufferedOutputStream(Files.newOutputStream(tmpfile));
        writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(out));
        writer.writeStartDocument("utf-8", "1.0");
    }

    /**
     * Dispatches the given operation to its corresponding handler method based on the operation type.
     * Routes {@code fetch}, {@code add}, {@code update}, {@code remove}, and {@code custom} operations
     * to their respective implementations. Unrecognized operation types trigger a failure response.
     *
     * @param operation the parsed operation to execute, determining which handler method to invoke
     * @throws XMLStreamException if an error occurs while writing an error response for unimplemented operations
     * @throws IOException if an I/O error occurs during operation processing
     */
    private void processOperation(Operation operation) throws XMLStreamException, IOException {
        switch (operation.getOperationType().toString()) {
            case "fetch" -> fetch(operation);
            case "add" -> add(operation);
            case "update" -> update(operation);
            case "remove" -> remove(operation);
            case "custom" -> custom(operation);
            default -> failure(operation.getOperationType() + " not implemented");
        }
    }

    /**
     * Processes the complete request by executing the associated operation(s) and returning
     * a temporary file input stream containing the fully constructed XML response.
     * <p>
     * If the request contains a transaction with multiple operations, each operation is processed sequentially
     * within a wrapping {@code <responses>} element. Otherwise, the single operation is processed directly
     * without transaction wrapping. After processing, the XML writer and output stream are flushed
     * to ensure all content is written to the temporary file.
     * </p>
     *
     * @return a {@link TempFileInputStream} pointing to the temporary file containing the serialized XML response
     * @throws XMLStreamException if an error occurs while writing the XML structure or elements
     * @throws IOException if an I/O error occurs during temporary file operations or stream flushing
     */
    public TempFileInputStream processRequest() throws XMLStreamException, IOException {
        if (request.getTransaction() != null) {
            writer.writeStartElement("responses");
            for (Operation operation : request.getTransaction().getOperations())
                processOperation(operation);
            writer.writeEndElement();
        } else
            processOperation(request.getOperation());
        writer.flush();
        out.flush();
        return new TempFileInputStream(tmpfile.toFile());
    }

    /**
     * Default implementation for the fetch operation, immediately triggering a failure response indicating
     * that the fetch operation is not implemented. Subclasses must override this method to provide
     * actual data retrieval and serialization logic.
     *
     * @param operation the fetch operation containing pagination and filtering parameters
     * @throws XMLStreamException always thrown as this operation is not implemented in the base class
     * @throws IOException always thrown as this operation is not implemented in the base class
     */
    protected void fetch(Operation operation) throws XMLStreamException, IOException // NOSONAR
    {
        failure("fetch operation not implemented");
    }

    /**
     * Default implementation for the add operation, immediately triggering a failure response indicating
     * that the add operation is not implemented. Subclasses must override this method to provide
     * actual record creation and persistence logic.
     *
     * @param operation the add operation containing the data payload for the new record
     * @throws XMLStreamException always thrown as this operation is not implemented in the base class
     * @throws IOException always thrown as this operation is not implemented in the base class
     */
    protected void add(Operation operation) throws XMLStreamException, IOException // NOSONAR
    {
        failure("add operation not implemented");
    }

    /**
     * Default implementation for the update operation, immediately triggering a failure response indicating
     * that the update operation is not implemented. Subclasses must override this method to provide
     * actual record modification and persistence logic.
     *
     * @param operation the update operation containing the target identifier and modified field values
     * @throws XMLStreamException always thrown as this operation is not implemented in the base class
     * @throws IOException always thrown as this operation is not implemented in the base class
     */
    protected void update(Operation operation) throws XMLStreamException, IOException // NOSONAR
    {
        failure("update operation not implemented");
    }

    /**
     * Default implementation for the remove (delete) operation, immediately triggering a failure response indicating
     * that the delete operation is not implemented. Subclasses must override this method to provide
     * actual record removal and cleanup logic.
     *
     * @param operation the remove operation containing the target identifier of the record to delete
     * @throws XMLStreamException always thrown as this operation is not implemented in the base class
     * @throws IOException always thrown as this operation is not implemented in the base class
     */
    protected void remove(Operation operation) throws XMLStreamException, IOException // NOSONAR
    {
        failure("delete operation not implemented");
    }

    /**
     * Default implementation for custom operations, immediately triggering a failure response indicating
     * that the custom operation is not implemented. Subclasses may override this method to handle
     * specialized operation types not covered by the standard CRUD operations.
     *
     * @param operation the custom operation whose type determines the specialized processing logic
     * @throws XMLStreamException always thrown as this operation is not implemented in the base class
     * @throws IOException always thrown as this operation is not implemented in the base class
     */
    protected void custom(Operation operation) throws XMLStreamException, IOException // NOSONAR
    {
        failure("custom operation not implemented");
    }

    /**
     * Closes the XML response by writing the closing document tag, terminating the XML stream writer,
     * and closing the underlying output stream. Ensures proper cleanup regardless of exceptions
     * encountered during the close operation, logging any XML stream errors.
     *
     * @throws IOException if an I/O error occurs while closing the output stream
     */
    @Override
    public void close() throws IOException {
        try {
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException e) {
            Log.err(e.getMessage(), e);
        } finally {
            out.close();
        }
    }

    /**
     * Writes a standard XML response envelope containing only a status code with no additional data.
     * The envelope includes a {@code <response>} root element with a nested {@code <status>} element
     * set to the specified integer value.
     *
     * @param status the integer status code to include in the response (e.g., 0 for success, negative values for errors)
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void error(int status) throws XMLStreamException {
        writer.writeStartElement(RESPONSE);
        writer.writeElement(STATUS, Integer.toString(status));
        writer.writeEndElement();
    }

    /**
     * Writes a standard XML response envelope containing a status code and a simple data string.
     * The envelope includes a {@code <response>} root element with a nested {@code <status>} element
     * and a {@code <data>} element containing the provided text content.
     *
     * @param status the integer status code to include in the response
     * @param data the string data to include alongside the status code
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void error(int status, String data) throws XMLStreamException {
        writer.writeStartElement(RESPONSE);
        writer.writeElement(STATUS, Integer.toString(status));
        writer.writeElement("data", data);
        writer.writeEndElement();
    }

    /**
     * Writes a standard XML response envelope containing a status code and structured validation errors.
     * The envelope includes a {@code <response>} root element with a nested {@code <status>} element
     * and optionally a {@code <errors>} container. When present, the {@code <errors>} element groups
     * error fields by their key names, with each field containing one or more {@code <errorMessage>} entries.
     *
     * @param status the integer status code to include in the response
     * @param data a map of error field names to lists of error message strings; may be null to omit error details
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void error(int status, Map<String, List<String>> data) throws XMLStreamException {
        writer.writeStartElement(RESPONSE);
        writer.writeElement(STATUS, Integer.toString(status));
        if (data != null) {
            writer.writeStartElement("errors");
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                writer.writeStartElement(entry.getKey());
                for (String msg : entry.getValue())
                    writer.writeElement("errorMessage", msg);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Convenience method that writes a success response with a zero status code.
     * Equivalent to calling {@code error(0)}.
     *
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void noError() throws XMLStreamException {
        error(0);
    }

    /**
     * Convenience method that writes a success response with a zero status code.
     * Equivalent to calling {@code error(0)}. Provided as an alternative semantic name.
     *
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void success() throws XMLStreamException {
        error(0);
    }

    /**
     * Writes a generic failure response with a status code of -1 and the provided error message.
     * Used for reporting unexpected conditions, unsupported features, or runtime errors.
     *
     * @param msg the descriptive error message explaining the failure condition
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void otherError(String msg) throws XMLStreamException {
        error(-1, msg);
    }

    /**
     * Writes a generic failure response with a status code of -1 and the provided error message.
     * An alias for {@link #otherError(String)} emphasizing that the failure indicates
     * an unimplemented or missing feature.
     *
     * @param msg the descriptive error message explaining the failure condition
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void failure(String msg) throws XMLStreamException {
        error(-1, msg);
    }

    /**
     * Writes a generic failure response with a status code of -1 and no accompanying message.
     * Used when the failure reason is self-evident from context or unnecessary to expose.
     *
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void failure() throws XMLStreamException {
        error(-1);
    }

    /**
     * Writes an authentication failure response with a status code of -5.
     * Indicates that the provided credentials were invalid or that the current session token is expired.
     *
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void loginIncorrect() throws XMLStreamException {
        error(-5);
    }

    /**
     * Writes an authentication required response with a status code of -7.
     * Indicates that the requested operation requires a valid authenticated session,
     * and no active session was found in the current request context.
     *
     * @throws XMLStreamException if an error occurs while writing the XML elements
     */
    protected void loginRequired() throws XMLStreamException {
        error(-7);
    }

    /**
     * Functional interface defining a callback for serializing individual array elements
     * during paginated list operations. Implementations receive the current element index
     * and the total element count, enabling position-aware serialization logic.
     */
    @FunctionalInterface
    protected interface FetchArrayCallback {
        /**
         * Serializes a single array element into the XML output stream.
         *
         * @param idx the zero-based index of the element within the entire collection
         * @param count the total number of elements in the collection
         * @throws XMLStreamException if an error occurs while writing XML content
         */
        public void apply(int idx, int count) throws XMLStreamException;
    }

    /**
     * Serializes a range of array elements as a paginated data block in the XML response.
     * Calculates the effective start and end indices clamped to available bounds, writes
     * pagination metadata (start row, end row, total rows), and invokes the callback
     * for each element in the specified range wrapped in a {@code <data>} element.
     *
     * @param operation the operation containing pagination parameters (start row and end row)
     * @param count the total number of available elements to paginate through
     * @param cb the callback invoked for each serialized element within the selected range
     * @throws XMLStreamException if an error occurs while writing the XML structure or invoking the callback
     */
    protected void fetchArray(Operation operation, int count, FetchArrayCallback cb) throws XMLStreamException {
        final int start = Math.min(count - 1, operation.getStartRow());
        final int end = Math.min(count - 1, operation.getEndRow());
        writer.writeElement(START_ROW, Integer.toString(start));
        writer.writeElement(END_ROW, Integer.toString(end));
        writer.writeElement(TOTAL_ROWS, Integer.toString(count));
        writer.writeStartElement("data");
        if (count > 0)
            for (int i = start; i <= end; i++)
                cb.apply(i, count);
        writer.writeEndElement();
    }

    /**
     * Functional interface defining a callback for serializing individual objects from a typed list
     * during paginated operations. Implementations receive the object instance and its index
     * within the collection, enabling object-aware serialization logic.
     *
     * @param <T> the type of objects in the list being serialized
     */
    @FunctionalInterface
    protected interface FetchListCallback<T> {
        /**
         * Serializes a single list element into the XML output stream.
         *
         * @param obj the object to serialize from the list
         * @param idx the zero-based index of the object within the list
         * @throws XMLStreamException if an error occurs while writing XML content
         */
        public void apply(T obj, int idx) throws XMLStreamException;
    }

    /**
     * Serializes a range of typed list elements as a paginated data block in the XML response.
     * Calculates the effective start and end indices clamped to available bounds, writes
     * pagination metadata (start row, end row, total rows), retrieves each element from the list
     * by index, and invokes the callback for serialization within a {@code <data>} element.
     *
     * @param <T> the type of objects in the list being serialized
     * @param operation the operation containing pagination parameters (start row and end row)
     * @param list the typed list of objects to paginate through and serialize
     * @param cb the callback invoked for each serialized element within the selected range
     * @throws XMLStreamException if an error occurs while writing the XML structure or invoking the callback
     */
    protected <T> void fetchList(Operation operation, List<T> list, FetchListCallback<T> cb) throws XMLStreamException {
        final int count = list.size();
        final int start = Math.min(count - 1, operation.getStartRow());
        final int end = Math.min(count - 1, operation.getEndRow());
        writer.writeElement(START_ROW, Integer.toString(start));
        writer.writeElement(END_ROW, Integer.toString(end));
        writer.writeElement(TOTAL_ROWS, Integer.toString(count));
        writer.writeStartElement("data");
        if (count > 0)
            for (int idx = start; idx <= end; idx++)
                cb.apply(list.get(idx), idx);
        writer.writeEndElement();
    }

    /**
     * Functional interface defining a callback for processing individual elements from a stream
     * during range-filtered operations. Unlike array/list callbacks, this interface does not receive
     * indexing information since streams do not guarantee random access.
     *
     * @param <T> the type of objects in the stream being processed
     */
    @FunctionalInterface
    protected interface FetchStreamCallback<T> {
        /**
         * Processes a single stream element for serialization or transformation.
         *
         * @param t the object to process from the stream
         */
        public void apply(T t);
    }

    /**
     * Filters a stream to a specified index range using {@link Range} matching and serializes
     * the filtered results into a paginated data block in the XML response. Unlike array and list
     * variants, this method does not write pagination metadata headers upfront; instead, it calculates
     * and appends the start row, end row, and total rows after processing the entire stream.
     * <p>
     * The method uses a counters-based approach where {@code Range.contains()} validates whether each
     * successive element falls within the requested index range defined by the operation parameters.
     * Only matching elements pass the filter and reach the callback invocation via {@code forEachOrdered}.
     * </p>
     *
     * @param <T> the type of objects in the stream being processed
     * @param operation the operation containing range parameters (start row and end row) defining the inclusive index boundaries
     * @param stream the source stream of objects to filter and serialize
     * @param cb the callback invoked for each element whose index falls within the specified range
     * @throws XMLStreamException if an error occurs while writing the XML structure or invoking the callback
     */
    protected <T> void fetchStream(Operation operation, Stream<T> stream, FetchStreamCallback<T> cb) throws XMLStreamException {
        final int start = operation.getStartRow();
        final int end = operation.getEndRow();
        final var range = Range.of(start, end);
        final int[] count = { 0 };
        writer.writeStartElement("data");
        stream.filter(_ -> range.contains(++count[0])).forEachOrdered(cb::apply);
        writer.writeEndElement();
        writer.writeElement(START_ROW, Integer.toString(Math.min(count[0] - 1, start)));
        writer.writeElement(END_ROW, Integer.toString(Math.min(count[0] - 1, end)));
        writer.writeElement(TOTAL_ROWS, Integer.toString(count[0]));
    }

}
