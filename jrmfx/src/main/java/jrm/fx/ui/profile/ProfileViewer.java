package jrm.fx.ui.profile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.fx.ui.JRMScene;
import jrm.fx.ui.MainFrame;
import jrm.fx.ui.misc.Settings;
import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Getter;

public class ProfileViewer extends Stage
{
	private ProfileViewerController controller;
	
	private Session session;
	
	@Getter private Profile profile;

	@Getter private static final AtomicInteger resetCounter = new AtomicInteger();

	private Timer resetTimer = null;

	public ProfileViewer(Stage parent) throws IOException, URISyntaxException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.NONE);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> Settings.fromJson(session.getUser().getSettings().getProperty("ProfileViewer.Bounds", null), this));
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("ProfileViewer.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ProfileViewer.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<JRMScene>load();
		controller = loader.getController();
		setScene(root);
		resetCounter.incrementAndGet();

		resetTimer = new Timer(true);
		resetTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				if(resetCounter.get() > 0)
				{
					Platform.runLater(() -> {
						if (MainFrame.getProfileViewer() != null)
						{
							resetCounter.set(0);
							MainFrame.getProfileViewer().reset(session.getCurrProfile());
						}
					});
					
				}
			}
		}, 0, 1000);
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
		this.profile = profile;
		controller.reset(profile);
	}
}
