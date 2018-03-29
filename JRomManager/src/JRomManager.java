import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FileUtils;

import actions.ContainerAction;
import misc.BreakException;
import misc.Log;
import profiler.Import;
import profiler.Profile;
import profiler.Scan;
import profiler.scan.MergeOptions;
import ui.Progress;

public class JRomManager
{

	private JFrame frmJrommanager;
	
	private Profile curr_profile;
	private Scan curr_scan;
	
	private JButton btnScan;
	private JButton btnFix;
	private JTabbedPane tabbedPane;
	private JPanel Profiles;
	private JPanel scanner;
	private JPanel panel;
	private JCheckBox chckbxNeedSHA1;
	private JCheckBox chckbxUseParallelism;
	private JComboBox<MergeOptions> cbbxMergeMode;
	private JTree tree;
	private JList<Object> list_1;
	private JPanel panel_1;
	private JButton button;
	private JButton button_1;
	private JSplitPane splitPane;
	private JTextField txtRomsDest;
	private JButton btnRomsDest;
	private JLabel lblRomsDest;
	private JPanel panel_3;
	private JPanel panel_4;
	private JLabel lblMergeMode;
	private JTextField textSrcDir;
	private JLabel lblSrcDir;
	private JButton btnSrcDir;

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
		try
		{
			loadSettings();
		//	UIManager.setLookAndFeel(getProperty("LookAndFeel",  UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName()*/));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		initialize();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				if(curr_profile!=null)
					curr_profile.saveSettings();
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize()
	{
		frmJrommanager = new JFrame();
		frmJrommanager.setTitle("JRomManager");
		frmJrommanager.setBounds(100, 100, 458, 313);
		frmJrommanager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJrommanager.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmJrommanager.getContentPane().add(tabbedPane);
		
		Profiles = new JPanel();
		tabbedPane.addTab("Profiles", null, Profiles, null);
		GridBagLayout gbl_Profiles = new GridBagLayout();
		gbl_Profiles.columnWidths = new int[]{0, 0};
		gbl_Profiles.rowHeights = new int[]{0, 0, 0};
		gbl_Profiles.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_Profiles.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		Profiles.setLayout(gbl_Profiles);
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.25);
		splitPane.setOneTouchExpandable(true);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		Profiles.add(splitPane, gbc_splitPane);
		
		tree = new JTree();
		splitPane.setLeftComponent(tree);
		tree.setSelectionRows(new int[] {5});
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("JTree") {
				{
				}
			}
		));
		tree.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tree.setRootVisible(false);
		tree.setEditable(true);
		
		list_1 = new JList<Object>();
		splitPane.setRightComponent(list_1);
		list_1.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		Profiles.add(panel_1, gbc_panel_1);
		
		button = new JButton("Load Profile");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadProfile();
			}
		});
		panel_1.add(button);
		
		button_1 = new JButton("Import Dat");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importDat();
			}
		});
		panel_1.add(button_1);
		
		scanner = new JPanel();
		tabbedPane.addTab("Scanner", null, scanner, null);
		tabbedPane.setEnabledAt(1, false);
		GridBagLayout gbl_scanner = new GridBagLayout();
		gbl_scanner.columnWidths = new int[]{104, 0};
		gbl_scanner.rowHeights = new int[]{0, 33, 0};
		gbl_scanner.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_scanner.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		scanner.setLayout(gbl_scanner);
		
		panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		scanner.add(panel_3, gbc_panel_3);
		
		btnScan = new JButton("Scan");
		panel_3.add(btnScan);
		btnScan.setEnabled(false);
		
		btnFix = new JButton("Fix");
		panel_3.add(btnFix);
		btnFix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fix();
			}
		});
		btnFix.setEnabled(false);
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scan();
			}
		});
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.ipady = 20;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		scanner.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{301, 0};
		gbl_panel.rowHeights = new int[]{20, 20, 20, 0, 20, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		chckbxNeedSHA1 = new JCheckBox("Calculate all SHA1");
		chckbxNeedSHA1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				curr_profile.setProperty("need_sha1", e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxNeedSHA1.setToolTipText("Calculate SHA1 while scanning new files, even if CRC is not suspicious (Slow process)");
		GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		panel.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);
		
		chckbxUseParallelism = new JCheckBox("Enable MultiThreading");
		chckbxUseParallelism.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				curr_profile.setProperty("use_parallelism", e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxUseParallelism.setToolTipText("Use all CPU while scanning and fixing, SSD is STRONGLY recommended otherwise you may get slower results!");
		GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		panel.add(chckbxUseParallelism, gbc_chckbxUseParallelism);
		
		panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		panel.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 8, 0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		lblMergeMode = new JLabel("Merge Mode");
		GridBagConstraints gbc_lblMergeMode = new GridBagConstraints();
		gbc_lblMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblMergeMode.anchor = GridBagConstraints.EAST;
		gbc_lblMergeMode.gridx = 0;
		gbc_lblMergeMode.gridy = 0;
		panel_4.add(lblMergeMode, gbc_lblMergeMode);
		
		cbbxMergeMode = new JComboBox<>();
		GridBagConstraints gbc_cbbxMergeMode = new GridBagConstraints();
		gbc_cbbxMergeMode.insets = new Insets(0, 0, 5, 0);
		gbc_cbbxMergeMode.gridwidth = 2;
		gbc_cbbxMergeMode.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxMergeMode.gridx = 1;
		gbc_cbbxMergeMode.gridy = 0;
		panel_4.add(cbbxMergeMode, gbc_cbbxMergeMode);
		cbbxMergeMode.setToolTipText("Select the Merge mode");
		cbbxMergeMode.setModel(new DefaultComboBoxModel<MergeOptions>(MergeOptions.values()));
		cbbxMergeMode.setSelectedIndex(3);
		
		lblRomsDest = new JLabel("Roms Dest.");
		GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
		gbc_lblRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblRomsDest.gridx = 0;
		gbc_lblRomsDest.gridy = 1;
		panel_4.add(lblRomsDest, gbc_lblRomsDest);
		
		txtRomsDest = new JTextField();
		txtRomsDest.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText());
			}
		});
		GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 1;
		panel_4.add(txtRomsDest, gbc_txtRomsDest);
		txtRomsDest.setColumns(10);
		
		btnRomsDest = new JButton("...");
		btnRomsDest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					setFileSelectionMode(DIRECTORIES_ONLY);
					setSelectedFile(new File(txtRomsDest.getText()));
					setDialogTitle("Choose Roms Destination");
					if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
					{
						txtRomsDest.setText(getSelectedFile().getAbsolutePath());
						curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText());
					}
				}};
			}
		});
		GridBagConstraints gbc_btnRomsDest = new GridBagConstraints();
		gbc_btnRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_btnRomsDest.gridx = 2;
		gbc_btnRomsDest.gridy = 1;
		panel_4.add(btnRomsDest, gbc_btnRomsDest);
		
		lblSrcDir = new JLabel("Src Dir.");
		GridBagConstraints gbc_lblSrcDir = new GridBagConstraints();
		gbc_lblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcDir.anchor = GridBagConstraints.EAST;
		gbc_lblSrcDir.gridx = 0;
		gbc_lblSrcDir.gridy = 2;
		panel_4.add(lblSrcDir, gbc_lblSrcDir);
		
		textSrcDir = new JTextField();
		textSrcDir.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				curr_profile.setProperty("src_dir", textSrcDir.getText());
			}
		});
		GridBagConstraints gbc_textSrcDir = new GridBagConstraints();
		gbc_textSrcDir.insets = new Insets(0, 0, 5, 0);
		gbc_textSrcDir.fill = GridBagConstraints.BOTH;
		gbc_textSrcDir.gridx = 1;
		gbc_textSrcDir.gridy = 2;
		panel_4.add(textSrcDir, gbc_textSrcDir);
		
		btnSrcDir = new JButton("...");
		btnSrcDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					setFileSelectionMode(DIRECTORIES_ONLY);
					setSelectedFile(new File(textSrcDir.getText()));
					setDialogTitle("Choose Source Dir");
					if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
					{
						textSrcDir.setText(getSelectedFile().getAbsolutePath());
						curr_profile.setProperty("src_dir", textSrcDir.getText());
					}
				}};
			}
		});
		GridBagConstraints gbc_btnSrcDir = new GridBagConstraints();
		gbc_btnSrcDir.insets = new Insets(0, 0, 5, 0);
		gbc_btnSrcDir.gridx = 2;
		gbc_btnSrcDir.gridy = 2;
		panel_4.add(btnSrcDir, gbc_btnSrcDir);
		
		frmJrommanager.pack();
	}

	@SuppressWarnings("serial")
	private void importDat()
	{
		try
		{
			new JFileChooser()
			{{
				addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
				addChoosableFileFilter(new FileNameExtensionFilter("Mame executable", "exe"));
				if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
				{
					Import imprt = new Import(getSelectedFile());
					new JFileChooser()
					{{
						setFileSelectionMode(JFileChooser.FILES_ONLY);
						setDialogType(JFileChooser.SAVE_DIALOG);
						setSelectedFile(imprt.file);
						if(showSaveDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
						{
							FileUtils.copyFile(imprt.file, getSelectedFile());
						}
					}};
				}
			}};
		}
		catch (IOException e)
		{
			Log.err("Encountered IO Exception", e);
		}
		
	}
	
	@SuppressWarnings("serial")
	private void loadProfile()
	{
		if(curr_profile!=null)
			curr_profile.saveSettings();
		new JFileChooser()
		{{
			File workdir = Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile();
			setCurrentDirectory(workdir);
			addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
			if(showOpenDialog(frmJrommanager) == JFileChooser.APPROVE_OPTION)
			{
				final Progress progress = new Progress(frmJrommanager);
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
					boolean success = false;
					
					@Override
					protected Void doInBackground() throws Exception
					{
						success = (null!=(curr_profile = Profile.load(getSelectedFile(),progress)));
						tabbedPane.setEnabledAt(1, success);
						btnScan.setEnabled(success);
						btnFix.setEnabled(false);
						return null;
					}
					
					@Override
					protected void done() {
						progress.dispose();
						if(success && curr_profile!=null)
						{
							initScanSettings();
							tabbedPane.setSelectedIndex(1);
						}
					}
					
				};
				worker.execute();
				progress.setVisible(true);
			}
		}};
	}
	
	private void scan()
	{
		String txtdstdir = txtRomsDest.getText();
		if(txtdstdir.isEmpty())
		{
			btnRomsDest.doClick();
			txtdstdir = txtRomsDest.getText();
		}
		if(txtdstdir.isEmpty())
			return;
		File dstdir = new File(txtdstdir);
		if(!dstdir.isDirectory())
			return;
		
		String txtsrcdir = textSrcDir.getText();
		List<File> srcdirs = new ArrayList<>();
		if(!txtsrcdir.isEmpty())
		{
			File srcdir = new File(txtsrcdir);
			if(!srcdir.isDirectory())
				return;
			srcdirs.add(srcdir);
		}

		final Progress progress = new Progress(frmJrommanager);
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() throws Exception
			{
				curr_scan = new Scan(curr_profile, dstdir, srcdirs, progress);
				AtomicInteger actions_todo = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> actions_todo.addAndGet(actions.size()));
				btnFix.setEnabled(actions_todo.get()>0);
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
	
	private void fix()
	{
		final Progress progress = new Progress(frmJrommanager);
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() throws Exception
			{
				boolean use_parallelism = curr_profile.getProperty("use_parallelism", false);
				AtomicInteger i = new AtomicInteger(0), max = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> max.addAndGet(actions.size()));
				progress.setProgress("Fixing...", i.get(), max.get());
				curr_scan.actions.forEach(actions -> {
					List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
					(use_parallelism?actions.parallelStream():actions.stream()).forEach(action -> {
						if (!action.doAction(progress))
							throw new BreakException();
						done.add(action);
						if(progress.isCancel())
							throw new BreakException();
						progress.setProgress(null, i.incrementAndGet());
					});
					actions.removeAll(done);
				});
				AtomicInteger actions_remain = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> actions_remain.addAndGet(actions.size()));
				btnFix.setEnabled(actions_remain.get()>0);
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
	
	public void initScanSettings()
	{
		chckbxNeedSHA1.setSelected(curr_profile.getProperty("need_sha1", false));
		chckbxUseParallelism.setSelected(curr_profile.getProperty("use_parallelism", false));
		txtRomsDest.setText(curr_profile.getProperty("roms_dest_dir", ""));
		textSrcDir.setText(curr_profile.getProperty("src_dir", ""));
	}

	private Properties settings = null;
	
	private File getSettingsFile()
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir, "settings");
		File settingsfile = new File(cachedir, getClass().getCanonicalName() + ".xml");
		settingsfile.getParentFile().mkdirs();
		return settingsfile;
		
	}

	public void saveSettings()
	{
		if(settings==null)
			settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(getSettingsFile()))
		{
			settings.storeToXML(os, null);
		}
		catch (IOException e)
		{
			Log.err("IO", e);
		}
	}
	
	public void loadSettings()
	{
		if(settings==null)
			settings = new Properties();
		if(getSettingsFile().exists())
		{
			try(FileInputStream is = new FileInputStream(getSettingsFile()))
			{
				settings.loadFromXML(is);
			}
			catch (IOException e)
			{
				Log.err("IO", e);
			}
		}
	}
	
	public void setProperty(String property, boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}
	
	public void setProperty(String property, String value)
	{
		settings.setProperty(property, value);
	}
	
	public boolean getProperty(String property, boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}
	
	public String getProperty(String property, String def)
	{
		return settings.getProperty(property, def);
	}
}
