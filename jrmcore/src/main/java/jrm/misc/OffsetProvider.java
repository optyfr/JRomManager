package jrm.misc;

public interface OffsetProvider
{
	public int getOffset();
	public int[] freeOffsets();
}