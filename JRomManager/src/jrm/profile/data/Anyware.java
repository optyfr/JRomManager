package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jrm.profile.Profile;
import jrm.profile.data.Entity.Status;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.AnywareRenderer;
import one.util.streamex.StreamEx;

/**
 * This class is the common class for machines and softwares
 * @author optyfr
 *  
 */
@SuppressWarnings("serial")
public abstract class Anyware extends AnywareBase implements Serializable, TableModel, Systm
{
	/**
	 * the name of the parent from which this instance is a clone, null if the instance is not a clone
	 */
	public String cloneof = null;
	/**
	 * The description field, generally the complete name of the game
	 */
	public final StringBuffer description = new StringBuffer();
	/**
	 * The release year (may contain non numeric chars like question mark [?])
	 */
	public final StringBuffer year = new StringBuffer();

	/**
	 * The list of {@link Rom} entities related to this object
	 */
	public final Collection<Rom> roms = new ArrayList<>();
	/**
	 * The list of {@link Disk} entities related to this object
	 */
	public final Collection<Disk> disks = new ArrayList<>();
	/**
	 * The list of {@link Sample} entities related to this object
	 */
	public final Collection<Sample> samples = new ArrayList<>();

	/**
	 * A hash table of clones if this object has clones
	 */
	public final HashMap<String, Anyware> clones = new HashMap<>();

	/**
	 * Is that machine/software is *individually* selected for inclusion in your set ? (true by default)
	 */
	public boolean selected = true;

	
	//Non serialized fields start from here 
	/**
	 * The merge mode used while filtering roms/disks
	 */
	public static transient MergeOptions merge_mode;
	/**
	 * Must we strictly conform to merge tag (explicit), or search merge-able ROMs by ourselves (implicit)
	 */
	public static transient Boolean implicit_merge;
	/**
	 * What hash collision mode is used?
	 */
	public static transient HashCollisionOptions hash_collision_mode;
	/**
	 * Do we have a collision?
	 */
	protected transient boolean collision;

	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static transient EventListenerList listenerList;
	/**
	 * Non permanent filter according scan status of entities (roms, disks, samples)
	 */
	private static transient EnumSet<EntityStatus> filter = null;
	/**
	 * entities list cache (according current {@link #filter})
	 */
	private transient List<EntityBase> table_entities;

	/**
	 * The constructor, will initialize transients fields
	 */
	public Anyware()
	{
		initTransient();
	}

	/**
	 * the Serializable method for special serialization handling (in that case : initialize transient default values) 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	/**
	 * The method called to initialize transient and static fields
	 */
	private void initTransient()
	{
		if (Anyware.merge_mode == null)
			Anyware.merge_mode = MergeOptions.SPLIT;
		if (Anyware.implicit_merge == null)
			Anyware.implicit_merge = false;
		if (Anyware.hash_collision_mode == null)
			Anyware.hash_collision_mode = HashCollisionOptions.SINGLEFILE;
		collision = false;
		table_entities = null;
		if (Anyware.listenerList == null)
			Anyware.listenerList = new EventListenerList();
		if (Anyware.filter == null)
			Anyware.filter = EnumSet.allOf(EntityStatus.class);
	}

	/**
	 * is collision enabled
	 * @return true if collision mode is enabled
	 */
	public boolean isCollisionMode()
	{
		return collision;
	}

	/**
	 * Enable collision
	 * @param parent if true will also propagate to clones from parent
	 */
	public void setCollisionMode(final boolean parent)
	{
		if (parent)
			getDest().clones.forEach((n, m) -> m.collision = true);
		collision = true;
	}

	/**
	 * Reset collision, including for all roms and disks entities
	 */
	public void resetCollisionMode()
	{
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

	/**
	 * is this object a clone? A clone as a parent and that parent is not a bios
	 * @return true is that object is effectively a clone
	 */
	public boolean isClone()
	{
		return (parent != null && !getParent().isBios());
	}

	/**
	 * get the destination object, and set merge options
	 * @param merge_mode the used merge mode
	 * @param implicit_merge is implicit merge should be used
	 * @return the {@link Anyware} destination object (can be this instance or a parent)
	 * @see #getDest()
	 */
	public Anyware getDest(final MergeOptions merge_mode, final boolean implicit_merge)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.implicit_merge = implicit_merge;
		return getDest();
	}

