import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import actions.SetAction;
import profiler.Import;
import profiler.Profile;
import profiler.Scan;
import ui.Progress;

public class JRomManager
{

	private JFrame frmJrommanager;
	
	private Profile curr_profile;
	private Scan curr_scan;
	
	private JButton btnScan;
	private JButton btnFix;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					JRomManager window = new JRomManager();
					window.frmJrommanager.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JRomManager()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frmJrommanager = new JFrame();
		frmJrommanager.setTitle("JRomManager");
		frmJrommanager.setBounds(100, 100, 478, 74);
		frmJrommanager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJrommanager.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnImport = new JButton("Import Dat");
		btnImport.addActionListener(new ActionListener() {
			@SuppressWarnings("serial")
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
					addChoosableFileFilter(new FileNameExtensionFilter("Mame executable", "exe"));
					if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
						new Import(getSelectedFile());
				}};
			}
		});
		frmJrommanager.getContentPane().add(btnImport);
		
		JButton btnLoad = new JButton("Load Profile");
		btnLoad.addActionListener(new ActionListener() {
			@SuppressWarnings("serial")
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
					if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
					{
						final Progress progress = new Progress(frmJrommanager);
						SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

							@Override
							protected Void doInBackground() throws Exception
							{
								boolean success = (null!=(curr_profile = Profile.load(getSelectedFile(),progress)));
								btnScan.setEnabled(success);
								btnFix.setEnabled(false);
								return null;
							}
							
							@Override
							protected void done() {
								progress.dispose();
							}
							
						};
						worker.execute();
						progress.setVisible(true);
					}
				}};
			}
		});
		frmJrommanager.getContentPane().add(btnLoad);
		
		btnScan = new JButton("Scan");
		btnScan.setEnabled(false);
		btnScan.addActionListener(new ActionListener() {
			@SuppressWarnings("serial")
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					setFileSelectionMode(DIRECTORIES_ONLY);
					setDialogTitle("Choose Roms Destination");
					if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
					{
						File dstdir = getSelectedFile();
						ArrayList<File> srcdirs = new ArrayList<>();
						new JFileChooser() {{
							setFileSelectionMode(DIRECTORIES_ONLY);
							setDialogTitle("Choose Roms Source");
							if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
								srcdirs.add(getSelectedFile());
						}};
						final Progress progress = new Progress(frmJrommanager);
						SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

							@Override
							protected Void doInBackground() throws Exception
							{
								curr_scan = new Scan(curr_profile, dstdir, srcdirs, progress);
								btnFix.setEnabled(curr_scan.actions.size()>0);
								return null;
							}
							
							@Override
							protected void done() {
								progress.dispose();
							}
							
						};
						worker.execute();
						progress.setVisible(true);
					}
				}};
			}
		});
		frmJrommanager.getContentPane().add(btnScan);
		
		btnFix = new JButton("Fix");
		btnFix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Progress progress = new Progress(frmJrommanager);
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

					@Override
					protected Void doInBackground() throws Exception
					{
						int i = 0;
						progress.setProgress("Fixing...", i, curr_scan.actions.size());
						Iterator<SetAction> actionsIterator = curr_scan.actions.iterator();
						while (actionsIterator.hasNext())
						{
							if (!actionsIterator.next().doAction(progress))
								break;
							actionsIterator.remove();
							progress.setProgress(null, ++i);
						}
						btnFix.setEnabled(curr_scan.actions.size()>0);
						return null;
					}
					
					@Override
					protected void done() {
						progress.dispose();
					}
					
				};
				worker.execute();
				progress.setVisible(true);
			}
		});
		btnFix.setEnabled(false);
		frmJrommanager.getContentPane().add(btnFix);
		frmJrommanager.pack();
	}

}
