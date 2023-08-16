package jrm.fx.ui.profile.filter;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.fx.ui.misc.Settings;
import jrm.fx.ui.profile.ProfileViewer;
import jrm.locale.Messages;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.filter.Keywords.KFCallBack;
import jrm.security.Session;
import jrm.security.Sessions;

public class Keywords extends Stage {

	private KeywordsController controller;
	private Session session;

	public Keywords(ProfileViewer parent, String[] keywords, AnywareList<? extends Anyware> awlist, KFCallBack callback) throws URISyntaxException, IOException {
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.WINDOW_MODAL);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> Settings.fromJson(session.getUser().getSettings().getProperty("Keywords.Bounds", null), this));
		setOnCloseRequest(e -> controller.onClose());
		final var loader = new FXMLLoader(getClass().getResource("Keywords.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<Scene>load();
		controller = loader.getController();
		controller.initKeywords(keywords);
		controller.callback = callback;
		controller.awlist = awlist;
		setScene(root);
		showAndWait();
	}
}
