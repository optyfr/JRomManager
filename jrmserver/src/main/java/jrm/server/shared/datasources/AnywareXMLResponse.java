package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.misc.Log;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Disk;
import jrm.profile.data.EntityBase;
import jrm.profile.data.MachineList;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.server.shared.datasources.XMLRequest.Operation;

/**
 * Handles XML responses for retrieving detailed information about specific Anyware items (e.g., ROMs, Disks, Samples).
 * <p>
 * This class processes incoming XML requests to fetch metadata for individual entities within a specified Anyware list.
 * It supports filtering by status and cache resetting.
 * </p>
 * 
 * <h2>XML Protocol</h2>
 * <ul>
 *   <li><b>Incoming Request Parameters:</b>
 *     <ul>
 *       <li>{@code list}: The name of the list to query (e.g., "MAME", or "*" for the main MachineList).</li>
 *       <li>{@code ware}: The name of the specific item (machine/software) within the list.</li>
 *       <li>{@code status}: (Optional) Comma-separated list of status strings to filter the entities (e.g., ROMs, Disks).</li>
 *       <li>{@code reset}: (Optional) Boolean flag indicating whether to reset the cache before fetching.</li>
 *     </ul>
 *   </li>
 *   <li><b>Outgoing Response Structure:</b>
 *     <pre><code class="language-xml">
 * &lt;response&gt;
 *   &lt;status&gt;0&lt;/status&gt;
 *   &lt;!-- Multiple record elements for each entity (ROM, Disk, Sample) --&gt;
 *   &lt;record list="..." ware="..." name="..." status="..." type="ROM" size="..." crc="..." md5="..." sha1="..." merge="..." dumpstatus="..."/&gt;
 * &lt;/response&gt;
 *     </code></pre>
 *   </li>
 * </ul>
 *
 * @author JRomManager Team
 * @see XMLResponse
 * @see XMLRequest.Operation
 */
public class AnywareXMLResponse extends XMLResponse {

    /**
     * The XML element/attribute name for the status indicator.
     */
    private static final String STATUS = "status";

    /**
     * Constructs a new AnywareXMLResponse for the given request.
     *
     * @param request The incoming XML request containing operation data.
     * @throws IOException If an I/O error occurs during response generation.
     * @throws XMLStreamException If an XML streaming error occurs.
     */
    public AnywareXMLResponse(XMLRequest request) throws IOException, XMLStreamException {
        super(request);
    }

    /**
     * Retrieves the target AnywareList based on the operation parameters.
     *
     * @param operation The operation containing the "list" parameter.
     * @return The matching AnywareList, or null if not found or not specified.
     */
    private AnywareList<?> getList(Operation operation) {
        String list = operation.getData("list");
        final AnywareList<?> al;
        if (list == null)
            al = null;
        else if (list.equals("*"))
            al = request.session.getCurrProfile().getMachineListList().get(0);
        else
            al = request.session.getCurrProfile().getMachineListList().getSoftwareListList().getByName(list);
        return al;
    }

    /**
     * Retrieves the specific Anyware item from the given list.
     *
     * @param al        The AnywareList to search within.
     * @param operation The operation containing the "ware" parameter.
     * @return The matching Anyware item, or null if the list is null or the item is not found.
     */
    private Anyware getWare(AnywareList<?> al, Operation operation) {
        String ware = operation.getData("ware");
        final Anyware aw;
        if (ware == null || al == null)
            aw = null;
        else
            aw = al.getByName(ware);
        return aw;
    }

