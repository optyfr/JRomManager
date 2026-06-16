package jrm.server.shared.datasources;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import jrm.batch.Compressor.FileResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

/**
 * XML response handler for batch compressor file results.
 * <p>
 * This class processes XML requests related to the batch compressor's file results, supporting operations such as fetching the
 * cached list, adding new file results, updating existing entries, removing entries, and clearing the entire cache.
 * </p>
 */
public class BatchCompressorFRXMLResponse extends XMLResponse {

    /** XML element name for the result attribute. */
    private static final String RESULT = "result";
    /** XML element name for a record. */
    private static final String RECORD = "record";
    /** XML element name for the status. */
    private static final String STATUS = "status";
    /** XML element name for the response wrapper. */
    private static final String RESPONSE = "response";

    /**
     * Constructs a new batch compressor file result XML response.
     *
     * @param request the XML request containing the operation to process
     * 
     * @throws IOException if an I/O error occurs during initialization
     * @throws XMLStreamException if an XML stream error occurs during initialization
     */
    public BatchCompressorFRXMLResponse(XMLRequest request) throws IOException, XMLStreamException {
        super(request);
    }

    /**
     * Fetches the cached list of compressor file results and writes them to the XML response.
     *
     * @param operation the operation containing request parameters (e.g., pagination)
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void fetch(Operation operation) throws XMLStreamException {
        writer.writeStartElement(RESPONSE);
        writer.writeElement(STATUS, "0");
        writer.writeElement("startRow", "0");
        writer.writeElement("endRow", Integer.toString(request.getSession().getCachedCompressorList().size() - 1));
        writer.writeElement("totalRows", Integer.toString(request.getSession().getCachedCompressorList().size()));
        writer.writeStartElement("data");
        for (final var sr : request.getSession().getCachedCompressorList().entrySet()) {
            writeRecord(writer, sr.getKey(), sr.getValue().getFile(), sr.getValue().getResult());
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    /**
     * Adds a new file result to the session's cached compressor list.
     *
     * @param operation the operation containing the "file" data to add
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void add(Operation operation) throws XMLStreamException {
        if (operation.hasData("file")) {
            String id = UUID.randomUUID().toString();
            FileResult fr = new FileResult(Paths.get(operation.getData("file")));
            request.getSession().getCachedCompressorList().put(id, fr);
            writer.writeStartElement(RESPONSE);
            writer.writeElement(STATUS, "0");
            writer.writeStartElement("data");
            writeRecord(writer, id, fr.getFile(), fr.getResult());
            writer.writeEndElement();
            writer.writeEndElement();
        } else {
            failure("file is missing in request");
        }
    }

    /**
     * Updates an existing file result in the session's cached compressor list.
     *
     * @param operation the operation containing the "id" of the entry to update, and optionally "file" or "result"
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void update(Operation operation) throws XMLStreamException {
        if (!operation.hasData("id")) {
            failure("id is missing in request");
            return;
        }
        final String id = operation.getData("id");
        final FileResult fr = request.getSession().getCachedCompressorList().get(id);
        if (fr == null) {
            failure(id + " not in list");
            return;
        }
        if (!operation.hasData("file") && !operation.hasData(RESULT)) {
            failure("field to update is missing in request");
            return;
        }
        if (operation.hasData("file")) {
            fr.setFile(Paths.get(operation.getData("file")));
        }
        if (operation.hasData(RESULT)) {
            fr.setResult(operation.getData(RESULT));
        }
        writer.writeStartElement(RESPONSE);
        writer.writeElement(STATUS, "0");
        writer.writeStartElement("data");
        writeRecord(writer, id, fr.getFile(), fr.getResult());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    /**
     * Writes a single file result record to the XML stream.
     *
     * @param writer the enhanced XML stream writer
     * @param id the unique identifier of the file result
     * @param file the path to the file
     * @param result the result string associated with the file
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    private void writeRecord(EnhancedXMLStreamWriter writer, String id, Path file, String result) throws XMLStreamException {
        writer.writeElement(RECORD, new SimpleAttribute("id", id), new SimpleAttribute("file", file), new SimpleAttribute(RESULT, result));
    }

    /**
     * Removes a file result from the session's cached compressor list by its ID.
     *
     * @param operation the operation containing the "id" of the entry to remove
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void remove(Operation operation) throws XMLStreamException {
        if (operation.hasData("id")) {
            final String id = operation.getData("id");
            if (request.getSession().getCachedCompressorList().remove(id) != null) {
                writer.writeStartElement(RESPONSE);
                writer.writeElement(STATUS, "0");
                writer.writeStartElement("data");
                writer.writeElement(RECORD, new SimpleAttribute("id", id));
                writer.writeEndElement();
                writer.writeEndElement();
            } else {
                failure(id + " is not in the list");
            }
        } else {
            failure("id is missing in request");
        }
    }

    /**
     * Executes a custom operation. Currently supports the "clear" operation to empty the cached compressor list.
     *
     * @param operation the operation containing the custom operation ID
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void custom(Operation operation) throws XMLStreamException {
        if ("clear".equals(operation.getOperationId().toString())) {
            request.getSession().getCachedCompressorList().clear();
            success();
        }
    }
}
