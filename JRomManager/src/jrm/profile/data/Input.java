package jrm.profile.data;

import java.io.Serializable;

import org.apache.commons.lang3.BooleanUtils;

@SuppressWarnings("serial")
public final class Input implements Serializable
{
	public int players = 0;
	public int coins = 0;
	public boolean service = false;
	public boolean tilt = false;
	
	Input()
	{
	}
	
	public void setPlayers(String players)
	{
		this.players = Integer.parseInt(players);
	}
	
	public void setCoins(String coins)
	{
		this.coins = Integer.parseInt(coins);
	}

	public void setService(String service)
	{
		this.service = BooleanUtils.toBoolean(service);
	}

	public void setTilt(String tilt)
	{
		this.tilt = BooleanUtils.toBoolean(tilt);
	}
}
