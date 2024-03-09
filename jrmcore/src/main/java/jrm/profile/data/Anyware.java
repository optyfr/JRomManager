/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Entity.Status;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import lombok.Getter;
import lombok.Setter;
import one.util.streamex.StreamEx;

/**
 * This class is the common class for machines and softwares sets
 * 
 * @author optyfr
 * 
 */
@SuppressWarnings("serial")
public abstract class Anyware extends AnywareBase implements Serializable, Systm {
	Profile profile;

	/**
	 * the name of the parent from which this instance is a clone, null if the
	 * instance is not a clone
	 */
	protected @Getter @Setter String cloneof = null;
	/**
	 * The description field, generally the complete name of the game
	 */
	public final StringBuilder description = new StringBuilder();
	/**
	 * The release year (may contain non numeric chars like question mark [?])
	 */
	public final StringBuilder year = new StringBuilder();

	/**
	 * The list of {@link Rom} entities related to this object
	 */
	private final @Getter Collection<Rom> roms = new ArrayList<>();
	/**
	 * The list of {@link Disk} entities related to this object
	 */
	private final @Getter Collection<Disk> disks = new ArrayList<>();
	/**
	 * The list of {@link Sample} entities related to this object
	 */
	private final @Getter Collection<Sample> samples = new ArrayList<>();

	/**
	 * A hash table of clones if this object has clones
	 */
	protected transient @Getter Map<String, Anyware> clones = new HashMap<>();

	/**
	 * Is that machine/software is *individually* selected for inclusion in your set
	 * ? (true by default)
	 */
//	private @Getter @Setter boolean selected = true;

	/**
	 * Do we have a collision?
	 */
	protected transient boolean collision;

	/**
	 * entities list cache (according current {@link Profile#filterEntities})
	 */
	private transient List<EntityBase> tableEntities;

	/**
	 * The constructor, will initialize transients fields
	 */
	protected Anyware(Profile profile) {
		this.profile = profile;
		initTransient();
	}

	/**
	 * the Serializable method for special serialization handling (in that case :
	 * initialize transient default values)
	 * 
	 * @param in the serialization inputstream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		initTransient();
	}

	/**
	 * The method called to initialize transient and static fields
	 */
	protected void initTransient() {
		collision = false;
		tableEntities = null;
		roms.forEach(r -> r.parent = this);
		disks.forEach(d -> d.parent = this);
		samples.forEach(s -> s.parent = this);
		clones = new HashMap<>();
	}

	/**
	 * is collision enabled
	 * 
	 * @return true if collision mode is enabled
	 */
	public boolean isCollisionMode() {
		return collision;
	}

	/**
	 * Enable collision
	 * 
	 * @param parent if true will also propagate to clones from parent
	 */
	public void setCollisionMode(final boolean parent) {
		if (parent)
			getDest().clones.forEach((n, m) -> m.collision = true);
		collision = true;
	}

	/**
	 * Reset collision, including for all roms and disks entities
	 */
	public void resetCollisionMode() {
		collision = false;
		roms.forEach(Rom::resetCollisionMode);
		disks.forEach(Disk::resetCollisionMode);
	}

	/**
	 * is this object a clone? A clone as a parent and that parent is not a bios
	 * 
	 * @return true is that object is effectively a clone
	 */
	public boolean isClone() {
		return (parent != null && !getParent().isBios());
	}

	/**
	 * get the destination object according the {@link ProfileSettings#mergeMode}
	 * and {@link #isClone()} - if we are merging ({@link MergeOptions#isMerge()}),
	 * then return a parent that is not a clone by returning the call from
	 * {@link #getParent()}.{@link #getDest()} - otherwise return {@code this}
	 * 
	 * @return the {@link Anyware} destination object (can be this instance or a
	 *         parent)
	 */
	public Anyware getDest() {
		if (profile.getSettings() != null && profile.getSettings().getMergeMode().isMerge() && isClone())
			return getParent().getDest();
		return this;
	}

	/**
	 * Is this object a bios
	 * 
	 * @return true if it's a bios (can be always false depending of extending
	 *         class)
	 */
	public abstract boolean isBios();

