package jrm.ui;

import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;

interface ProfileLoader
{
	public void loadProfile(final Session session, final ProfileNFO profile);
}
