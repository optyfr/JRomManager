package io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeaderV5 extends CHDHeader implements CHDHeaderIntf
{
	private String sha1;
	
	public CHDHeaderV5(MappedByteBuffer bb, CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		this.tag = header.tag;
		this.len = header.len;
		this.version = header.version;
		byte[] sha1 = new byte[20];
		bb.position(84);
		bb.get(sha1);
		this.sha1 = bytesToHex(sha1);
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