	/**
	 * Is this object depends of roms from another object?
	 * 
	 * @return true if it needs roms from another object (can be always false
	 *         depending of extending class)
	 */
	public abstract boolean isRomOf();

	/**
	 * return filtered list of disks according various flags and status
	 * 
	 * @return the filtered list of disks
	 * @see "Source code for more explanations"
	 */
	public List<Disk> filterDisks() {
		Stream<Disk> stream = getFilterDisksStream();

		/*
		 * Stream filtering
		 */
		return stream.filter(d -> {
			if (d.dumpStatus == Status.nodump) // exclude nodump disks
				return false;
			if (profile.getSettings().getMergeMode() == MergeOptions.SPLIT && containsInParent(this, d)) // exclude if
				// splitting
				// and the disk
				// is in parent
				return false;
			if (profile.getSettings().getMergeMode() == MergeOptions.NOMERGE && containsInParent(this, d)) // explicitely
				// include if
				// nomerge
				// and the
				// disk is in
				// parent
				return true;
			return isBios() || !containsInParent(this, d); // otherwise include
															// if I'm bios or
															// the disk is not
															// in parent
		}).collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	private Stream<Disk> getFilterDisksStream() {
		/*
		 * / Stream initialization
		 */
		Stream<Disk> stream;
		if (!isSelected()) // skip if not selected
			stream = Stream.empty();
		else if (profile.getSettings().getMergeMode().isMerge()) // if merging
		{
			if (isClone() && getParent().isSelected()) // skip if I'm a clone and my
													// parent is selected for
													// inclusion
				stream = Stream.empty();
			else // otherwise...
			{
				// concatenate my disks and all my clones disks into a stream
				final List<Disk> disksWithClones = Stream.concat(disks.stream(), clones.values().stream().flatMap(m -> m.disks.stream())).toList();
				// and mark for collision disks with same names but with
				// different hash (this
				// will change the way getName return disks names)
				StreamEx.of(disksWithClones).groupingBy(Disk::getName).forEach((n, l) -> {
					if (l.size() > 1 && StreamEx.of(l).distinct(Disk::hashString).count() > 1)
						l.forEach(Disk::setCollisionMode);
				});
				// and finally remove real duplicate disks
				stream = StreamEx.of(disksWithClones).distinct(Disk::getName);
			}
		} else // if not merging
			stream = disks.stream(); // simply initialize stream to current
										// object disks list
		return stream;
	}

	/**
	 * return filtered list of roms according various flags and status
	 * 
	 * @return the filtered list of roms
	 * @see "Source code for more explanations"
	 */
	public List<Rom> filterRoms() {
		final var list = getFilterRomsStream().filter(this::getRomsFilter)
				.collect(Collectors.toCollection(ArrayList::new));
		if (profile.getSettings().getMergeMode() == MergeOptions.SUPERFULLNOMERGE) {
			final var map = new HashMap<String, Rom>();
			list.forEach(r -> map.merge(r.getName(), r, (oldR, newR) -> oldR.parent == this ? oldR : newR));
			return new ArrayList<>(map.values());
		}
		return list;
	}

	/**
	 * @param r
	 * @return
	 */
	private boolean getRomsFilter(Rom r) {
		if (r.dumpStatus == Status.nodump || r.crc == null/* || "00000000".equals(r.crc)*/ || r.name.isEmpty())
			return false; // exclude nodump, nocrc, empty name
		if (Boolean.FALSE.equals(profile.getSettings().getProperty(ProfileSettingsEnum.zero_entry_matters, Boolean.class)) && "00000000".equals(r.crc) && r.size==0)
			return false;
		if (profile.getSettings().getMergeMode() == MergeOptions.SUPERFULLNOMERGE
				|| profile.getSettings().getMergeMode() == MergeOptions.FULLNOMERGE
				|| profile.getSettings().getMergeMode() == MergeOptions.FULLMERGE)
			return true; // Unconditionally include roms in SUPERFULLNOMERGE,
							// FULLNOMERGE, FULLMERGE
		// modes
		if (profile.getSettings().getMergeMode() == MergeOptions.SPLIT && containsInParent(this, r, false))
			return false; // exclude if splitting and the rom is in parent
		if (profile.getSettings().getMergeMode() == MergeOptions.NOMERGE && containsInParent(this, r, true))
			return false; // exclude if not merging and the rom is in BIOS
		if (profile.getSettings().getMergeMode() == MergeOptions.NOMERGE && Anyware.wouldMerge(this, r))
			return true; // include if not merging and the rom would be merged
							// in parent (when selected)
		if (profile.getSettings().getMergeMode() == MergeOptions.MERGE && containsInParent(this, r, true))
			return false; // exclude if merging and the rom is in BIOS
		if (profile.getSettings().getMergeMode() == MergeOptions.MERGE && Anyware.wouldMerge(this, r))
			return true; // include if merging and the rom would be merged in
							// parent (when selected)
		return isBios() || !containsInParent(this, r, false); // otherwise
																// include if
																// I'm bios or
																// if the rom is
																// not in
		// parent
	}

	/**
	 * @return
	 */
	private Stream<Rom> getFilterRomsStream() {
		/*
		 * Stream initialization
		 */
		Stream<Rom> stream;
		if (!isSelected()) // skip if not selected
			stream = Stream.empty();
		else if (profile.getSettings().getMergeMode().isMerge()) // if merging
		{
			stream = getFilterRomsMergingStream();
		} else // if not merging
		{
			if (profile.getSettings().getMergeMode().equals(MergeOptions.SUPERFULLNOMERGE)) // also include
																							// devices
			{
				if (Boolean.TRUE.equals(profile.getProperty(ProfileSettingsEnum.exclude_games, Boolean.class))) {
					if (profile.getProperty(ProfileSettingsEnum.exclude_machines, Boolean.class)) // NOSONAR
						stream = streamWithDevices(true, false, true); // bios-devices
					else
						stream = streamWithDevices(true, false, true); // machine-bios-devices
				} else
					stream = streamWithDevices(false, true, false); // all
			} else // simply initialize stream to current object disks list
				stream = roms.stream();
		}
		return stream;
	}

	/**
	 * @return
	 */
	private Stream<Rom> getFilterRomsMergingStream() {
		Stream<Rom> stream;
		if (isClone() && getParent().isSelected()) // skip if I'm a clone and my
												// parent is selected for
												// inclusion
			stream = Stream.empty();
		else // otherwise...
		{
			// concatenate my roms and all my clones roms into a stream
			final List<Rom> romsWithClones = Stream.concat(roms.stream(), clones.values().stream().flatMap(m -> m.roms.stream())).toList();
			// and mark for collision roms with same names but with different
			// hash (this
			// will change the way getName return roms names)
			StreamEx.of(romsWithClones).groupingBy(Rom::getName).forEach((n, l) -> {
				if (l.size() > 1 && StreamEx.of(l).distinct(Rom::hashString).count() > 1)
					l.forEach(Rom::setCollisionMode);
			});
			if (HashCollisionOptions.HALFDUMB == profile.getSettings().getHashCollisionMode()) // HALFDUMB extra stuffs
			// for PD
			{
				/*
				 * This will filter roms with same hash between clones (the encountered first
				 * clone collisioning rom is kept), and also remove parent roms from clone
				 * (implicit merge or not) note that roms dat declaration order is important, as
				 * well that clone name alphabetical order
				 */
				final LinkedHashMap<String, Rom> map = new LinkedHashMap<>();
				roms.forEach(r -> map.put(r.hashString(), r));
				clones.values().stream().sorted()
						.forEach(w -> w.roms.stream().sorted().forEach(r -> map.putIfAbsent(r.hashString(), r)));
				final List<Rom> clonesRoms = new ArrayList<>(map.values());
				clonesRoms.removeAll(roms);
				stream = Stream.concat(roms.stream(), clonesRoms.stream());
			} else // finally remove real duplicate disks
				stream = StreamEx.of(romsWithClones).distinct(Rom::getName);
		}
		return stream;
	}

	/**
	 * get a stream of roms for this current machine optionally including related
	 * bios and devices
	 * 
	 * @param excludeBios if true do not include bios
	 * @param partial     if true exclude device references
	 * @param recurse     if true will also get devices of devices
	 * @return a stream of roms
	 */
	abstract Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse);

