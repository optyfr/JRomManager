package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

class CHDHeader implements CHDHeaderIntf
{
	protected String tag;
	protected int len;
	protected int version;

	public CHDHeader(final MappedByteBuffer bb) throws UnsupportedEncodingException
	{
		final byte[] tag = new byte[8];
		bb.get(tag);
		this.tag= new String(tag, "ASCII"); //$NON-NLS-1$
		len = bb.getInt();
		version = bb.getInt();
	}

	protected CHDHeader() {}

	@Override
	public boolean isValidTag()
	{
		return tag.equals("MComprHD"); //$NON-NLS-1$
	}

	@Override
	public int getLen()
	{
		return len;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	private final static char[] hexArray = "0123456789abcdef".toCharArray(); //$NON-NLS-1$
	protected static String bytesToHex(final byte[] bytes)
	{
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = CHDHeader.hexArray[v >>> 4];
			hexChars[j * 2 + 1] = CHDHeader.hexArray[v & 0x0F];
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
