package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.security.Sessions;
import lombok.Getter;

public class MainFrame extends Application
{
	private static @Getter MainFrameController controller;

	public static void launch()
	{
		Application.launch();
	}

	@Override
	public void start(Stage primaryStage)
	{
		System.out.println("Starting...");
		final var loading = new Loading();
		Platform.runLater(new Runnable()
		{
			public void run()
			{
				try
				{
					final var loader = new FXMLLoader(getClass().getResource("MainFrame.fxml").toURI().toURL(), Messages.getBundle());
					final var root = loader.<TabPane>load();
					controller = loader.getController();
					root.getStylesheets().add(getClass().getResource("MainFrame.css").toExternalForm());
					primaryStage.getIcons().add(getIcon("/jrm/resicons/rom.png"));
					primaryStage.setTitle(Messages.getString("MainFrame.Title") + " " + getVersion());
					primaryStage.setScene(new Scene(root));
					primaryStage.show();
				}
				catch (URISyntaxException | IOException e)
				{
					e.printStackTrace();
				}
				loading.hide();
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (Sessions.getSingleSession().getCurrProfile() != null)
				Sessions.getSingleSession().getCurrProfile().saveSettings();
			Sessions.getSingleSession().getUser().getSettings().saveSettings();
			System.out.println("Shutdown");
		}));
	}

	private static HashMap<String, Image> iconsCache = new HashMap<>();
	private static Optional<Module> iconsModule = ModuleLayer.boot().findModule("res.icons");

	public static Image getIcon(String res)
	{
		if (!iconsCache.containsKey(res))
		{
			iconsModule.ifPresentOrElse(module -> {
				try (final var in = module.getResourceAsStream(res))
				{
					if (in != null)
						iconsCache.put(res, new Image(in));
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
			}, () -> {
				try (final var in = MainFrame.class.getResourceAsStream(res))
				{
					if (in != null)
						iconsCache.put(res, new Image(in));
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
			});
		}
		return iconsCache.get(res);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	private String getVersion()
	{
		String version = ""; //$NON-NLS-1$
		final var pkg = getClass().getPackage();
		if (pkg.getSpecificationVersion() != null)
		{
			version += pkg.getSpecificationVersion(); // $NON-NLS-1$
			if (pkg.getImplementationVersion() != null)
				version += "." + pkg.getImplementationVersion(); //$NON-NLS-1$
		}
		return version;
	}
}
