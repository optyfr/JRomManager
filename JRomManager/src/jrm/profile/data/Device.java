package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Machine}'s device
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Device implements Serializable
{
	/**
	 * type of device
	 */
	public String type;
	/**
	 * tag name used for device
	 */
	public String tag = null;
	/**
	 * the interface name associated
	 */
	public String intrface = null;
	/**
	 * is this device available as media switch? commonly "1" or null
	 */
	public String fixed_image = null;
	/**
	 * is this device mandatory? commonly "1" or null
	 */
	public String mandatory = null;
	
	/**
	 * The {@link Instance} associated with this {@link Device}
	 */
	public Instance instance = null;
	
	/**
	 * The {@link List} of file {@link Extension}s supported for this {@link Device}
	 */
	public List<Extension> extensions = new ArrayList<>();
	
	/**
	 * The Instance associated with this {@link Device} 
	 */
	public class Instance implements Serializable
	{
		/**
		 * the instance name
		 */
		public String name;
		/**
		 * the instance brief name
		 */
		public String briefname = null;
	}
	
	/**
	 * File {@link Extension} associated with this {@link Device}
	 */
	public class Extension implements Serializable
	{
		/**
		 * The extension name
		 */
		public String name;
	}
}
