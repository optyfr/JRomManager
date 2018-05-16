package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeaderV5 extends CHDHeader implements CHDHeaderIntf
{
	private final String sha1;

	public CHDHeaderV5(final MappedByteBuffer bb, final CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		final byte[] sha1 = new byte[20];
		bb.position(84);
		bb.get(sha1);
		this.sha1 = CHDHeader.bytesToHex(sha1);
	}

	@Override
	public String getSHA1()
	{
		return sha1;
	}

	@Override
	public String getMD5()
	{
		return null;
	}

}
