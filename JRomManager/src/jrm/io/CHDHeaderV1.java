package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeaderV1 extends CHDHeader implements CHDHeaderIntf
{
	private final String md5;

	public CHDHeaderV1(final MappedByteBuffer bb, final CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		bb.position(44);
		final byte[] md5 = new byte[16];
		bb.get(md5);
		this.md5 = CHDHeader.bytesToHex(md5);
	}

	@Override
	public String getSHA1()
	{
		return null;
	}

	@Override
	public String getMD5()
	{
		return md5;
	}

}
