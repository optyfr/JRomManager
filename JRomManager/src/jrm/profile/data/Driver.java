package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Driver implements Serializable
{
	private StatusType status = StatusType.preliminary;
	private StatusType emulation = StatusType.preliminary;
	private StatusType cocktail = StatusType.preliminary;
	private SaveStateType savestate = SaveStateType.unsupported;
	
	public enum StatusType
	{
		good,
		imperfect,
		preliminary
	};
	
	public enum SaveStateType
	{
		supported,
		unsupported
	};
	
	Driver()
	{
	}

	public void setStatus(String status)
	{
		this.status = StatusType.valueOf(status);
	}
	
	public void setEmulation(String status)
	{
		this.emulation = StatusType.valueOf(status);
	}
	
	public void setCocktail(String status)
	{
		this.cocktail = StatusType.valueOf(status);
	}
	
	public void setSaveState(String status)
	{
		this.savestate = SaveStateType.valueOf(status);
	}
	
	public StatusType getStatus()
	{
		return status;
	}
	
	public StatusType getEmulation()
	{
		return emulation;
	}

	public StatusType getCocktail()
	{
		return cocktail;
	}

	public SaveStateType getSaveState()
	{
		return savestate;
	}
}
