package jrm.profiler.report;

public class RomSuspiciousCRC extends Subject
{
	String crc;
	
	public RomSuspiciousCRC(String crc)
	{
		super(null);
		this.crc = crc;
	}

	@Override
	public String toString()
	{
		return "Detected suspicious CRC : " + crc + " (SHA1 has been calculated for theses roms)";
	}

}
