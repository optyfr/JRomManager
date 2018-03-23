package actions;

import java.nio.file.FileSystem;

import data.Entry;
import ui.ProgressHandler;

abstract public class RomAction
{
	Entry entry;
	SetAction parent;
	
	public RomAction(Entry entry)
	{
		this.entry = entry;
	}

	public abstract boolean doAction(FileSystem fs, ProgressHandler handler);
}
