package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.profile.ProfileViewer;
import jrm.fx.ui.progress.ProgressTask;
import jrm.fx.ui.web.HTMLFormatter;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.profile.scan.options.ScanAutomation;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.security.Sessions;

public class ScannerPanelController implements Initializable, ProfileLoader
{
	private static final String DISK_ICON = "/jrm/resicons/icons/disk.png";

	@FXML	private Tab dirTab;
	
	@FXML	private Button infosBtn;
	@FXML	private Button scanBtn;
	@FXML	private Button reportBtn;
	@FXML	private Button fixBtn;
	@FXML	private Button importBtn;
	@FXML	private Button exportBtn;

	@FXML	private Button romsDestBtn;
	@FXML	private TextField romsDest;
	@FXML	private CheckBox disksDestCB;
	@FXML	private Button disksDestBtn;
	@FXML	private TextField disksDest;
	@FXML	private CheckBox swDestCB;
	@FXML	private Button swDestBtn;
	@FXML	private TextField swDest;
	@FXML	private CheckBox swDisksDestCB;
	@FXML	private Button swDisksDestBtn;
	@FXML	private TextField swDisksDest;
	@FXML	private CheckBox samplesDestCB;
	@FXML	private Button samplesDestBtn;
	@FXML	private TextField samplesDest;
	@FXML	private CheckBox backupDestCB;
	@FXML	private Button backupDestBtn;
	@FXML	private TextField backupDest;
	
	@FXML	private ListView<File> srcList;
	@FXML	private ContextMenu srcListMenu;
	@FXML	private MenuItem srcListAddMenuItem;
	@FXML	private MenuItem srcListDelMenuItem;

	@FXML	private Tab settingsTab;

	@FXML	private ScannerPanelSettingsController scannerPanelSettingsController; 
	
	@FXML	private Tab filterTab;
	@FXML	private Tab advFilterTab;
	@FXML	private Tab automationTab;

	@FXML	private WebView profileinfoLbl;

	final Session session = Sessions.getSingleSession();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		dirTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/folder.png")));
		settingsTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/cog.png")));
		filterTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/arrow_join.png")));
		advFilterTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/arrow_in.png")));
		automationTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/link.png")));
		
		romsDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		disksDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		disksDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			disksDest.setDisable(!newValue);
			disksDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(SettingsEnum.disks_dest_dir_enabled, newValue);
		});
		swDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		swDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			swDest.setDisable(!newValue);
			swDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(SettingsEnum.swroms_dest_dir_enabled, newValue);
		});
		swDisksDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		swDisksDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			swDisksDest.setDisable(!newValue);
			swDisksDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(SettingsEnum.swdisks_dest_dir_enabled, newValue);
		});
		samplesDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		samplesDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			samplesDest.setDisable(!newValue);
			samplesDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(SettingsEnum.samples_dest_dir_enabled, newValue);
		});
		backupDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		backupDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			backupDest.setDisable(!newValue);
			backupDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(SettingsEnum.backup_dest_dir_enabled, newValue);
		});
		infosBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png")));
		scanBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/magnifier.png")));
		reportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/report.png")));
		fixBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/tick.png")));
		importBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_refresh.png")));
		exportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_save.png")));
		
		srcList.setCellFactory(param -> new ListCell<File>()
		{
			@Override
			protected void updateItem(File item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty)
				{
					setText(null);
					setOnMouseClicked(null);
				}
				else
				{
					setText(item.toString());
					setOnMouseClicked(ev -> {
						if (ev.getClickCount() == 2)
						{
							chooseSrc(item, SettingsEnum.src_dir, "MainFrame.ChooseRomsSource");
						}
					});
				}
			}
		});
		srcListMenu.setOnShowing(e -> srcListDelMenuItem.setDisable(srcList.getSelectionModel().getSelectedIndex() < 0));
		srcListDelMenuItem.setOnAction(e -> {
			srcList.getItems().removeAll(srcList.getSelectionModel().getSelectedItems());
			saveSrcList();
		});
		srcListAddMenuItem.setOnAction(e -> chooseSrc(null, SettingsEnum.src_dir, "MainFrame.ChooseRomsSource"));
		new DragNDrop(romsDest).addDragDropFile(txt -> romsDest.setText(txt));
	}
	
	@Override
	public void loadProfile(Session session, ProfileNFO profile)
	{
		if (session.getCurrProfile() != null)
			session.getCurrProfile().saveSettings();
		
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().clear();

		try
		{
			final var thread = new Thread(new ProgressTask<Profile>((Stage) romsDest.getScene().getWindow())
			{

				@Override
				protected Profile call() throws Exception
				{
					return Profile.load(session, profile, this);
				}

				@Override
				protected void succeeded()
				{
					try
					{
						final var profile = get();
						session.getReport().setProfile(session.getCurrProfile());
						MainFrame.getReportFrame().setNeedUpdate(true);
						
						if (MainFrame.getProfileViewer() != null)
							MainFrame.getProfileViewer().reset(session.getCurrProfile());

						MainFrame.getController().getScannerPanelTab().setDisable(profile == null);
						scanBtn.setDisable(profile == null);
						fixBtn.setDisable(true);
						if (profile != null && session.getCurrProfile() != null)
						{
							profileinfoLbl.getEngine().loadContent(HTMLFormatter.toHTML(session.getCurrProfile().getName()));
							/*
							 * scannerFilters.checkBoxListSystems.setModel(new SystmsModel(session.getCurrProfile().getSystems()));
							 * scannerFilters.checkBoxListSources.setModel(new SourcesModel(session.getCurrProfile().getSources()));
							 */
							initProfileSettings(session);
							MainFrame.getController().getTabPane().getSelectionModel().select(1);
						}
						this.close();
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (ExecutionException e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}

				@Override
				protected void failed()
				{
					if (getException() instanceof BreakException)
						Dialogs.showAlert("Cancelled");
					else
					{
						this.close();
						Optional.ofNullable(getException().getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(getException().getMessage(), getException());
							Dialogs.showError(getException());
						});
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
		catch (IOException | URISyntaxException e)
		{
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
		}

	}

	@FXML private void scan(ActionEvent e)
	{
		scan(session, true);
	}
	
	/**
	 * Scan.
	 */
	private void scan(final Session session, final boolean automate)
	{
		String txtdstdir = romsDest.getText();
		if (txtdstdir.isEmpty())
		{
			romsDestBtn.fire();
			txtdstdir = romsDest.getText();
		}
		if (txtdstdir.isEmpty())
			return;
		try
		{
			final var thread = new Thread(new ProgressTask<Scan>((Stage) romsDest.getScene().getWindow())
			{

				@Override
				protected Scan call() throws Exception
				{
					return new Scan(session.getCurrProfile(), this);
				}

				@Override
				protected void succeeded()
				{
					try
					{
						session.setCurrScan(get());
						fixBtn.setDisable(session.getCurrScan()==null || session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() == 0);
						close();
						// update entries in profile viewer 
						if (MainFrame.getProfileViewer() != null)
							MainFrame.getProfileViewer().reload();
						ScanAutomation automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
						if(MainFrame.getReportFrame() != null)
						{
							if(automation.hasReport())
								MainFrame.getReportFrame().setVisible();
							MainFrame.getReportFrame().setNeedUpdate(true);
						}
						if (automate && !fixBtn.isDisabled() && automation.hasFix())
						{
							fix(session);
						}
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (Exception e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}
				
				@Override
				protected void failed()
				{
					this.close();
					if (getException() instanceof BreakException)
						Dialogs.showAlert("Cancelled");
					else
					{
						Optional.ofNullable(getException().getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(getException().getMessage(), getException());
							Dialogs.showError(getException());
						});
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
		catch (IOException | URISyntaxException e)
		{
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
		}
	}
	
	@FXML private void report(ActionEvent evt)
	{
		MainFrame.getReportFrame().setVisible();
	}
	
	@FXML private void fix(ActionEvent e)
	{
		fix(session);
	}

	/**
	 * Fix.
	 */
	private void fix(final Session session)
	{
		try
		{
			final var thread = new Thread(new ProgressTask<Fix>((Stage) romsDest.getScene().getWindow())
			{
				private boolean toFix = false;
				
				@Override
				protected Fix call() throws Exception
				{
					if (session.getCurrProfile().hasPropsChanged())
					{
						final var answer = Dialogs.showConfirmation(Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
						if (answer.isPresent())
						{
							if (answer.get() == ButtonType.YES)
							{
								session.setCurrScan(new Scan(session.getCurrProfile(), this));
								toFix = session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0;
								if (!toFix)
									return null;
							}
							else if (answer.get() != ButtonType.NO)
								return null;
						}
					}
					final Fix fix = new Fix(session.getCurrProfile(), session.getCurrScan(), this);
					toFix = fix.getActionsRemain() > 0;
					return fix;
				}
				
				@Override
				protected void succeeded()
				{
					try
					{
						get();
						fixBtn.setDisable(!toFix);
						close();
						// update entries in profile viewer
						if (MainFrame.getProfileViewer() != null)
							MainFrame.getProfileViewer().reload();
						ScanAutomation automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
						if (automation.hasScanAgain())
							scan(session, false);
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (Exception e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}
				
				@Override
				protected void failed()
				{
					if (getException() instanceof BreakException)
						Dialogs.showAlert("Cancelled");
					else
					{
						this.close();
						Optional.ofNullable(getException().getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(getException().getMessage(), getException());
							Dialogs.showError(getException());
						});
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
		catch (IOException | URISyntaxException e)
		{
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
		}

	}

	
	private void initProfileSettings(Session session)
	{
		romsDest.setText(session.getCurrProfile().getProperty(SettingsEnum.roms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		disksDestCB.setSelected(session.getCurrProfile().getProperty(SettingsEnum.disks_dest_dir_enabled, false)); //$NON-NLS-1$
		disksDest.setText(session.getCurrProfile().getProperty(SettingsEnum.disks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		swDestCB.setSelected(session.getCurrProfile().getProperty(SettingsEnum.swroms_dest_dir_enabled, false)); //$NON-NLS-1$
		swDest.setText(session.getCurrProfile().getProperty(SettingsEnum.swroms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		swDisksDestCB.setSelected(session.getCurrProfile().getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)); //$NON-NLS-1$
		swDisksDest.setText(session.getCurrProfile().getProperty(SettingsEnum.swdisks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		samplesDestCB.setSelected(session.getCurrProfile().getProperty(SettingsEnum.samples_dest_dir_enabled, false)); //$NON-NLS-1$
		samplesDest.setText(session.getCurrProfile().getProperty(SettingsEnum.samples_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		backupDestCB.setSelected(session.getCurrProfile().getProperty(SettingsEnum.backup_dest_dir_enabled, false)); //$NON-NLS-1$
		backupDest.setText(session.getCurrProfile().getProperty(SettingsEnum.backup_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		srcList.setItems(FXCollections.observableList(Stream.of(StringUtils.split(session.getCurrProfile().getProperty(SettingsEnum.src_dir, ""),'|')).filter(s->!s.isEmpty()).map(File::new).collect(Collectors.toList())));

		scannerPanelSettingsController.initProfileSettings(session);
		
}
	
	@FXML private void chooseRomsDest(ActionEvent e)
	{
		chooseAnyDest(romsDest, SettingsEnum.roms_dest_dir, "MainFrame.ChooseRomsDestination");
	}
	
	@FXML private void chooseDisksDest(ActionEvent e)
	{
		chooseAnyDest(disksDest, SettingsEnum.disks_dest_dir, "MainFrame.ChooseDisksDestination");
	}
	
	@FXML private void chooseSWRomsDest(ActionEvent e)
	{
		chooseAnyDest(swDest, SettingsEnum.swroms_dest_dir, "MainFrame.ChooseSWRomsDestination");
	}
	
	@FXML private void chooseSWDisksDest(ActionEvent e)
	{
		chooseAnyDest(swDisksDest, SettingsEnum.swdisks_dest_dir, "MainFrame.ChooseSWDisksDestination");
	}
	
	@FXML private void chooseSamplesDest(ActionEvent e)
	{
		chooseAnyDest(samplesDest, SettingsEnum.samples_dest_dir, "MainFrame.ChooseSamplesDestination");
	}
	
	@FXML private void chooseBackupDest(ActionEvent e)
	{
		chooseAnyDest(backupDest, SettingsEnum.backup_dest_dir, "MainFrame.ChooseBackupDestination");
	}
	
	private void chooseAnyDest(TextField tf, SettingsEnum ppt, String defPptName)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile();
		final var defdir = PathAbstractor.getAbsolutePath(session, session.getCurrProfile().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
		final var chooser = new DirectoryChooser();
		Optional.ofNullable(tf.getText()).filter(t -> !t.isBlank()).map(t -> PathAbstractor.getAbsolutePath(session, t).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, () -> chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showDialog(tf.getScene().getWindow());
		if (chosen != null)
		{
			final var dir = PathAbstractor.getRelativePath(session, chosen.toPath());
			tf.setText(dir.toString());
			session.getCurrProfile().setProperty(defPptName, tf.getText()); // $NON-NLS-1$
			session.getCurrProfile().setProperty(ppt, tf.getText()); // $NON-NLS-1$
		}
	}
	
	private void chooseSrc(File oldDir, SettingsEnum ppt, String defPptName)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile();
		final var defdir = PathAbstractor.getAbsolutePath(session, session.getCurrProfile().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
		final var chooser = new DirectoryChooser();
		Optional.ofNullable(oldDir).map(f->PathAbstractor.getAbsolutePath(session, f.toString()).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, ()->chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showDialog(srcList.getScene().getWindow());
		if (chosen != null)
		{
			var modified = false;
			final var dir = PathAbstractor.getRelativePath(session, chosen.toPath());
			if(oldDir!=null)
			{
				if(!oldDir.equals(dir.toFile()))
				{
					final var i = srcList.getItems().indexOf(oldDir);
					srcList.getItems().set(i, dir.toFile());
					modified = true;
				}
			}
			else
			{
				if(-1 == srcList.getItems().indexOf(dir.toFile()))
				{
					srcList.getItems().add(dir.toFile());
					modified = true;
				}
			}
			if(modified)
			{
				saveSrcList();
				session.getCurrProfile().setProperty(defPptName, dir.toString()); // $NON-NLS-1$
			}
		}
	}
	
	private void saveSrcList()
	{
		session.getCurrProfile().setProperty(SettingsEnum.src_dir, String.join("|", srcList.getItems().stream().map(File::getAbsolutePath).toList()));
	}
	
	@FXML private void infos(ActionEvent evt)
	{
		if (MainFrame.getProfileViewer() == null)
		{
			try
			{
				MainFrame.setProfileViewer(new ProfileViewer((Stage)infosBtn.getScene().getWindow()));
			}
			catch (IOException | URISyntaxException e)
			{
				Log.err(e.getMessage(), e);
			}
		}
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().show();
	}
	

}
