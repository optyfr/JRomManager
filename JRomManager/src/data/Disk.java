package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Disk extends Entity implements Serializable
{
	public String sha1 = null;
	public String merge = null;
	public String status = "";
	
	public Disk()
	{
	}

	@Override
	public String getName()
	{
		return name+".chd";
	}
	
	@Override
	public void setName(String name)
	{
		this.name=name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Disk)
		{
			if(((Disk)obj).sha1!=null && this.sha1 !=null)
				return ((Disk)obj).sha1.equals(this.sha1);
		}
		return super.equals(obj);
	}
}
