package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public class Machine implements Serializable
{
	public String name;
	public StringBuffer description = new StringBuffer();
	public String romof = null;
	public String cloneof = null;
	public String sampleof = null;
	public boolean isbios = false;
	public boolean ismechanical = false;
	public boolean isdevice = false;

	public ArrayList<Rom> roms = new ArrayList<>();
	public ArrayList<Disk> disks = new ArrayList<>();

	public Machine parent = null;
	public HashMap<String, Machine> clones = new HashMap<>();

	public static transient MergeOptions merge_mode;
	public static transient HashCollisionOptions hash_collision_mode;
	private transient boolean collision = false;

	public Machine()
	{
	}

	public void setCollisionMode(boolean parent)
	{
		if(parent)
			getDestMachine(merge_mode).clones.forEach((n, m) -> m.collision = true);
		collision = true;
	}

	public boolean isCollisionMode()
	{
		return collision;
	}

	public void resetCollisionMode()
	{
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

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
			if(d.status.equals("nodump"))
				return false;
			if(merge_mode == MergeOptions.SPLIT && d.merge != null)
				return false;
			if(merge_mode == MergeOptions.NOMERGE && d.merge != null)
				return true;
			return this.isbios || this.romof == null || d.merge == null;
		}).collect(Collectors.toList());
	}

	public boolean isClone()
	{
		return (parent != null && !parent.isbios);
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
			if(r.status.equals("nodump"))
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
			return isbios || romof == null || r.merge == null;
		}).collect(Collectors.toList());
	}

	public Machine getDestMachine(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		if(merge_mode.isMerge() && isClone())
			return parent.getDestMachine(merge_mode);
		return this;
	}
}