	/**
	 * get a reversed order stream of a list of type {@link T}
	 * 
	 * @param <T>   any object type
	 * @param input a {@link List}{@literal <}{@link T}{@literal >} with {@link T}
	 *              any class type
	 * @return return a {@link Stream}{@literal <}{@link T}{@literal >} where
	 *         objects are starting from the end of the list
	 */
	public static <T> Stream<T> streamInReverse(List<T> input) {
		return IntStream.range(1, input.size() + 1).mapToObj(i -> input.get(input.size() - i));
	}

	/**
	 * Tell if an {@link Entity} (a rom or a disk) would be explicitly merged when
	 * in merge mode
	 * 
	 * @param ware the {@link Anyware} (a Machine or Software) for which the entity
	 *             is compared (it is not necessarily its direct parent)
	 * @param e    the entity to test
	 * @return true, if the {@link Entity} as merge flag and the {@link Anyware} as
	 *         a selected parent
	 */
	public static boolean wouldMerge(final Anyware ware, final Entity e) {
		return e.merge != null && ware.isRomOf() && ware.getParent() != null && ware.getParent().isSelected();
	}

	/**
	 * Tell whether a {@link Rom} is contained in the given {@link Anyware}'s
	 * ancestors This method is recursive and take care about implicit_merge option
	 * 
	 * @param ware     the {@link Anyware} to test against
	 * @param r        the {@link Rom} to find in the ware's ancestors
	 * @param onlyBios if true test only if ware's parent is a bios
	 * @return true if rom was found in one of the ware's ancestors
	 */
	public boolean containsInParent(final Anyware ware, final Rom r, final boolean onlyBios) {
		if ((r.merge != null || profile.getSettings().getImplicitMerge()) && ware.getParent() != null
				&& ware.getParent().isSelected()) {
			if ((!onlyBios || ware.getParent().isBios()) && ware.getParent().roms.contains(r))
				return true;
			return containsInParent(ware.getParent(), r, onlyBios);
		}
		return false;
	}