    /**
     * Writes a single record for an entity (ROM, Disk, or Sample) to the XML response.
     *
     * @param al The AnywareList containing the entity.
     * @param aw The parent Anyware item.
     * @param e  The specific entity (Rom, Disk, or Sample) to write.
     */
    private void writeRecord(final AnywareList<?> al, final Anyware aw, final EntityBase e) {
        try {
            writer.writeEmptyElement("record");
            writer.writeAttribute("list", al instanceof MachineList ? "*" : al.getBaseName());
            writer.writeAttribute("ware", aw.getBaseName());
            writer.writeAttribute("name", e.getBaseName());
            writer.writeAttribute(STATUS, e.getStatus().toString());
            if (e instanceof Rom r)
                writeRomInfos(r);
            else if (e instanceof Disk dsk)
                writeDiskInfos(dsk);
            else if (e instanceof Sample)
                writer.writeAttribute("type", "SAMPLE");
        } catch (XMLStreamException ex) {
            Log.err(ex.getMessage(), ex);
        }
    }

    /**
     * Writes specific metadata attributes for a Disk entity to the XML response.
     *
     * @param d The Disk entity to write.
     * @throws XMLStreamException If an XML streaming error occurs during writing.
     */
    protected void writeDiskInfos(Disk d) throws XMLStreamException {
        writer.writeAttribute("type", "DISK");
        if (d.getSize() > 0)
            writer.writeAttribute("size", Long.toString(d.getSize()));
        if (d.getCrc() != null)
            writer.writeAttribute("crc", d.getCrc());
        if (d.getMd5() != null)
            writer.writeAttribute("md5", d.getMd5());
        if (d.getSha1() != null)
            writer.writeAttribute("sha1", d.getSha1());
        if (d.getMerge() != null)
            writer.writeAttribute("merge", d.getMerge());
        if (d.getDumpStatus() != null)
            writer.writeAttribute("dumpstatus", d.getDumpStatus().toString());
    }

    /**
     * Writes specific metadata attributes for a ROM entity to the XML response.
     *
     * @param r The ROM entity to write.
     * @throws XMLStreamException If an XML streaming error occurs during writing.
     */
    protected void writeRomInfos(Rom r) throws XMLStreamException {
        writer.writeAttribute("type", "ROM");
        if (r.getSize() > 0)
            writer.writeAttribute("size", Long.toString(r.getSize()));
        if (r.getCrc() != null)
            writer.writeAttribute("crc", r.getCrc());
        if (r.getMd5() != null)
            writer.writeAttribute("md5", r.getMd5());
        if (r.getSha1() != null)
            writer.writeAttribute("sha1", r.getSha1());
        if (r.getMerge() != null)
            writer.writeAttribute("merge", r.getMerge());
        if (r.getDumpStatus() != null)
            writer.writeAttribute("dumpstatus", r.getDumpStatus().toString());
    }

    /**
     * Fetches the list of entities (ROMs, Disks, Samples) for a specific Anyware item and writes them to the XML response.
     * <p>
     * If the {@code reset} parameter is true, the cache of the target Anyware item is reset.
     * The entities are then filtered based on the {@code status} parameter, if provided.
     * </p>
     *
     * @param operation The operation containing request parameters.
     * @throws XMLStreamException If an XML streaming error occurs during writing.
     */
    @Override
    protected void fetch(Operation operation) throws XMLStreamException {
        writer.writeStartElement("response");
        writer.writeElement(STATUS, "0");
        final Set<String> lstatus = operation.hasData(STATUS) ? Stream.of(operation.getData(STATUS).split(",")).collect(Collectors.toSet()) : null;
        final var reset = Boolean.parseBoolean(operation.getData("reset"));
        final AnywareList<?> al = getList(operation);
        final Anyware aw = getWare(al, operation);
        if (aw != null) {
            if (reset)
                aw.resetCache();
            final List<EntityBase> faw = new ArrayList<>();
            for (var i = 0; i < aw.count(); i++) {
                var a = aw.getObject(i);
                if (lstatus != null && !lstatus.contains(a.getStatus().toString()))
                    continue;
                faw.add(a);
            }
            fetchList(operation, faw, (a, _) -> writeRecord(al, aw, a));
        }
        writer.writeEndElement();
    }

}