package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

class CHDHeaderV3 extends CHDHeader implements CHDHeaderIntf
{
	private final String md5,sha1;

	public CHDHeaderV3(final MappedByteBuffer bb, final CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		bb.position(44);
		final byte[] md5 = new byte[16];
		bb.get(md5);
		this.md5 = CHDHeader.bytesToHex(md5);
		bb.position(80);
		final byte[] sha1 = new byte[20];
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
		return md5;
	}

}