	/**
	 * Tell whether a {@link Disk} is contained in the given {@link Anyware}'s
	 * ancestors This method is recursive and take care about implicit_merge option
	 * 
	 * @param ware the {@link Anyware} to test against
	 * @param d    the {@link Disk} to find in the ware's ancestors
	 * @return true if disk was found in one of the ware's ancestors
	 */
	public boolean containsInParent(final Anyware ware, final Disk d) {
		if ((d.merge != null || profile.getSettings().getImplicitMerge()) && ware.getParent() != null
				&& ware.getParent().isSelected()) {
			if (ware.getParent().disks.contains(d))
				return true;
			return containsInParent(ware.getParent(), d);
		}
		return false;
	}

	/**
	 * resets entities list cache and fire a TableChanged event to listeners
	 */
	public void resetCache() {
		tableEntities = null;
	}

	/**
	 * Set a new Entity status set filter and reset list cache
	 * 
	 * @param filter the new entity status set filter to apply
	 */
	public void setFilterCache(final Set<EntityStatus> filter) {
		profile.setFilterEntities(filter);
	}

	/**
	 * get the entities current list cache
	 * 
	 * @return a {@link List}{@literal <}{@link EntityBase}{@literal >}
	 */
	public List<EntityBase> getEntities() {
		if (tableEntities == null)
			tableEntities = Stream.of(roms.stream(), disks.stream(), samples.stream()).flatMap(s -> s)
					.filter(t -> profile.getFilterEntities().contains(t.getStatus())).sorted()
					.collect(Collectors.toList());
		return tableEntities;
	}

