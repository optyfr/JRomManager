package jrm.profile.scan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.data.Entry.Type;
import jrm.profile.data.Machine;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.DirScan.Options;
import jrm.security.Session;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

public class Dir2Dat
{
	private static final String DIR2_DAT_SAVING = "Dir2Dat.Saving";
	private static final String CDROM = "cdrom";
	private static final String DESCRIPTION = "description";
	private static final String UTF_8 = "UTF-8";
	private Session session;

	public Dir2Dat(final Session session, File srcdir, File dstdat, final ProgressHandler progress, Set<Options> options, ExportType type, Map<String, String> headers)
	{
		this.session = session;
		DirScan srcDirScan = new DirScan(session, srcdir, progress, options);
		write(dstdat, srcDirScan, progress, options, type, headers);
	}

	private void write(final File dstdat, final DirScan scan, final ProgressHandler progress, Set<Options> options, final ExportType type, Map<String, String> headers)
	{
		progress.clearInfos();
		progress.setInfos(1, false);
		AtomicInteger i = new AtomicInteger();
		scan.getContainersIterable().forEach(c -> i.incrementAndGet());
		progress.setProgress(Messages.getString(DIR2_DAT_SAVING), 0, i.get()); // $NON-NLS-1$
		i.set(0);
		try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dstdat)))
		{
			final EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, UTF_8)); // $NON-NLS-1$
			writer.writeStartDocument(UTF_8, "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			switch (type)
			{
				case MAME:
				{
					writeMame(scan, progress, options, i, writer);
					break;
				}
				case DATAFILE:
				{
					writeDataFile(scan, progress, options, headers, i, writer);
					break;
				}
				case SOFTWARELIST:
				{
					writeSoftwareList(scan, progress, options, i, writer);
					break;
				}
			}
			writer.writeEndDocument();
			writer.close();
		}
		catch (FactoryConfigurationError | XMLStreamException | IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	/**
	 * @param scan
	 * @param progress
	 * @param options
	 * @param i
	 * @param writer
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void writeSoftwareList(final DirScan scan, final ProgressHandler progress, Set<Options> options, AtomicInteger i, final EnhancedXMLStreamWriter writer) throws XMLStreamException, IOException
	{
		Map<String, Map<String, AtomicInteger>> slcounter = new HashMap<>();
		Map<String, SL> slmap = new HashMap<>();
		buildSLMap(scan, progress, options, i, slcounter, slmap);
		if (slmap.size() > 1)
		{
			writer.writeDTD("<!DOCTYPE softwarelists [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelists.dtd"), StandardCharsets.UTF_8) + "\n]>" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			writer.writeStartElement("softwarelists"); //$NON-NLS-1$
		}
		else
			writer.writeDTD("<!DOCTYPE softwarelist [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelist.dtd"), StandardCharsets.UTF_8) + "\n]>" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		for (final var e : slmap.entrySet())
			writeSoftwareList(writer, e, options);
		if (slmap.size() > 1)
			writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param sl
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeSoftwareList(final EnhancedXMLStreamWriter writer, final java.util.Map.Entry<String, SL> sl, Set<Options> options) throws XMLStreamException
	{
		writer.writeStartElement("softwarelist", new SimpleAttribute("name", sl.getValue().name)); // $NON-NLS-1$ //$NON-NLS-2$
		if (sl.getValue().softwarelist != null)
			writer.writeElement(DESCRIPTION, sl.getValue().softwarelist.getDescription()); // $NON-NLS-1$
		for (final var ee : sl.getValue().sw.entrySet())
		{
			if (ee.getValue().software != null)
				ee.getValue().software.export(writer, ee.getValue().container.getEntries());
			else
			{
				writer.writeStartElement("software", new SimpleAttribute("name", ee.getValue().name)); //$NON-NLS-1$ //$NON-NLS-2$
				final var ii = new AtomicInteger();
				for (Entry entry : ee.getValue().container.getEntries())
				{
					if (entry.getType() == Type.CHD)
						writeSWCHD(writer, entry, ii, options);
					else
						writeSWRom(writer, entry, ii, options);
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param entry
	 * @param ii
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeSWRom(final EnhancedXMLStreamWriter writer, Entry entry, final AtomicInteger ii, Set<Options> options) throws XMLStreamException
	{
		String ename = normalize(entry.getName());
		if (options.contains(Options.JUNK_SUBFOLDERS))
		{
			Path path = Paths.get(ename);
			Path fileName = path.getFileName();
			if (fileName != null)
				ename = fileName.toString();
		}
		writer.writeStartElement("part", //$NON-NLS-1$
				new SimpleAttribute("name", "flop" + ii.incrementAndGet()), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("interface", "floppy_3_5") //$NON-NLS-1$ //$NON-NLS-2$
		);
		writer.writeStartElement("dataarea", //$NON-NLS-1$
				new SimpleAttribute("name", "flop"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("size", entry.getSize()) //$NON-NLS-1$
		);
		writer.writeElement("rom", //$NON-NLS-1$
				new SimpleAttribute("name", ename), //$NON-NLS-1$
				new SimpleAttribute("size", entry.getSize()), //$NON-NLS-1$
				new SimpleAttribute("crc", entry.getCrc()), //$NON-NLS-1$
				new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1) ? entry.getSha1() : null) //$NON-NLS-1$
		);
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param entry
	 * @param ii
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeSWCHD(final EnhancedXMLStreamWriter writer, Entry entry, final AtomicInteger ii, Set<Options> options) throws XMLStreamException
	{
		String ename = normalize(FilenameUtils.removeExtension(entry.getName()));
		if (options.contains(Options.JUNK_SUBFOLDERS))
		{
			Path path = Paths.get(ename);
			Path fileName = path.getFileName();
			if (fileName != null)
				ename = fileName.toString();
		}
		writer.writeStartElement("part", //$NON-NLS-1$
				new SimpleAttribute("name", CDROM + ii.incrementAndGet()), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("interface", CDROM) //$NON-NLS-1$ //$NON-NLS-2$
		);
		writer.writeStartElement("diskarea", //$NON-NLS-1$
				new SimpleAttribute("name", CDROM) //$NON-NLS-1$ //$NON-NLS-2$
		);
		writer.writeElement("disk", //$NON-NLS-1$
				new SimpleAttribute("name", ename), //$NON-NLS-1$
				new SimpleAttribute("sha1", entry.getSha1()) //$NON-NLS-1$
		);
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * @param scan
	 * @param progress
	 * @param options
	 * @param i
	 * @param slcounter
	 * @param slmap
	 */
	private void buildSLMap(final DirScan scan, final ProgressHandler progress, Set<Options> options, AtomicInteger i, Map<String, Map<String, AtomicInteger>> slcounter, Map<String, SL> slmap)
	{
		for (Container c : scan.getContainersIterable())
		{
			progress.setProgress(Messages.getString(DIR2_DAT_SAVING), i.incrementAndGet()); // $NON-NLS-1$
			final Path relativized = scan.getDir().toPath().relativize(c.getFile().toPath());
			final Path filename = relativized.getFileName();
			final Path parent = relativized.getParent();
			if (filename == null || parent == null)
				continue;
			final var swname = new StringBuilder(FilenameUtils.removeExtension(filename.toString()));
			final var slname = new StringBuilder(parent.toString());
			final var software = buildSLMapNames(swname, slname, options);
			final var swcounter = slcounter.computeIfAbsent(slname.toString(), k -> new HashMap<>());
			final var sl = slmap.computeIfAbsent(slname.toString(), k -> new SL(k, software != null ? software.getSl() : null));
			final var val = swcounter.computeIfAbsent(swname.toString(), k -> new AtomicInteger());
			if (val.incrementAndGet() > 1)
				swname.append("_" + val.get()); //$NON-NLS-1$
			sl.sw.put(swname.toString(), new SL.SW(swname.toString(), software, c));
		}
	}

	/**
	 * @param swname
	 * @param slname
	 * @param options
	 * @return
	 */
	private Software buildSLMapNames(final StringBuilder swname, final StringBuilder slname, Set<Options> options)
	{
		final Software software;
		if (session.getCurrProfile() != null)
		{
			SoftwareList sl = session.getCurrProfile().getMachineListList().getSoftwareListList().getByName(slname.toString());
			if (sl != null && sl.containsName(swname.toString()))
				software = sl.getByName(swname.toString());
			else
				software = null;
			if (software != null && options.contains(Options.MATCH_PROFILE))
			{
				swname.setLength(0);
				swname.append(software.getBaseName());
				slname.setLength(0);
				slname.append(software.getSl().getBaseName());
			}
		}
		else
			software = null;
		return software;
	}

	/**
	 * @param scan
	 * @param progress
	 * @param options
	 * @param headers
	 * @param i
	 * @param writer
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void writeDataFile(final DirScan scan, final ProgressHandler progress, Set<Options> options, Map<String, String> headers, AtomicInteger i, final EnhancedXMLStreamWriter writer) throws XMLStreamException, IOException
	{
		writer.writeDTD("<!DOCTYPE datafile [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/datafile.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																																												// //$NON-NLS-4$
		writer.writeStartElement("datafile"); //$NON-NLS-1$
		writer.writeStartElement("header"); //$NON-NLS-1$
		for (Map.Entry<String, String> entry : headers.entrySet())
			writer.writeElement(entry.getKey(), entry.getValue());
		writer.writeEndElement();
		Map<String, AtomicInteger> counter = new HashMap<>();
		for (Container container : scan.getContainersIterable())
		{
			progress.setProgress(Messages.getString(DIR2_DAT_SAVING), i.incrementAndGet()); // $NON-NLS-1$
			writeDataFile(writer, container, counter, options);
		}
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param container
	 * @param counter
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeDataFile(final EnhancedXMLStreamWriter writer, Container container, Map<String, AtomicInteger> counter, Set<Options> options) throws XMLStreamException
	{
		String name = FilenameUtils.removeExtension(container.getFile().getName());
		final var machine = (session.getCurrProfile() != null && options.contains(Options.MATCH_PROFILE)) ? session.getCurrProfile().getMachineListList().get(0).getByName(name) : null;
		if (machine != null)
			name = machine.getBaseName();
		final var val = counter.computeIfAbsent(name, n -> new AtomicInteger());
		if (val.incrementAndGet() > 1)
			name = name + "_" + val.get(); //$NON-NLS-1$
		writer.writeStartElement("game", //$NON-NLS-1$
				new SimpleAttribute("name", name), //$NON-NLS-1$
				new SimpleAttribute("isbios", Optional.ofNullable(machine).filter(Machine::isBios).map(m -> "yes").orElse(null)), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("cloneof", Optional.ofNullable(machine).map(Machine::getCloneof).orElse(null)), //$NON-NLS-1$
				new SimpleAttribute("romof", Optional.ofNullable(machine).map(Machine::getRomof).orElse(null)), //$NON-NLS-1$
				new SimpleAttribute("sampleof", Optional.ofNullable(machine).map(Machine::getSampleof).orElse(null)) //$NON-NLS-1$
		);
		writer.writeElement(DESCRIPTION, machine != null ? machine.description : name); // $NON-NLS-1$
		writer.writeElement("year", machine != null ? machine.year : "????"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.writeElement("manufacturer", machine != null ? machine.manufacturer : ""); //$NON-NLS-1$ //$NON-NLS-2$
		for (Entry entry : container.getEntries())
		{
			if (entry.getType() == Type.CHD)
				writeDataFileCHD(writer, entry, options);
			else
				writeDataFileRom(writer, entry, options);
		}
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param entry
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeDataFileRom(final EnhancedXMLStreamWriter writer, Entry entry, Set<Options> options) throws XMLStreamException
	{
		String ename = normalize(entry.getName());
		if (options.contains(Options.JUNK_SUBFOLDERS))
		{
			Path path = Paths.get(ename);
			Path fileName = path.getFileName();
			if (fileName != null)
				ename = fileName.toString();
		}
		writer.writeElement("rom", //$NON-NLS-1$
				new SimpleAttribute("name", ename), //$NON-NLS-1$
				new SimpleAttribute("size", entry.getSize()), //$NON-NLS-1$
				new SimpleAttribute("crc", entry.getCrc()), //$NON-NLS-1$
				new SimpleAttribute("md5", options.contains(Options.NEED_MD5) ? entry.getMd5() : null), //$NON-NLS-1$
				new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1) ? entry.getSha1() : null), //$NON-NLS-1$
				new SimpleAttribute("offset", 0), //$NON-NLS-1$
				new SimpleAttribute("date", entry.getModified()) //$NON-NLS-1$
		);
	}

	/**
	 * @param writer
	 * @param entry
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeDataFileCHD(final EnhancedXMLStreamWriter writer, Entry entry, Set<Options> options) throws XMLStreamException
	{
		String ename = normalize(FilenameUtils.removeExtension(entry.getName()));
		if (options.contains(Options.JUNK_SUBFOLDERS))
		{
			Path path = Paths.get(ename);
			Path fileName = path.getFileName();
			if (fileName != null)
				ename = fileName.toString();
		}
		writer.writeElement("disk", //$NON-NLS-1$
				new SimpleAttribute("name", ename), //$NON-NLS-1$
				new SimpleAttribute("md5", entry.getMd5()), //$NON-NLS-1$
				new SimpleAttribute("sha1", entry.getSha1()) //$NON-NLS-1$
		);
	}

	/**
	 * @param scan
	 * @param progress
	 * @param options
	 * @param i
	 * @param writer
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void writeMame(final DirScan scan, final ProgressHandler progress, Set<Options> options, AtomicInteger i, final EnhancedXMLStreamWriter writer) throws XMLStreamException, IOException
	{
		writer.writeDTD("<!DOCTYPE mame [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/mame.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																																										// //$NON-NLS-4$
		writer.writeStartElement("mame"); //$NON-NLS-1$
		Map<String, AtomicInteger> counter = new HashMap<>();
		for (Container c : scan.getContainersIterable())
		{
			progress.setProgress(Messages.getString(DIR2_DAT_SAVING), i.incrementAndGet()); // $NON-NLS-1$
			writeMame(writer, c, counter, options);
		}
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param container
	 * @param counter
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeMame(final EnhancedXMLStreamWriter writer, Container container, Map<String, AtomicInteger> counter, Set<Options> options) throws XMLStreamException
	{
		String name = FilenameUtils.removeExtension(container.getFile().getName());
		Machine machine = (session.getCurrProfile() != null && options.contains(Options.MATCH_PROFILE)) ? session.getCurrProfile().getMachineListList().get(0).getByName(name) : null;
		if (machine != null)
			name = machine.getBaseName();
		final var val = counter.computeIfAbsent(name, n -> new AtomicInteger());
		if (val.incrementAndGet() > 1)
			name = name + "_" + val.get(); //$NON-NLS-1$
		writer.writeStartElement("machine", //$NON-NLS-1$
				new SimpleAttribute("name", name), //$NON-NLS-1$
				new SimpleAttribute("isbios", Optional.ofNullable(machine).filter(Machine::isBios).map(m -> "yes").orElse(null)), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("isdevice", Optional.ofNullable(machine).filter(Machine::isIsdevice).map(m -> "yes").orElse(null)), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("ismechanical", Optional.ofNullable(machine).filter(Machine::isIsmechanical).map(m -> "yes").orElse(null)), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleAttribute("cloneof", Optional.ofNullable(machine).map(Machine::getCloneof).orElse(null)), //$NON-NLS-1$
				new SimpleAttribute("romof", Optional.ofNullable(machine).map(Machine::getRomof).orElse(null)), //$NON-NLS-1$
				new SimpleAttribute("sampleof", Optional.ofNullable(machine).map(Machine::getSampleof).orElse(null)) //$NON-NLS-1$
		);
		writer.writeElement(DESCRIPTION, machine != null ? machine.description : name); // $NON-NLS-1$
		writer.writeElement("year", machine != null ? machine.year : "????"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.writeElement("manufacturer", machine != null ? machine.manufacturer : ""); //$NON-NLS-1$ //$NON-NLS-2$
		for (Entry e : container.getEntries())
		{
			if (e.getType() == Type.CHD)
				writeDataFileCHD(writer, e, options);
			else
				writeMameRom(writer, e, options);
		}
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param entry
	 * @param options
	 * @throws XMLStreamException
	 */
	private void writeMameRom(final EnhancedXMLStreamWriter writer, Entry entry, Set<Options> options) throws XMLStreamException
	{
		String ename = normalize(entry.getName());
		if (options.contains(Options.JUNK_SUBFOLDERS))
		{
			Path path = Paths.get(ename);
			Path fileName = path.getFileName();
			if (fileName != null)
				ename = fileName.toString();
		}
		writer.writeElement("rom", //$NON-NLS-1$
				new SimpleAttribute("name", ename), //$NON-NLS-1$
				new SimpleAttribute("size", entry.getSize()), //$NON-NLS-1$
				new SimpleAttribute("crc", entry.getCrc()), //$NON-NLS-1$
				new SimpleAttribute("md5", options.contains(Options.NEED_MD5) ? entry.getMd5() : null), //$NON-NLS-1$
				new SimpleAttribute("sha1", options.contains(Options.NEED_SHA1) ? entry.getSha1() : null) //$NON-NLS-1$
		);
	}

	private static class SL
	{
		private String name;
		private SoftwareList softwarelist = null;
		private Map<String, SW> sw = new HashMap<>();

		private static class SW
		{
			private String name;
			private Software software = null;
			private Container container = null;

			private SW(String name, Software software, Container container)
			{
				this.name = name;
				this.software = software;
				this.container = container;
			}
		}

		private SL(String name, SoftwareList softwarelist)
		{
			this.name = name;
			this.softwarelist = softwarelist;
		}
	}

	/**
	 * Normalize char separator according platform default separator
	 * 
	 * @param entry
	 *            the entry to normalize
	 * @return the normalized entry
	 */
	private String normalize(final String entry)
	{
		if (File.separatorChar == '/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}
}
