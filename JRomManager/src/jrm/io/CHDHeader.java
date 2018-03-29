package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeader implements CHDHeaderIntf
{
	protected String tag;
	protected int len;
	protected int version;
	
	public CHDHeader(MappedByteBuffer bb) throws UnsupportedEncodingException
	{
		byte[] tag = new byte[8];
		bb.get(tag);
		this.tag= new String(tag,"ASCII");
		this.len = bb.getInt();
		this.version = bb.getInt();
	}

	protected CHDHeader() {}

	public boolean isValidTag()
	{
		return tag.equals("MComprHD");
	}
	
	public int getLen()
	{
		return len;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	private final static char[] hexArray = "0123456789abcdef".toCharArray();
	protected static String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public String getSHA1()
	{
		return null;
	}

	@Override
	public String getMD5()
	{
		return null;
	}
}
