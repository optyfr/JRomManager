package jrm.io;

public interface CHDHeaderIntf
{
	boolean isValidTag();
	int getLen();
	int getVersion();
	public String getSHA1();
	public String getMD5();
}
