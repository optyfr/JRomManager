package actions;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import data.Entry;
import ui.ProgressHandler;

abstract public class EntryAction
{
	Entry entry;
	ContainerAction parent;
	
	public EntryAction(Entry entry)
	{
		this.entry = entry;
	}

	public abstract boolean doAction(FileSystem fs, ProgressHandler handler);
	public abstract boolean doAction(Path target, ProgressHandler handler);
}
