package jrm.profile;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profile.data.*;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.SWList;
import jrm.profile.data.Machine.SWStatus;
import jrm.profile.data.Rom.LoadFlag;
import jrm.profile.data.Software.Part;
import jrm.profile.data.Software.Part.DataArea;
import jrm.profile.data.Software.Part.DataArea.Endianness;
import jrm.profile.data.Software.Part.DiskArea;
import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.SubCategory;
import jrm.profile.filter.NPlayers;
import jrm.profile.filter.NPlayers.NPlayer;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public class Profile implements Serializable
{

	public long machines_cnt = 0;
	public long softwares_list_cnt = 0;
	public long softwares_cnt = 0;
	public long roms_cnt = 0, swroms_cnt = 0;
	public long disks_cnt = 0, swdisks_cnt = 0;
	public long samples_cnt = 0;

	public boolean md5_roms = false;
	public boolean md5_disks = false;
	public boolean sha1_roms = false;
	public boolean sha1_disks = false;

	public String build = null;
	public final HashMap<String, StringBuffer> header = new HashMap<>();
	public final MachineListList machinelist_list = new MachineListList();
	public final HashSet<String> suspicious_crc = new HashSet<>();

	public transient Properties settings = null;
	public transient Systms systems = null;
	public transient Collection<String> years = null;
	public transient ProfileNFO nfo = null;
	public transient CatVer catver = null;
	public transient NPlayers nplayers = null;

	public static transient Profile curr_profile;

	private Profile()
	{

	}

	public boolean _load(final File file, final ProgressHandler handler)
	{
		handler.setProgress(String.format(Messages.getString("Profile.Parsing"), file), -1); //$NON-NLS-1$
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try(InputStream in = handler.getInputStream(new FileInputStream(file), (int)file.length()))
		{
			final SAXParser parser = factory.newSAXParser();
			parser.parse(in, new DefaultHandler()
			{
				private final HashMap<String, Rom> roms_bycrc = new HashMap<>();
				private boolean in_description = false;
				private boolean in_year = false;
				private boolean in_manufacturer = false;
				private boolean in_publisher = false;
				private boolean in_header = false;
				private boolean in_cabinet_dipsw = false;
				private final EnumSet<CabinetType> cabtype_set = EnumSet.noneOf(CabinetType.class);
				private SoftwareList curr_software_list = null;
				private Software curr_software = null;
				private Software.Part curr_part = null;
				private Software.Part.DataArea curr_dataarea = null;
				private Software.Part.DiskArea curr_diskarea = null;
				private Machine curr_machine = null;
				private Samples curr_sampleset = null;
				private Rom curr_rom = null;
				private Disk curr_disk = null;
				private final HashSet<String> roms = new HashSet<>();
				private final HashSet<String> disks = new HashSet<>();

				private String curr_tag;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
				{
					curr_tag = qName;
					if (qName.equals("mame") || qName.equals("datafile")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "build": //$NON-NLS-1$
									build = attributes.getValue(i);
							}
						}
					}
					else if (qName.equals("header")) //$NON-NLS-1$
					{
						in_header = true;
					}
					else if (qName.equals("softwarelist") && curr_machine == null) //$NON-NLS-1$
					{
						curr_software_list = new SoftwareList();
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software_list.setName(attributes.getValue(i).trim());
									machinelist_list.softwarelist_list.putByName(curr_software_list);
									break;
								case "description": //$NON-NLS-1$
									curr_software_list.description.append(attributes.getValue(i).trim());
									break;
							}
						}
					}
					else if (qName.equals("softwarelist")) //$NON-NLS-1$
					{
						final SWList swlist = curr_machine.new SWList();
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									swlist.name = attributes.getValue(i);
									break;
								case "status": //$NON-NLS-1$
									swlist.status = SWStatus.valueOf(attributes.getValue(i));
									break;
								case "filter": //$NON-NLS-1$
									swlist.filter = attributes.getValue(i);
									break;
							}
						}
						curr_machine.swlists.put(swlist.name, swlist);
						if (!machinelist_list.softwarelist_defs.containsKey(swlist.name))
							machinelist_list.softwarelist_defs.put(swlist.name, new ArrayList<>());
						machinelist_list.softwarelist_defs.get(swlist.name).add(curr_machine);
					}
					else if (qName.equals("software")) //$NON-NLS-1$
					{
						curr_software = new Software();
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software.setName(attributes.getValue(i).trim());
									break;
								case "cloneof": //$NON-NLS-1$
									curr_software.cloneof = attributes.getValue(i);
									break;
								case "supported": //$NON-NLS-1$
									curr_software.supported = Software.Supported.valueOf(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("feature") && curr_software != null) //$NON-NLS-1$
					{
						if (attributes.getValue("name").equalsIgnoreCase("compatibility")) //$NON-NLS-1$ //$NON-NLS-2$
							curr_software.compatibility = attributes.getValue("value"); //$NON-NLS-1$
					}
					else if (qName.equals("part") && curr_software != null) //$NON-NLS-1$
					{
						curr_software.parts.add(curr_part = new Part());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_part.name = attributes.getValue(i).trim();
									break;
								case "interface": //$NON-NLS-1$
									curr_part.intrface = attributes.getValue(i).trim();
									break;
							}
						}
					}
					else if (qName.equals("dataarea") && curr_software != null) //$NON-NLS-1$
					{
						curr_part.dataareas.add(curr_dataarea = new DataArea());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_dataarea.name = attributes.getValue(i).trim();
									break;
								case "size": //$NON-NLS-1$
									curr_dataarea.size = Integer.valueOf(attributes.getValue(i));
									break;
								case "width": //$NON-NLS-1$
									curr_dataarea.width = Integer.valueOf(attributes.getValue(i));
									break;
								case "endianness": //$NON-NLS-1$
									curr_dataarea.endianness = Endianness.valueOf(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("diskarea") && curr_software != null) //$NON-NLS-1$
					{
						curr_part.diskareas.add(curr_diskarea = new DiskArea());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_diskarea.name = attributes.getValue(i).trim();
									break;
							}
						}
					}
					else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						curr_machine = new Machine();
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_machine.setName(attributes.getValue(i).trim());
									machinelist_list.get(0).putByName(curr_machine);
									break;
								case "romof": //$NON-NLS-1$
									curr_machine.romof = attributes.getValue(i).trim();
									break;
								case "cloneof": //$NON-NLS-1$
									curr_machine.cloneof = attributes.getValue(i).trim();
									break;
								case "sampleof": //$NON-NLS-1$
									curr_machine.sampleof = attributes.getValue(i).trim();
									if (!machinelist_list.get(0).samplesets.containsName(curr_machine.sampleof))
										machinelist_list.get(0).samplesets.putByName(curr_sampleset = new Samples(curr_machine.sampleof));
									else
										curr_sampleset = machinelist_list.get(0).samplesets.getByName(curr_machine.sampleof);
									break;
								case "isbios": //$NON-NLS-1$
									curr_machine.isbios = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
								case "ismechanical": //$NON-NLS-1$
									curr_machine.ismechanical = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
								case "isdevice": //$NON-NLS-1$
									curr_machine.isdevice = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("description") && (curr_machine != null || curr_software != null || curr_software_list != null)) //$NON-NLS-1$
					{
						in_description = true;
					}
					else if (qName.equals("year") && (curr_machine != null || curr_software != null)) //$NON-NLS-1$
					{
						in_year = true;
					}
					else if (qName.equals("manufacturer") && (curr_machine != null)) //$NON-NLS-1$
					{
						in_manufacturer = true;
					}
					else if (qName.equals("publisher") && (curr_software != null)) //$NON-NLS-1$
					{
						in_publisher = true;
					}
					else if (qName.equals("driver")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "status": //$NON-NLS-1$
										curr_machine.driver.setStatus(attributes.getValue(i));
										break;
									case "emulation": //$NON-NLS-1$
										curr_machine.driver.setEmulation(attributes.getValue(i));
										break;
									case "cocktail": //$NON-NLS-1$
										curr_machine.driver.setCocktail(attributes.getValue(i));
										break;
									case "savestate": //$NON-NLS-1$
										curr_machine.driver.setSaveState(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("display")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "rotate": //$NON-NLS-1$
									{
										try
										{
											final Integer orientation = Integer.parseInt(attributes.getValue(i));
											if (orientation == 0 || orientation == 180)
												curr_machine.orientation = Machine.DisplayOrientation.horizontal;
											if (orientation == 90 || orientation == 270)
												curr_machine.orientation = Machine.DisplayOrientation.vertical;
										}
										catch (final NumberFormatException e)
										{
										}
										break;
									}
								}
							}
						}
					}
					else if (qName.equals("input")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "players": //$NON-NLS-1$
										curr_machine.input.setPlayers(attributes.getValue(i));
										break;
									case "coins": //$NON-NLS-1$
										curr_machine.input.setCoins(attributes.getValue(i));
										break;
									case "service": //$NON-NLS-1$
										curr_machine.input.setService(attributes.getValue(i));
										break;
									case "tilt": //$NON-NLS-1$
										curr_machine.input.setTilt(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("dipswitch")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if ("cabinet".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											in_cabinet_dipsw = true;
								}
							}
						}
					}
					else if (qName.equals("dipvalue")) //$NON-NLS-1$
					{
						if (curr_machine != null && in_cabinet_dipsw)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if ("cocktail".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											cabtype_set.add(CabinetType.cocktail);
										else if ("upright".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											cabtype_set.add(CabinetType.upright);
								}
							}

						}
					}
					else if (qName.equals("sample")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if(curr_sampleset==null)
										{
											curr_machine.sampleof = curr_machine.getBaseName();
											if (!machinelist_list.get(0).samplesets.containsName(curr_machine.sampleof))
												machinelist_list.get(0).samplesets.putByName(curr_sampleset = new Samples(curr_machine.sampleof));
											else
												curr_sampleset = machinelist_list.get(0).samplesets.getByName(curr_machine.sampleof);
										}
										curr_machine.samples.add(curr_sampleset.add(new Sample(curr_sampleset, attributes.getValue(i))));
										samples_cnt++;
										break;
								}
							}
						}
					}
					else if (qName.equals("device_ref")) //$NON-NLS-1$
					{
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_machine.device_ref.add(attributes.getValue(i));
								}
							}
						}
					}
					else if (qName.equals("rom")) //$NON-NLS-1$
					{
						if (curr_machine != null || curr_software != null)
						{
							curr_rom = new Rom(curr_machine != null ? curr_machine : curr_software);
							if (curr_software != null && curr_dataarea != null)
								curr_dataarea.roms.add(curr_rom);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_rom.setName(attributes.getValue(i).trim());
										break;
									case "size": //$NON-NLS-1$
										curr_rom.size = Long.decode(attributes.getValue(i));
										break;
									case "offset": //$NON-NLS-1$
										try
										{
											curr_rom.offset = Integer.decode(attributes.getValue(i));
										}
										catch (final NumberFormatException e)
										{
											curr_rom.offset = Integer.decode("0x" + attributes.getValue(i)); //$NON-NLS-1$
										}
										break;
									case "value": //$NON-NLS-1$
										curr_rom.value = attributes.getValue(i);
										break;
									case "crc": //$NON-NLS-1$
										curr_rom.crc = attributes.getValue(i);
										break;
									case "sha1": //$NON-NLS-1$
										curr_rom.sha1 = attributes.getValue(i);
										sha1_roms = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_rom.md5 = attributes.getValue(i);
										md5_roms = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_rom.merge = attributes.getValue(i).trim();
										break;
									case "bios": //$NON-NLS-1$
										curr_rom.bios = attributes.getValue(i);
										break;
									case "region": //$NON-NLS-1$
										curr_rom.region = attributes.getValue(i);
										break;
									case "date": //$NON-NLS-1$
										curr_rom.date = attributes.getValue(i);
										break;
									case "optional": //$NON-NLS-1$
										curr_rom.optional = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "status": //$NON-NLS-1$
										curr_rom.status = Entity.Status.valueOf(attributes.getValue(i));
										break;
									case "loadflag": //$NON-NLS-1$
										curr_rom.loadflag = LoadFlag.getEnum(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("disk")) //$NON-NLS-1$
					{
						if (curr_machine != null || curr_software != null)
						{
							curr_disk = new Disk(curr_machine != null ? curr_machine : curr_software);
							if (curr_software != null && curr_diskarea != null)
								curr_diskarea.disks.add(curr_disk);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_disk.setName(attributes.getValue(i).trim());
										break;
									case "sha1": //$NON-NLS-1$
										curr_disk.sha1 = attributes.getValue(i);
										sha1_disks = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_disk.md5 = attributes.getValue(i);
										md5_disks = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_disk.merge = attributes.getValue(i).trim();
										break;
									case "index": //$NON-NLS-1$
										curr_disk.index = Integer.decode(attributes.getValue(i));
										break;
									case "optional": //$NON-NLS-1$
										curr_disk.optional = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "writeable": //$NON-NLS-1$
										curr_disk.writeable = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "region": //$NON-NLS-1$
										curr_disk.region = attributes.getValue(i);
										break;
									case "status": //$NON-NLS-1$
										curr_disk.status = Entity.Status.valueOf(attributes.getValue(i));
										break;
								}
							}
						}
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException
				{
					if (qName.equals("header")) //$NON-NLS-1$
					{
						in_header = false;
					}
					else if (qName.equals("softwarelist") && curr_software_list != null) //$NON-NLS-1$
					{
						machinelist_list.softwarelist_list.add(curr_software_list);
						softwares_list_cnt++;
						curr_software_list = null;
					}
					else if (qName.equals("software")) //$NON-NLS-1$
					{
						roms.clear();
						disks.clear();
						curr_software_list.add(curr_software);
						softwares_cnt++;
						curr_software = null;
						handler.setProgress(String.format(Messages.getString("Profile.SWLoaded"), softwares_cnt, swroms_cnt, swdisks_cnt)); //$NON-NLS-1$
						if (handler.isCancel())
							throw new BreakException();
					}
					else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						roms.clear();
						disks.clear();
						machinelist_list.get(0).add(curr_machine);
						machines_cnt++;
						curr_machine = null;
						curr_sampleset = null;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), machines_cnt, roms_cnt, disks_cnt, samples_cnt)); //$NON-NLS-1$
						if (handler.isCancel())
							throw new BreakException();
					}
					else if (qName.equals("rom")) //$NON-NLS-1$
					{
						if (curr_rom.getName() != null)
						{
							if(!roms.contains(curr_rom.getBaseName()))
							{
								roms.add(curr_rom.getBaseName());
								if (curr_machine != null)
								{
									curr_machine.roms.add(curr_rom);
									roms_cnt++;
								}
								else
								{
									curr_software.roms.add(curr_rom);
									swroms_cnt++;
								}
							}
							if (curr_rom.crc != null)
							{
								final Rom old_rom = roms_bycrc.put(curr_rom.crc, curr_rom);
								if (old_rom != null)
								{
									if (old_rom.sha1 != null && curr_rom.sha1 != null)
										if (!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
									if (old_rom.md5 != null && curr_rom.md5 != null)
										if (!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
								}
							}
						}
					}
					else if (qName.equals("disk")) //$NON-NLS-1$
					{
						if (curr_disk.getName() != null)
						{
							if(!disks.contains(curr_disk.getBaseName()))
							{
								disks.add(curr_disk.getBaseName());
								if (curr_machine != null)
								{
									curr_machine.disks.add(curr_disk);
									disks_cnt++;
								}
								else
								{
									curr_software.disks.add(curr_disk);
									swdisks_cnt++;
								}
							}
						}
					}
					else if (qName.equals("description") && (curr_machine != null || curr_software != null || curr_software_list != null)) //$NON-NLS-1$
					{
						in_description = false;
					}
					else if (qName.equals("year") && (curr_machine != null || curr_software != null)) //$NON-NLS-1$
					{
						in_year = false;
					}
					else if (qName.equals("manufacturer") && (curr_machine != null)) //$NON-NLS-1$
					{
						in_manufacturer = false;
					}
					else if (qName.equals("publisher") && (curr_software != null)) //$NON-NLS-1$
					{
						in_publisher = false;
					}
					else if (qName.equals("dipswitch") && in_cabinet_dipsw) //$NON-NLS-1$
					{
						if (cabtype_set.contains(CabinetType.cocktail))
						{
							if (cabtype_set.contains(CabinetType.upright))
								curr_machine.cabinetType = CabinetType.any;
							else
								curr_machine.cabinetType = CabinetType.cocktail;
						}
						else
							curr_machine.cabinetType = CabinetType.upright;
						cabtype_set.clear();
						in_cabinet_dipsw = false;
					}
				}

				@Override
				public void characters(final char[] ch, final int start, final int length) throws SAXException
				{
					if (in_description)
					{
						if (curr_machine != null)
							curr_machine.description.append(ch, start, length);
						else if (curr_software != null)
							curr_software.description.append(ch, start, length);
						else if (curr_software_list != null)
							curr_software_list.description.append(ch, start, length);
					}
					else if (in_year)
					{
						if (curr_machine != null)
							curr_machine.year.append(ch, start, length);
						else if (curr_software != null)
							curr_software.year.append(ch, start, length);
					}
					else if (in_manufacturer && curr_machine != null)
					{
						curr_machine.manufacturer.append(ch, start, length);
					}
					else if (in_publisher && curr_software != null)
					{
						curr_software.publisher.append(ch, start, length);
					}
					else if (in_header)
					{
						if (!header.containsKey(curr_tag))
							header.put(curr_tag, new StringBuffer());
						header.get(curr_tag).append(ch, start, length);
					}
				}
			});
			return true;
		}
		catch (final ParserConfigurationException | SAXException e)
		{
			Log.err("Parser Exception", e); //$NON-NLS-1$
		}
		catch (final IOException e)
		{
			Log.err("IO Exception", e); //$NON-NLS-1$
		}
		catch (final BreakException e)
		{
			return false;
		}
		catch (final Throwable e)
		{
			Log.err("Other Exception", e); //$NON-NLS-1$
		}
		return false;
	}

	private static File getCacheFile(final File file)
	{
		return new File(file.getParentFile(), file.getName() + ".cache"); //$NON-NLS-1$
	}

	public void save()
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(Profile.getCacheFile(nfo.file)))))
		{
			oos.writeObject(this);
		}
		catch (final Throwable e)
		{

		}
	}

	public static Profile load(final File file, final ProgressHandler handler)
	{
		return Profile.load(ProfileNFO.load(file), handler);
	}

	public static Profile load(final ProfileNFO nfo, final ProgressHandler handler)
	{
		Profile profile = null;
		final File cachefile = Profile.getCacheFile(nfo.file);
		if (cachefile.lastModified() >= nfo.file.lastModified() && (!nfo.isJRM() || cachefile.lastModified() >= nfo.mame.fileroms.lastModified()) && !Settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
		{
			handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
			try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(handler.getInputStream(new FileInputStream(cachefile),(int)cachefile.length()))))
			{
				profile = (Profile) ois.readObject();
				profile.nfo = nfo;
			}
			catch (final Throwable e)
			{
			}
		}
		if (profile == null)
		{
			profile = new Profile();
			profile.nfo = nfo;
			if (nfo.isJRM())
			{
				if (nfo.mame.fileroms != null)
				{
					if (!profile._load(nfo.mame.fileroms, handler))
						return null;
					if (nfo.mame.filesl != null)
					{
						if (!profile._load(nfo.mame.filesl, handler))
							return null;
					}
				}
			}
			else if (!profile._load(nfo.file, handler))
				return null;
			handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
			profile.buildParentClonesRelations();
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			profile.save();
		}
		profile.nfo.stats.version = profile.build != null ? profile.build : (profile.header.containsKey("version") ? profile.header.get("version").toString() : null); //$NON-NLS-1$ //$NON-NLS-2$
		profile.nfo.stats.totalSets = profile.softwares_cnt + profile.machines_cnt;
		profile.nfo.stats.totalRoms = profile.roms_cnt + profile.swroms_cnt;
		profile.nfo.stats.totalDisks = profile.disks_cnt + profile.swdisks_cnt;
		handler.setProgress("Loading settings...", -1); //$NON-NLS-1$
		profile.loadSettings();
		handler.setProgress("Creating Systems filters...", -1); //$NON-NLS-1$
		profile.loadSystems();
		handler.setProgress("Creating Years filters...", -1); //$NON-NLS-1$
		profile.loadYears();
		handler.setProgress("Loading catver.ini ...", -1); //$NON-NLS-1$
		profile.loadCatVer();
		handler.setProgress("Loading nplayers.ini ...", -1); //$NON-NLS-1$
		profile.loadNPlayers();
		return profile;
	}

	private void buildParentClonesRelations()
	{
		machinelist_list.forEach(machine_list -> {
			machine_list.forEach(machine -> {
				if (machine.romof != null)
				{
					machine.setParent(machine_list.getByName(machine.romof));
					if (machine.parent != null)
					{
						if (!machine.getParent().isbios)
							machine.getParent().clones.put(machine.getName(), machine);
					}
				}
				machine.device_ref.forEach(device_ref -> machine.devices.putIfAbsent(device_ref, machine_list.getByName(device_ref)));
			});
		});
		machinelist_list.softwarelist_list.forEach(software_list -> {
			software_list.forEach(software -> {
				if (software.cloneof != null)
				{
					software.setParent(software_list.getByName(software.cloneof));
					if (software.parent != null)
						software.getParent().clones.put(software.getName(), software);
				}
			});
		});
	}

	private File getSettingsFile(final File file)
	{
		return new File(file.getParentFile(), file.getName() + ".properties"); //$NON-NLS-1$
	}

	public void saveSettings()
	{
		if (settings == null)
			settings = new Properties();
		try (FileOutputStream os = new FileOutputStream(getSettingsFile(nfo.file)))
		{
			settings.storeToXML(os, null);
			nfo.save();
		}
		catch (final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}

	public void loadSettings()
	{
		if (settings == null)
			settings = new Properties();
		if (getSettingsFile(nfo.file).exists())
		{
			try (FileInputStream is = new FileInputStream(getSettingsFile(nfo.file)))
			{
				settings.loadFromXML(is);
			}
			catch (final IOException e)
			{
				Log.err("IO", e); //$NON-NLS-1$
			}
		}
	}

	public void setProperty(final String property, final boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}

	public void setProperty(final String property, final String value)
	{
		settings.setProperty(property, value);
	}

	public boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}

	public String getProperty(final String property, final String def)
	{
		return settings.getProperty(property, def);
	}

	private int props_hashcode = 0;
	
	public void setPropsCheckPoint()
	{
		props_hashcode = settings.hashCode();
	}
	
	public boolean hasPropsChanged()
	{
		return props_hashcode != settings.hashCode();
	}
	
	public String getName()
	{
		String name = "<html><body>[<span color='blue'>" + Paths.get(".", "xmlfiles").toAbsolutePath().normalize().relativize(nfo.file.toPath()) + "</span>] "; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (build != null)
			name += "<b>" + build + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
		else if (header.size() > 0)
		{
			if (header.containsKey("description")) //$NON-NLS-1$
				name += "<b>" + header.get("description") + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			else if (header.containsKey("name")) //$NON-NLS-1$
			{
				name += "<b>" + header.get("name") + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				if (header.containsKey("version")) //$NON-NLS-1$
					name += " (" + header.get("version") + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		String strcnt = ""; //$NON-NLS-1$
		if (machinelist_list.get(0).size() > 0)
			strcnt += machines_cnt + " Machines"; //$NON-NLS-1$
		if (machinelist_list.softwarelist_list.size() > 0)
			strcnt += (strcnt.isEmpty() ? "" : ", ") + softwares_list_cnt + " Software Lists, " + softwares_cnt + " Softwares"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		name += "(" + strcnt + ")</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$
		return name;
	}

	public void loadSystems()
	{
		systems = new Systms();
		systems.add(SystmStandard.STANDARD);
		systems.add(SystmMechanical.MECHANICAL);
		systems.add(SystmDevice.DEVICE);
		final ArrayList<Machine> machines = new ArrayList<>();
		machinelist_list.get(0).forEach(m -> {
			if (m.isbios)
				machines.add(m);
		});
		machines.sort((a, b) -> a.getName().compareTo(b.getName()));
		machines.forEach(systems::add);
		final ArrayList<SoftwareList> softwarelists = new ArrayList<>();
		machinelist_list.softwarelist_list.forEach(softwarelists::add);
		softwarelists.sort((a, b) -> a.getName().compareTo(b.getName()));
		softwarelists.forEach(systems::add);
	}

	public void loadYears()
	{
		final HashSet<String> years = new HashSet<>();
		years.add(""); //$NON-NLS-1$
		machinelist_list.get(0).forEach(m -> years.add(m.year.toString()));
		machinelist_list.softwarelist_list.forEach(sl -> sl.forEach(s -> years.add(s.year.toString())));
		years.add("????"); //$NON-NLS-1$
		this.years = years;
	}

	public void loadCatVer()
	{
		try
		{
			catver = CatVer.read(new File(getProperty("filter.catver.ini", null))); //$NON-NLS-1$
			for (final Category cat : catver)
			{
				for (final SubCategory subcat : cat)
				{
					for (final String game : subcat)
					{
						final Machine m = machinelist_list.get(0).getByName(game);
						if (m != null)
							m.subcat = subcat;
					}
				}
			}
		}
		catch (final Throwable e)
		{
			catver = null;
		}
	}

	public void loadNPlayers()
	{
		try
		{
			nplayers = NPlayers.read(new File(getProperty("filter.nplayers.ini", null))); //$NON-NLS-1$
			for (final NPlayer nplayer : nplayers)
			{
				for (final String game : nplayer)
				{
					final Machine m = machinelist_list.get(0).getByName(game);
					if (m != null)
						m.nplayer = nplayer;
				}
			}
		}
		catch (final Throwable e)
		{
			nplayers = null;
		}
	}

	public int size()
	{
		return machinelist_list.size() + machinelist_list.softwarelist_list.size();
	}

	public int subsize()
	{
		return machinelist_list.get(0).size() + machinelist_list.get(0).samplesets.size() + machinelist_list.softwarelist_list.stream().flatMapToInt(sl -> IntStream.of(sl.size())).sum();
	}
}
