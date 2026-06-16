package jrm.server.shared.datasources;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import jrm.batch.DirUpdaterResults;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

/**
 * XML response handler for batch DAT to directory update results.
 * <p>
 * This class processes XML requests to retrieve the results of a directory update operation based on a DAT file, providing
 * statistics such as sets found, created, fixed, and missing.
 * </p>
 */
public class BatchDat2DirResultXMLResponse extends XMLResponse {

    /**
     * Constructs a new batch DAT to directory result XML response.
     *
     * @param request the XML request containing the operation to process
     * 
     * @throws IOException if an I/O error occurs during initialization
     * @throws XMLStreamException if an XML stream error occurs during initialization
     */
    public BatchDat2DirResultXMLResponse(XMLRequest request) throws IOException, XMLStreamException {
        super(request);
    }

    /**
     * Fetches the directory updater results and writes them to the XML response.
     * <p>
     * Calculates and outputs statistics including sets found OK, created complete, fixed complete, missing, and total sets based on
     * the provided source path.
     * </p>
     *
     * @param operation the operation containing the "src" data parameter
     * 
     * @throws XMLStreamException if an error occurs while writing the XML stream
     */
    @Override
    protected void fetch(Operation operation) throws XMLStreamException {
        final String src = operation.getData("src");
        final DirUpdaterResults results = src != null ? DirUpdaterResults.load(request.getSession(), pathAbstractor.getAbsolutePath(src).toFile()) : null;
        writer.writeStartElement("response");
        writer.writeElement("status", "0");
        writer.writeElement("startRow", "0");
        writer.writeElement("endRow", Integer.toString((results != null ? results.getResults().size() : 0) - 1));
        writer.writeElement("totalRows", Integer.toString(results != null ? results.getResults().size() : 0));
        writer.writeStartElement("data");
        if (results != null) {
            for (DirUpdaterResult result : results.getResults()) {
                writer.writeElement("record", new SimpleAttribute("src", pathAbstractor.getRelativePath((result.getDat()))),
                        new SimpleAttribute("have", result.getStats().getSetFoundOk()), new SimpleAttribute("create", result.getStats().getSetCreateComplete()),
                        new SimpleAttribute("fix", result.getStats().getSetFoundFixComplete()),
                        new SimpleAttribute("miss",
                                result.getStats().getSetCreate() + result.getStats().getSetFound() + result.getStats().getSetMissing()
                                        - (result.getStats().getSetCreateComplete() + result.getStats().getSetFoundFixComplete() + result.getStats().getSetFoundOk())),
                        new SimpleAttribute("total", result.getStats().getSetCreate() + result.getStats().getSetFound() + result.getStats().getSetMissing()));
            }
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }
}