	/**
	 * get the destination object according the {@link #merge_mode} and {@link #isClone()}
	 * - if we are merging ({@link MergeOptions#isMerge()}), then return a parent that is not a clone by returning the call from {@link #getParent()}.{@link #getDest()}
	 * - otherwise return {@code this}
	 * @return the {@link Anyware} destination object (can be this instance or a parent)
	 */
	private Anyware getDest()
	{
		if (Anyware.merge_mode.isMerge() && isClone())
			return getParent().getDest();
		return this;
	}

	/**
	 * Is this object a bios
	 * @return true if it's a bios (can be always false depending of extending class)
	 */
	public abstract boolean isBios();

	/**
	 * Is this object depends of roms from another object?
	 * @return true if it needs roms from another object (can be always false depending of extending class)
	 */
	public abstract boolean isRomOf();

	/**
	 * return filtered list of disks according various flags and status
	 * @param merge_mode the merge mode to set and use
	 * @param hash_collision_mode the hash collision mode to set and use 
	 * @return the filtered list of disks
	 * @see "Source code for more explanations"
	 */
	public List<Disk> filterDisks(final MergeOptions merge_mode, final HashCollisionOptions hash_collision_mode)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.hash_collision_mode = hash_collision_mode;
		
		/*/
		 * Stream initialization
		 */
		Stream<Disk> stream;
		if (!selected) // skip if not selected 
			stream = Stream.empty();
		else if (merge_mode.isMerge()) // if merging
		{
			if (isClone() && getParent().selected)	// skip if I'm a clone and my parent is selected for inclusion
				stream = Stream.empty();
			else // otherwise...
			{
				// concatenate my disks and all my clones disks into a stream
				final List<Disk> disks_with_clones = Stream.concat(disks.stream(), clones.values().stream().flatMap(m -> m.disks.stream())).collect(Collectors.toList());
				// and mark for collision disks with same names but with different hash (this will change the way getName return disks names)
				StreamEx.of(disks_with_clones).groupingBy(Disk::getName).forEach((n, l) -> {
					if (l.size() > 1 && StreamEx.of(l).distinct(Disk::hashString).count() > 1)
						l.forEach(Disk::setCollisionMode);
				});
				// and finally remove real duplicate disks
				stream = StreamEx.of(disks_with_clones).distinct(Disk::getName);
			}
		}
		else // if not merging
			stream = disks.stream();	// simply initialize stream to current object disks list
		
