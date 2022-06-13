package jrm.fx.ui;

import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;

public interface ProfileLoader
{
	public void loadProfile(final Session session, final ProfileNFO profile);
}
