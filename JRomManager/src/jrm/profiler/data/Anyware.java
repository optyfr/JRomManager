package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jrm.profiler.data.Entity.Status;
import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public abstract class Anyware implements Serializable
{
	public String name;	// required
	public String cloneof = null;
	public StringBuffer description = new StringBuffer();

	public ArrayList<Rom> roms = new ArrayList<>();
	public ArrayList<Disk> disks = new ArrayList<>();

	public HashMap<String, Anyware> clones = new HashMap<>();
	
	public Anyware parent = null;

	public static transient MergeOptions merge_mode;
	public static transient HashCollisionOptions hash_collision_mode;
	protected transient boolean collision = false;

	public Anyware()
	{
		// TODO Auto-generated constructor stub
	}

	public <T extends Anyware> T getParent(Class<T> type)
	{
		return type.cast(parent);
	}
	
	public abstract Anyware getParent();
	
	public boolean isCollisionMode()
	{
		return collision;
	}

	public void setCollisionMode(boolean parent)
	{
		if(parent)
			getDest(merge_mode).clones.forEach((n, m) -> m.collision = true);
		this.collision = true;
	}
	
	public void resetCollisionMode()
	{
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

	public abstract boolean isClone();

	public Anyware getDest(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		if(merge_mode.isMerge() && isClone())
			return getParent().getDest(merge_mode);
		return this;
	}
	
	public abstract boolean isBios();
	public abstract boolean isRomOf();
	
	public List<Disk> filterDisks(MergeOptions merge_mode, HashCollisionOptions hash_collision_mode)
	{
		Machine.merge_mode = merge_mode;
		Machine.hash_collision_mode = hash_collision_mode;
		Stream<Disk> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				List<Disk> disks_with_clones = Stream.concat(disks.stream(), clones.values().stream().flatMap(m -> m.disks.stream())).collect(Collectors.toList());
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
			if(d.status==Status.nodump)
				return false;
			if(merge_mode == MergeOptions.SPLIT && d.merge != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && d.merge != null)
				return true;
			return this.isBios() || !this.isRomOf() || d.merge == null;
		}).collect(Collectors.toList());
	}

	public List<Rom> filterRoms(MergeOptions merge_mode, HashCollisionOptions hash_collision_mode)
	{
		Machine.merge_mode = merge_mode;
		Machine.hash_collision_mode = hash_collision_mode;
		Stream<Rom> stream;
		if(merge_mode.isMerge())
		{
			if(isClone())
				stream = Stream.empty();
			else
			{
				List<Rom> roms_with_clones = Stream.concat(roms.stream(), clones.values().stream().flatMap(m -> m.roms.stream())).collect(Collectors.toList());
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
			if(r.status==Status.nodump)
				return false;
			if(r.crc == null)
				return false;
			if(merge_mode == MergeOptions.SPLIT && r.merge != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && r.bios != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.FULLNOMERGE && r.bios != null)
				return true;
			if(merge_mode == MergeOptions.FULLNOMERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.MERGE && r.bios != null)
				return false;
			if(merge_mode == MergeOptions.MERGE && r.merge != null)
				return true;
			if(merge_mode == MergeOptions.FULLMERGE && r.bios != null)
				return true;
			if(merge_mode == MergeOptions.FULLMERGE && r.merge != null)
				return true;
			return this.isBios() || !this.isRomOf() || r.merge == null;
		}).collect(Collectors.toList());
	}

}
