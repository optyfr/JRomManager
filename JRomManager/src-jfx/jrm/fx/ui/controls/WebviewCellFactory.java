package jrm.fx.ui.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

public class WebviewCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>>
{
	@Override
	public TableCell<S, T> call(TableColumn<S, T> column)
	{
		return new TableCell<S, T>()
		{
			@Override
			protected void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null || empty)
				{
					setText(null);
					setGraphic(null);
					setStyle("");
				}
				else
				{
					WebView webview = new WebView();
					WebEngine engine = webview.getEngine();
					webview.setPrefHeight(-1); // <- Absolute must at this position (before calling the Javascript)
					webview.setBlendMode(BlendMode.DARKEN);
					webview.setFontScale(0.75);
					setGraphic(webview);
					engine.loadContent("<body topmargin=0 leftmargin=0 style=\"background-color: transparent;white-space:nowrap;overflow:hidden;text-overflow:ellipsis\">" + item + "</body>");
/*					engine.documentProperty().addListener((obj, prev, newv) -> {
						String heightText = engine.executeScript( // <- Some modification, which gives moreless the same
																	// result than the original
								"var body = document.body," + "html = document.documentElement;" + "Math.max( body.scrollHeight , body.offsetHeight, " + "html.clientHeight, html.scrollHeight , html.offsetHeight );").toString();
						Double height = Double.parseDouble(heightText.replace("px", "")) + 10; // <- Why are this 15.0
																								// required??
						webview.setPrefHeight(height);
						this.setPrefHeight(height);
					});*/
				}
			}
		};
	}
}
