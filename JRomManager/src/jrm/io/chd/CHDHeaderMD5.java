package jrm.io.chd;

import java.nio.MappedByteBuffer;

abstract class CHDHeaderMD5 extends CHDHeader implements CHDHeaderIntf
{
	private final String md5;

	protected CHDHeaderMD5(final MappedByteBuffer bb, final CHDHeader header, int position)
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		md5 = getHash(bb, position, 16);
	}

	@Override
	public String getMD5()
	{
		return md5;
	}
}
