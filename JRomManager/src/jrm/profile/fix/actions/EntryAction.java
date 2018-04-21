package jrm.profile.fix.actions;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import jrm.compressors.Archive;
import jrm.misc.HTMLRenderer;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

abstract public class EntryAction implements HTMLRenderer
{
	Entry entry;
	ContainerAction parent;

	public EntryAction(Entry entry)
	{
		this.entry = entry;
	}

	public abstract boolean doAction(Archive archive, ProgressHandler handler);

	public abstract boolean doAction(FileSystem fs, ProgressHandler handler);

	public abstract boolean doAction(Path target, ProgressHandler handler);
}
