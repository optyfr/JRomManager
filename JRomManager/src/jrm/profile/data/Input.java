package jrm.profile.data;

import java.io.Serializable;

import org.apache.commons.lang3.BooleanUtils;

/**
 * Describe the input of a machine (used for filtering)
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Input implements Serializable
{
	/**
	 * Number of supported players
	 */
	public int players = 0;
	/**
	 * Number of coin slots
	 */
	public int coins = 0;
	/**
	 * does it have service mode
	 */
	public boolean service = false;
	/**
	 * does it support tilt
	 */
	public boolean tilt = false;

	Input()
	{
	}

	/**
	 * set number of players
	 * @param players the player string value that will be converted to int
	 */
	public void setPlayers(final String players)
	{
		this.players = Integer.parseInt(players);
	}

	/**
	 * set number of coins slots
	 * @param coins the coins string value that will be converted to int
	 */
	public void setCoins(final String coins)
	{
		this.coins = Integer.parseInt(coins);
	}

	/**
	 * set the service support
	 * @param service the service boolean yes/no string representation 
	 */
	public void setService(final String service)
	{
		this.service = BooleanUtils.toBoolean(service);
	}

	/**
	 * set the tilt support
	 * @param tilt the tilt boolean yes/no string representation 
	 */
	public void setTilt(final String tilt)
	{
		this.tilt = BooleanUtils.toBoolean(tilt);
	}
}
