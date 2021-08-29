package jrm.io.chd;

import java.nio.MappedByteBuffer;

abstract class CHDHeaderSHA1 extends CHDHeader implements CHDHeaderIntf
{
	private final String sha1;

	protected CHDHeaderSHA1(final MappedByteBuffer bb, final CHDHeader header, int position)
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		sha1 = getHash(bb, position, 20);
	}

	@Override
	public String getSHA1()
	{
		return sha1;
	}
}
