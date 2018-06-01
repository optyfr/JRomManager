package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jrm.profile.data.Entity.Status;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.AnywareRenderer;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public abstract class Anyware extends AnywareBase implements Serializable, TableModel, Systm
{
	public String cloneof = null;
	public final StringBuffer description = new StringBuffer();
	public final StringBuffer year = new StringBuffer();

	public final Collection<Rom> roms = new ArrayList<>();
	public final Collection<Disk> disks = new ArrayList<>();
	public final Collection<Sample> samples = new ArrayList<>();

	public final HashMap<String, Anyware> clones = new HashMap<>();

	public static transient MergeOptions merge_mode;
	public static transient Boolean implicit_merge;
	public static transient HashCollisionOptions hash_collision_mode;
	protected transient boolean collision;

	private static transient EventListenerList listenerList;
	private static transient EnumSet<EntityStatus> filter = null;
	private transient List<EntityBase> table_entities;

	public Anyware()
	{
		initTransient();
	}

	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	private void initTransient()
	{
		if(Anyware.merge_mode == null)
			Anyware.merge_mode = MergeOptions.SPLIT;
		if(Anyware.implicit_merge == null)
			Anyware.implicit_merge = false;
		if(Anyware.hash_collision_mode == null)
			Anyware.hash_collision_mode = HashCollisionOptions.SINGLEFILE;
		collision = false;
		table_entities = null;
		if(Anyware.listenerList == null)
			Anyware.listenerList = new EventListenerList();
		if(Anyware.filter == null)
			Anyware.filter = EnumSet.allOf(EntityStatus.class);
	}

	public boolean isCollisionMode()
	{
		return collision;
	}

	public void setCollisionMode(final boolean parent)
	{
		if(parent)
			getDest().clones.forEach((n, m) -> m.collision = true);
		collision = true;
	}

	public void resetCollisionMode()
	{
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

	public boolean isClone()
	{
		return (parent != null && !getParent().isBios());
	}

	public Anyware getDest(final MergeOptions merge_mode, final boolean implicit_merge)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.implicit_merge = implicit_merge;
		return getDest();
	}

	private Anyware getDest()
	{
		if(Anyware.merge_mode.isMerge() && isClone())
			return getParent().getDest(Anyware.merge_mode, Anyware.implicit_merge);
		return this;
	}

	public abstract boolean isBios();

	public abstract boolean isRomOf();

	public List<Disk> filterDisks(final MergeOptions merge_mode, final HashCollisionOptions hash_collision_mode)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.hash_collision_mode = hash_collision_mode;
		Stream<Disk> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				final List<Disk> disks_with_clones = Stream.concat(disks.stream(), clones.values().stream().flatMap(m -> m.disks.stream())).collect(Collectors.toList());
				StreamEx.of(disks_with_clones).groupingBy(Disk::getName).forEach((n, l) -> {
					if(l.size() > 1 && StreamEx.of(l).distinct(Disk::hashString).count() > 1)
						l.forEach(Disk::setCollisionMode);
				});
				stream = StreamEx.of(disks_with_clones).distinct(Disk::getName);
			}
		}
		else
			stream = disks.stream();
		return stream.filter(d -> {
			if(d.status == Status.nodump)
				return false;
			if(merge_mode == MergeOptions.SPLIT && Anyware.containsInParent(this, d))
				return false;
			if(merge_mode == MergeOptions.NOMERGE && Anyware.containsInParent(this, d))
				return true;
			return isBios() || !Anyware.containsInParent(this, d);
		}).collect(Collectors.toList());
	}

	public List<Rom> filterRoms(final MergeOptions merge_mode, final HashCollisionOptions hash_collision_mode)
	{
		Anyware.merge_mode = merge_mode;
		Anyware.hash_collision_mode = hash_collision_mode;
		Stream<Rom> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				final List<Rom> roms_with_clones = Stream.concat(roms.stream(), clones.values().stream().flatMap(m -> m.roms.stream())).collect(Collectors.toList());
				StreamEx.of(roms_with_clones).groupingBy(Rom::getName).forEach((n, l) -> {
					if(l.size() > 1 && StreamEx.of(l).distinct(Rom::hashString).count() > 1)
						l.forEach(Rom::setCollisionMode);
				});
				stream = StreamEx.of(roms_with_clones).distinct(Rom::getName);
			}
		}
		else
			stream = roms.stream();
		return stream.filter(r -> {
			if(r.status == Status.nodump)
				return false;
			if(r.crc == null)
				return false;
			if(r.name.isEmpty())
				return false;
			if(merge_mode == MergeOptions.FULLNOMERGE)
				return true;
			if(merge_mode == MergeOptions.FULLMERGE)
				return true;
			if(merge_mode == MergeOptions.SPLIT && Anyware.containsInParent(this, r, false))
				return false;
			if(merge_mode == MergeOptions.NOMERGE && Anyware.containsInParent(this, r, true))
				return false;
			if(merge_mode == MergeOptions.NOMERGE && Anyware.wouldMerge(this, r))
				return true;
			if(merge_mode == MergeOptions.MERGE && Anyware.containsInParent(this, r, true))
				return false;
			if(merge_mode == MergeOptions.MERGE && Anyware.wouldMerge(this, r))
				return true;
			return isBios() || !Anyware.containsInParent(this, r, false);
		}).collect(Collectors.toList());
	}

	public static boolean wouldMerge(final Anyware ware, final Entity e)
	{
		if(e.merge!=null)
			if(ware.isRomOf())
				if(ware.getParent()!=null)
					return true;
		return false;
	}

	public static boolean containsInParent(final Anyware ware, final Rom r, final boolean onlyBios)
	{
		if(r.merge!=null || Anyware.implicit_merge)
		{
			if(ware.getParent()!=null)
			{
				if(!onlyBios || ware.getParent().isBios())
					if(ware.getParent().roms.contains(r))
						return true;
				return Anyware.containsInParent(ware.getParent(), r, onlyBios);
			}
		}
		return false;
	}

	public static boolean containsInParent(final Anyware ware, final Disk d)
	{
		if(d.merge!=null || Anyware.implicit_merge)
		{
			if(ware.getParent()!=null)
			{
				if(ware.getParent().disks.contains(d))
					return true;
				return Anyware.containsInParent(ware.getParent(), d);
			}
		}
		return false;
	}

	public void reset()
	{
		table_entities = null;
		fireTableChanged(new TableModelEvent(this));
	}

	public void setFilter(final EnumSet<EntityStatus> filter)
	{
		Anyware.filter = filter;
		reset();
	}

	private List<EntityBase> getEntities()
	{
		if(table_entities == null)
			table_entities = Stream.of(roms.stream(), disks.stream(), samples.stream()).flatMap(s->s).filter(t -> Anyware.filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
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

	public static TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return columnIndex < AnywareRenderer.columnsRenderers.length && AnywareRenderer.columnsRenderers[columnIndex] != null ? AnywareRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

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
		switch(columnIndex)
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

	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = Anyware.listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for(final Disk disk : disks)
		{
			final EntityStatus estatus = disk.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		for(final Rom rom : roms)
		{
			final EntityStatus estatus = rom.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		for(final Sample sample : samples)
		{
			final EntityStatus estatus = sample.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if(status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}

	public int countHave()
	{
		return countHaveRoms() + countHaveDisks() + countHaveSamples();
	}

	public int countHaveRoms()
	{
		return roms.stream().mapToInt(r -> r.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	public int countHaveDisks()
	{
		return disks.stream().mapToInt(d -> d.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	public int countHaveSamples()
	{
		return samples.stream().mapToInt(s -> s.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	public int countAll()
	{
		return roms.size() + disks.size() + samples.size();
	}

	@Override
	public Anyware getParent()
	{
		return getParent(Anyware.class);
	}

}
