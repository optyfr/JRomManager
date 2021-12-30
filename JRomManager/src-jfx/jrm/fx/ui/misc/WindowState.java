package jrm.fx.ui.misc;

import javafx.stage.Stage;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class WindowState
{
	private double x;
	private double y;
	private double w;
	private double h;
	private boolean m = false;
	private boolean f = false;
	private boolean i = false;

	private WindowState(Stage window)
	{
		m = window.isMaximized();
		f = window.isFullScreen();
		i = window.isIconified();
		window.setIconified(false);
		window.setFullScreen(false);
		window.setMaximized(false);
		x = window.getX();
		y = window.getY();
		w = window.getWidth();
		h = window.getHeight();
	}

	public static WindowState getInstance(Stage window)
	{
		return new WindowState(window);
	}

	public void restore(Stage window)
	{
		window.setX(x);
		window.setY(y);
		window.setWidth(w);
		window.setHeight(h);
		window.setMaximized(m);
		window.setFullScreen(f);
		window.setIconified(i);
	}
}
