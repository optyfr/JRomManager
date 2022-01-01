package jrm.fx.ui.profile;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.fx.ui.misc.Settings;
import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.security.Session;
import jrm.security.Sessions;

public class ProfileViewer extends Stage
{
	private ProfileViewerController controller;
	
	private Session session;
	

	public ProfileViewer(Stage parent) throws IOException, URISyntaxException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.NONE);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
			Settings.fromJson(session.getUser().getSettings().getProperty("ProfileViewer.Bounds", null), this);
		});
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("ProfileViewer.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ProfileViewer.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<Scene>load();
		controller = loader.getController();
		setScene(root);
		reset(session.getCurrProfile());
	}
	
	public void clear()
	{
		controller.clear();
	}
	
	public void reload()
	{
		controller.reload();
	}
	
	public void reset(Profile profile)
	{
		controller.reset(profile);
	}
}
