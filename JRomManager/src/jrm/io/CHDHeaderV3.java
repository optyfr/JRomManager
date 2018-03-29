package jrm.io;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;

public class CHDHeaderV3 extends CHDHeader implements CHDHeaderIntf
{
	private String md5,sha1;
	
	public CHDHeaderV3(MappedByteBuffer bb, CHDHeader header) throws UnsupportedEncodingException
	{
		super();
		this.tag = header.tag;
		this.len = header.len;
		this.version = header.version;
		bb.position(44);
		byte[] md5 = new byte[16];
		bb.get(md5);
		this.md5 = bytesToHex(md5);
		bb.position(80);
		byte[] sha1 = new byte[20];
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
		return md5;
	}

}
