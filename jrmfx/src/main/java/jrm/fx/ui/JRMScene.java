package jrm.fx.ui;

import javafx.beans.NamedArg;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.Setter;

public class JRMScene extends Scene
{
	public enum StyleSheet
	{
		SYSTEM(null), XS("XS.css"), S("S.css"), M("M.css"), L("L.css"), XL("XL.css");

		@Getter
		private String fileName;

		private StyleSheet(String fileName)
		{
			this.fileName = fileName;
		}
	}
	
	@Getter @Setter private static StyleSheet sheet = StyleSheet.XL;
	
	private String[] orgSheets;
	
	public JRMScene(@NamedArg("root") Parent root, @NamedArg(value="width", defaultValue="-1") double width, @NamedArg(value="height", defaultValue="-1") double height, @NamedArg("depthBuffer") boolean depthBuffer, @NamedArg(value="antiAliasing", defaultValue="DISABLED") SceneAntialiasing antiAliasing)
	{
		super(root, width, height, depthBuffer, antiAliasing);
		initSheets();
	}

	public JRMScene(@NamedArg("root") Parent root, @NamedArg(value="width", defaultValue="-1") double width, @NamedArg(value="height", defaultValue="-1") double height, @NamedArg("depthBuffer") boolean depthBuffer)
	{
		super(root, width, height, depthBuffer);
		initSheets();
	}

	public JRMScene(@NamedArg("root") Parent root, @NamedArg(value="width", defaultValue="-1") double width, @NamedArg(value="height", defaultValue="-1") double height, @NamedArg(value="fill", defaultValue="WHITE") Paint fill)
	{
		super(root, width, height, fill);
		initSheets();
	}

	public JRMScene(@NamedArg("root") Parent root, @NamedArg(value="width", defaultValue="-1") double width, @NamedArg(value="height", defaultValue="-1") double height)
	{
		super(root, width, height);
		initSheets();
	}

	public JRMScene(@NamedArg("root") Parent root, Paint fill)
	{
		super(root, fill);
		initSheets();
	}

	public JRMScene(@NamedArg("root") Parent root)
	{
		super(root);
		initSheets();
	}
	
	private void initSheets()
	{
		final var styleSheets = getStylesheets();
		orgSheets = styleSheets.toArray(String[]::new);
		applySheet(sheet);
	}

	public void applySheet(StyleSheet ss)
	{
		getStylesheets().clear();
		getStylesheets().addAll(orgSheets);
		setSheet(ss);
		applySheet(this);
	}

	public static void applySheet(Scene scene)
	{
		if(sheet.fileName!=null)
		{
			final var url = Scene.class.getResource("/jrm/fx/ui/css/%s".formatted(sheet.fileName)).toExternalForm();
			scene.getStylesheets().add(url);
		}
	}
}
