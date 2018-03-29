package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeaderV1 extends CHDHeader implements CHDHeaderIntf
{
	private String md5;
	
	public CHDHeaderV1(MappedByteBuffer bb, CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		this.tag = header.tag;
		this.len = header.len;
		this.version = header.version;
		bb.position(44);
		byte[] md5 = new byte[16];
		bb.get(md5);
		this.md5 = bytesToHex(md5);
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
