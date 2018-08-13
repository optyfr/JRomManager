package jrm.digest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * A federating class between {@link MessageDigest} and {@link CRC32}. {@link #toString()} have to be implemented
 * @author optyfr
 */
public abstract class MDigest
{
	/**
	 * update digest with bytes input
	 * @param input the bytes input
	 * @param offset the offset to start (generally 0)
	 * @param len the len of bytes to read from input (starting from offset)
	 */
	public abstract void update(byte[] input, int offset, int len);

	/**
	 * call {@code update(byte[] input, 0, input.length)}
	 * @param input the bytes input
	 */
	public void update(byte[] input)
	{
		update(input, 0, input.length);
	}

	/**
	 * get the current named algorithm
	 * @return the same string used when getting instance with {@link #getAlgorithm(String)}
	 */
	public abstract String getAlgorithm();
	/**
	 * reset the digest to 0 (for reuse)
	 */
	public abstract void reset();
	
	/**
	 * return the hash as a lower-case hex string
	 */
	public abstract String toString();
	
	/**
	 * get appropriate MDigest instance according named algorithm
	 * @param algorithm CRC, MD5, SHA-1, SHA-256
	 * @return {@link MDigest}
	 * @throws NoSuchAlgorithmException
	 */
	public static MDigest getAlgorithm(String algorithm) throws NoSuchAlgorithmException
	{
		if(algorithm.equalsIgnoreCase("CRC"))
			return new CRCDigest();
		return new MsgDigest(algorithm);
	}

	/**
	 * Compute hashes from an {@link InputStream}
	 * @param is the {@link InputStream} to read
	 * @param md an array of {@link MDigest}
	 */
	public static MDigest[] computeHash(final InputStream in, final MDigest[] md) throws IOException
	{
		try(final InputStream is = new BufferedInputStream(in, 1024*1024))
		{
			final byte[] buffer = new byte[8192];
			int len = is.read(buffer);
			while(len != -1)
			{
				for(MDigest m : md)
					m.update(buffer, 0, len);
				len = is.read(buffer);
			}
		}
		return md;
	}
}