		/*
		 * Stream filtering
		 */
		return stream.filter(d -> {
			if (d.status == Status.nodump)	// exclude nodump disks
				return false;
			if (merge_mode == MergeOptions.SPLIT && Anyware.containsInParent(this, d))	// exclude if splitting and the disk is in parent
				return false;
			if (merge_mode == MergeOptions.NOMERGE && Anyware.containsInParent(this, d))	// explicitely include if nomerge and the disk is in parent
				return true;
			return isBios() || !Anyware.containsInParent(this, d);	// otherwise include if I'm bios or the disk is not in parent
		}).collect(Collectors.toList());
	}

	public List<Rom> filterRoms(final MergeOptions merge_mode, final HashCollisionOptions hash_collision_mode)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.hash_collision_mode = hash_collision_mode;
		
		/*
		 * Stream initialization
		 */
		Stream<Rom> stream;
		if (!selected) // skip if not selected 
			stream = Stream.empty();
		else if (merge_mode.isMerge()) // if merging
		{
			if (isClone() && getParent().selected)	// skip if I'm a clone and my parent is selected for inclusion
				stream = Stream.empty();
			else // otherwise...
			{
				// concatenate my roms and all my clones roms into a stream
				final List<Rom> roms_with_clones = Stream.concat(roms.stream(), clones.values().stream().flatMap(m -> m.roms.stream())).collect(Collectors.toList());
				// and mark for collision roms with same names but with different hash (this will change the way getName return roms names)
				StreamEx.of(roms_with_clones).groupingBy(Rom::getName).forEach((n, l) -> {
					if (l.size() > 1 && StreamEx.of(l).distinct(Rom::hashString).count() > 1)
						l.forEach(Rom::setCollisionMode);
				});
				if (HashCollisionOptions.HALFDUMB == hash_collision_mode)	// HALFDUMB extra stuffs for PD
				{
					/*
					 *  This will filter roms with same hash between clones (the encountered first clone collisioning rom is kept),
					 *  and also remove parent roms from clone (implicit merge or not)
					 *  note that roms dat declaration order is important, as well that clone name alphabetical order
					 */
					final LinkedHashMap<String , Rom> map = new LinkedHashMap<>();
					roms.forEach(r -> map.put(r.hashString(), r));
					clones.values().stream().sorted().forEach(w -> w.roms.stream().sorted().forEach(r -> map.putIfAbsent(r.hashString(), r)));
					final List<Rom> clones_roms = new ArrayList<>(map.values());
					clones_roms.removeAll(roms);
					stream = Stream.concat(roms.stream(), clones_roms.stream());
				}
				else // finally remove real duplicate disks
					stream = StreamEx.of(roms_with_clones).distinct(Rom::getName);
			}
		}
		else // if not merging
		{
			if(merge_mode.equals(MergeOptions.SUPERFULLNOMERGE))	// also include devices
			{
				if(Profile.curr_profile.getProperty("exclude_games", false))
				{
					if(Profile.curr_profile.getProperty("exclude_machines", false))
						stream = streamWithDevices(true, false, true);	// bios-devices
					else
						stream = streamWithDevices(true, false, true);	// machine-bios-devices
				}
				else
					stream = streamWithDevices(false, true, false);	// all
			}
			else	// simply initialize stream to current object disks list
				stream = roms.stream();
		}
		return stream.filter(r -> {
			if (r.status == Status.nodump)	// exclude nodump roms
				return false;
			if (r.crc == null)	// exclude nocrc roms
				return false;
			if (r.name.isEmpty())	// exclude empty name roms
				return false;
			if (merge_mode == MergeOptions.SUPERFULLNOMERGE)	// Unconditionally include roms in SUPERFULLNOMERGE mode
				return true;
			if (merge_mode == MergeOptions.FULLNOMERGE)	// Unconditionally include roms in FULLNOMERGE mode
				return true;
			if (merge_mode == MergeOptions.FULLMERGE)	// Unconditionally include roms in FULLMERGE mode
				return true;
			if (merge_mode == MergeOptions.SPLIT && Anyware.containsInParent(this, r, false))	// exclude if splitting and the rom is in parent
				return false;
			if (merge_mode == MergeOptions.NOMERGE && Anyware.containsInParent(this, r, true))	// exclude if not merging and the rom is in BIOS
				return false;
			if (merge_mode == MergeOptions.NOMERGE && Anyware.wouldMerge(this, r))	// include if not merging and the rom would be merged in parent (when selected)
				return true;
			if (merge_mode == MergeOptions.MERGE && Anyware.containsInParent(this, r, true))	// exclude if merging and the rom is in BIOS
				return false;
			if (merge_mode == MergeOptions.MERGE && Anyware.wouldMerge(this, r))	// include if merging and the rom would be merged in parent (when selected)
				return true;
			return isBios() || !Anyware.containsInParent(this, r, false);	// otherwise include if I'm bios or if the rom is not in parent
		}).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param excludeBios
	 * @param partial
	 * @param recurse
	 * @return
	 */
	abstract Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse);

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static <T> Stream<T> streamInReverse(List<T> input)
	{
		return IntStream.range(1, input.size() + 1).mapToObj(i -> input.get(input.size() - i));
	}

	/**
	 * 
	 * @param ware
	 * @param e
	 * @return
	 */
	public static boolean wouldMerge(final Anyware ware, final Entity e)
	{
		if (e.merge != null)
			if (ware.isRomOf())
				if (ware.getParent() != null && ware.getParent().selected)
					return true;
		return false;
	}

	/**
	 * 
	 * @param ware
	 * @param r
	 * @param onlyBios
	 * @return
	 */
	public static boolean containsInParent(final Anyware ware, final Rom r, final boolean onlyBios)
	{
		if (r.merge != null || Anyware.implicit_merge)
		{
			if (ware.getParent() != null && ware.getParent().selected)
			{
				if (!onlyBios || ware.getParent().isBios())
					if (ware.getParent().roms.contains(r))
						return true;
				return Anyware.containsInParent(ware.getParent(), r, onlyBios);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param ware
	 * @param d
	 * @return
	 */
	public static boolean containsInParent(final Anyware ware, final Disk d)
	{
		if (d.merge != null || Anyware.implicit_merge)
		{
			if (ware.getParent() != null && ware.getParent().selected)
			{
				if (ware.getParent().disks.contains(d))
					return true;
				return Anyware.containsInParent(ware.getParent(), d);
			}
		}
		return false;
	}

	/**
	 * 
	 */
	public void reset()
	{
		table_entities = null;
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * 
	 * @param filter
	 */
	public void setFilter(final EnumSet<EntityStatus> filter)
	{
		Anyware.filter = filter;
		reset();
	}

	/**
	 * 
	 * @return
	 */
	private List<EntityBase> getEntities()
	{
		if (table_entities == null)
			table_entities = Stream.of(roms.stream(), disks.stream(), samples.stream()).flatMap(s -> s).filter(t -> Anyware.filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return table_entities;
	}

	@Override
	public int getRowCount()
	{
		return getEntities().size();
	}

	@Override
	public int getColumnCount()
	{
		return AnywareRenderer.columns.length;
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		return AnywareRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return AnywareRenderer.columnsTypes[columnIndex];
	}

	/**
	 * 
	 * @param columnIndex
	 * @return
	 */
	public static TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return columnIndex < AnywareRenderer.columnsRenderers.length && AnywareRenderer.columnsRenderers[columnIndex] != null ? AnywareRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	/**
	 * 
	 * @param columnIndex
	 * @return
	 */
	public static int getColumnWidth(final int columnIndex)
	{
		return AnywareRenderer.columnsWidths[columnIndex];
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex)
	{
		return false;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
				return getEntities().get(rowIndex);
			case 1:
				return getEntities().get(rowIndex);
			case 2:
				return getEntities().get(rowIndex).getProperty("size"); //$NON-NLS-1$
			case 3:
				return getEntities().get(rowIndex).getProperty("crc"); //$NON-NLS-1$
			case 4:
				return getEntities().get(rowIndex).getProperty("md5"); //$NON-NLS-1$
			case 5:
				return getEntities().get(rowIndex).getProperty("sha1"); //$NON-NLS-1$
			case 6:
				return getEntities().get(rowIndex).getProperty("merge"); //$NON-NLS-1$
			case 7:
				return getEntities().get(rowIndex).getProperty("status"); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(final TableModelListener l)
	{
		Anyware.listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		Anyware.listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * 
	 * @param e
	 */
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = Anyware.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	/**
	 * 
	 */
	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for (final Disk disk : disks)
		{
			final EntityStatus estatus = disk.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok = true;
			else if (estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		for (final Rom rom : roms)
		{
			final EntityStatus estatus = rom.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok = true;
			else if (estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		for (final Sample sample : samples)
		{
			final EntityStatus estatus = sample.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok = true;
			else if (estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if (status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}

	/**
	 * 
	 * @return
	 */
	public int countHave()
	{
		return countHaveRoms() + countHaveDisks() + countHaveSamples();
	}

	/**
	 * 
	 * @return
	 */
	public int countHaveRoms()
	{
		return roms.stream().mapToInt(r -> r.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * 
	 * @return
	 */
	public int countHaveDisks()
	{
		return disks.stream().mapToInt(d -> d.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * 
	 * @return
	 */
	public int countHaveSamples()
	{
		return samples.stream().mapToInt(s -> s.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * 
	 * @return
	 */
	public int countAll()
	{
		return roms.size() + disks.size() + samples.size();
	}

	@Override
	public Anyware getParent()
	{
		return getParent(Anyware.class);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Anyware)
			return this.name.equals(((Anyware)obj).name);
		return super.equals(obj);
	}


}
