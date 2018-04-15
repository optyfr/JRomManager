package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class SoftwareList implements Serializable
{
	public String name;	// required
	public StringBuffer description = new StringBuffer();

	public List<Software> softwares = new ArrayList<>();
	public Map<String, Software> softwares_byname = new HashMap<>();

	public SoftwareList()
	{
	}
	
	public boolean add(Software software)
	{
		software.list = this;
		softwares_byname.put(software.name, software);
		return softwares.add(software);
	}

}
