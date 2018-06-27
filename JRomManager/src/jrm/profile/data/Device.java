package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public final class Device implements Serializable
{
	public String type; 
	public String tag = null; 
	public String intrface = null; 
	public String fixed_image = null; 
	public String mandatory = null;
	public Instance instance = null;
	public List<Extension> extensions = new ArrayList<>();
	
	public class Instance implements Serializable
	{
		public String name;
		public String briefname = null;
	}
	public class Extension implements Serializable
	{
		public String name;
	}
}
