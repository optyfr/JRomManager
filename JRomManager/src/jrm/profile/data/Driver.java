package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Driver implements Serializable
{
	public enum StatusType
	{
		good,
		imperfect,
		preliminary
	};
	
	public Driver()
	{
	}

}
