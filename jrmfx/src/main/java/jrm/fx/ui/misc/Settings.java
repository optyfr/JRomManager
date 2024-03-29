package jrm.fx.ui.misc;

import com.google.gson.Gson;

import javafx.stage.Stage;
import jrm.misc.Log;

public class Settings
{
	private Settings()
	{
		// Do not instantiate
	}
	
	private static final Gson gson = new Gson();
	
	public static String toJson(Stage window)
	{
		return gson.toJson(WindowState.getInstance(window));
	}

	public static void fromJson(String json, Stage window)
	{
		if(json==null || json.isEmpty())
			return;
		try
		{
			gson.fromJson(json, WindowState.class).restore(window);
		}
		catch(Exception e)
		{
			Log.warn(e.getMessage());
		}
	}
}
