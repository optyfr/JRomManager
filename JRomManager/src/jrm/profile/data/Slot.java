package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Slot extends ArrayList<SlotOption> implements Serializable
{
	public String name;
}