	/**
	 * get the ware current status according the status of its attached entities
	 * (roms, disks, ...)
	 * 
	 * @return an {@link AnywareStatus}
	 */
	@Override
	public AnywareStatus getStatus() {
		AnywareStatus status = AnywareStatus.COMPLETE;
		final var ok = new AtomicBoolean();
		status = getDisksStatus(status, ok);
		status = getRomsStatus(status, ok);
		status = getSamplesStatus(status, ok);
		if (status == AnywareStatus.PARTIAL && !ok.get())
			status = AnywareStatus.MISSING;
		return status;
	}

	/**
	 * @param status
	 * @param ok
	 * @return
	 */
	private AnywareStatus getSamplesStatus(AnywareStatus status, final AtomicBoolean ok) {
		for (final Sample sample : samples) {
			final EntityStatus estatus = sample.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok.set(true);
			else if (estatus == EntityStatus.UNKNOWN) {
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		return status;
	}

	/**
	 * @param status
	 * @param ok
	 * @return
	 */
	private AnywareStatus getRomsStatus(AnywareStatus status, final AtomicBoolean ok) {
		for (final Rom rom : roms) {
			final EntityStatus estatus = rom.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok.set(true);
			else if (estatus == EntityStatus.UNKNOWN) {
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		return status;
	}

	/**
	 * @param status
	 * @param ok
	 * @return
	 */
	private AnywareStatus getDisksStatus(AnywareStatus status, final AtomicBoolean ok) {
		for (final Disk disk : disks) {
			final EntityStatus estatus = disk.getStatus();
			if (estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if (estatus == EntityStatus.OK)
				ok.set(true);
			else if (estatus == EntityStatus.UNKNOWN) {
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		return status;
	}

	/**
	 * count the number of correct entities we have in this ware
	 * 
	 * @return an int which is the total counted
	 */
	public int countHave() {
		return countHaveRoms() + countHaveDisks() + countHaveSamples();
	}

	/**
	 * count the number of correct ROMs we have in this ware
	 * 
	 * @return an int which is the counted ROMs
	 */
	public int countHaveRoms() {
		return roms.stream().mapToInt(r -> r.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * count the number of correct disks we have in this ware
	 * 
	 * @return an int which is the counted disks
	 */
	public int countHaveDisks() {
		return disks.stream().mapToInt(d -> d.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * count the number of correct samples we have in this ware
	 * 
	 * @return an int which is the counted samples
	 */
	public int countHaveSamples() {
		return samples.stream().mapToInt(s -> s.getStatus() == EntityStatus.OK ? 1 : 0).sum();
	}

	/**
	 * count the number of entities contained in this ware, whether they are OK or
	 * not
	 * 
	 * @return an int which is the sum of all the entities
	 */
	public int countAll() {
		return roms.size() + disks.size() + samples.size();
	}

	private transient HashMap<String,EntityStatus> clonesRomsStatus = null;
	
	public void resetClonesRomsStatus()
	{
		clonesRomsStatus = null;
	}
	
	EntityStatus getCloneRomStatus(Rom rom)
	{
		if(clonesRomsStatus == null)
		{
			clonesRomsStatus = new HashMap<>();
			if(clones.size()>0)
			{
				clones.values().stream().flatMap(aw -> aw.roms.stream()).filter(r -> r.ownStatus != EntityStatus.UNKNOWN).forEach(r -> {
					clonesRomsStatus.put(r.hashString(), r.ownStatus);
				});
			}
		}
		return clonesRomsStatus.get(rom.hashString());
	}
	
	@Override
	public Anyware getParent() {
		return getParent(Anyware.class);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Anyware aw)
			return this.name.equals(aw.name);
		return super.equals(obj);
	}

	@Override
	public Profile getProfile() {
		return profile;
	}

	public int count() {
		return getEntities().size();
	}

	public EntityBase getObject(int i) {
		return getEntities().get(i);
	}

	/**
	 * get the selection state in profile properties according  {@link #getPropertyName()}
	 * @return true if selected
	 */
	public boolean isSelected()
	{
		return profile.getProperty(getPropertyName(), true);
	}

	/**
	 * set the selection state in profile properties according {@link #getPropertyName()}
	 * @param selected the selection state to set
	 */
	public void setSelected(final boolean selected)
	{
		profile.setProperty(getPropertyName(), selected);
	}
}
