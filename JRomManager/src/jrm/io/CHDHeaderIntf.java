package jrm.io;

/**
 * Interface to get informations from CHD headers
 * @author optyfr
 *
 */
interface CHDHeaderIntf
{
	/**
	 * Does the file has a valid CHD header?
	 * @return true for valid, otherwise false
	 */
	boolean isValidTag();
	
	/**
	 * Header length
	 * @return length in bytes
	 */
	int getLen();
	
	/**
	 * Header version
	 * @return version number
	 */
	int getVersion();
	
	/**
	 * get the SHA1 (for uncompressed data), null if not reported by header
	 * @return the SHA1 string or null
	 */
	public String getSHA1();

	/**
	 * get the MD5 (for uncompressed data), null if not reported by header
	 * @return the MD5 string or null
	 */
	public String getMD5();
}
