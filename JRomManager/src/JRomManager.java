import java.awt.EventQueue;

import jrm.ui.MainFrame;

public final class JRomManager
{
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					MainFrame window = new MainFrame();
					window.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

}
