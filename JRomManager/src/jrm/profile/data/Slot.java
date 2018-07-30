package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * a Slot of {@link SlotOption}s with {@link Device}s
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Slot extends ArrayList<SlotOption> implements Serializable
{
	/**
	 * the name of the Slot
	 */
	public String name;
}
