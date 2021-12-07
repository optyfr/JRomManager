package jrm.fx.ui;

import java.util.HashMap;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jrm.locale.Messages;
import jrm.misc.Log;

public class MainFrame extends Application
{
	public static void launch()
	{
		Application.launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		System.out.println("start");
		final var root = FXMLLoader.<TabPane>load(getClass().getResource("MainFrame.fxml").toURI().toURL(), Messages.getBundle());
		root.getStylesheets().add(getClass().getResource("MainFrame.css").toExternalForm());
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
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
					if(in!=null)
						iconsCache.put(res, new Image(in));
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
			}, () -> {
				try (final var in = MainFrame.class.getResourceAsStream(res))
				{
					if(in!=null)
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

}
