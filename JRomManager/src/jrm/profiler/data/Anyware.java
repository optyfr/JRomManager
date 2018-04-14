package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;

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
	
}
