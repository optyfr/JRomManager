package jrm.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

class MsgDigest extends MDigest
{
	private MessageDigest digest;
	
	MsgDigest(String algorithm) throws NoSuchAlgorithmException
	{
		digest = MessageDigest.getInstance(algorithm);
	}
	
	@Override
	public void update(byte[] input, int offset, int len)
	{
		digest.update(input, offset, len);
	}
	
	@Override
	public String toString()
	{
		return Hex.encodeHexString(digest.digest());
	}

	@Override
	public String getAlgorithm()
	{
		return digest.getAlgorithm();
	}

	@Override
	public void reset()
	{
		digest.reset();
	}
}