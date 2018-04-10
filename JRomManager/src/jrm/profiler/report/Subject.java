package jrm.profiler.report;

import java.util.ArrayList;

import jrm.profiler.data.Machine;

public abstract class Subject
{
	protected Machine machine;
	
	protected ArrayList<Note> notes = new ArrayList<>();

	public Subject(Machine machine)
	{
		this.machine = machine;
	}
	
	public boolean add(Note note)
	{
		note.parent = this;
		return notes.add(note);
	}
	
	public abstract String toString();
}
