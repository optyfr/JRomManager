package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SoftwareList implements Serializable
{
	public String name;
	public String description;

	public List<Software> softwares = new ArrayList<>();

	public SoftwareList()
	{
	}
	
	public boolean add(Software software)
	{
		software.list = this;
		return softwares.add(software);
	}

}
