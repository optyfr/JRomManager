package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.progress.ProgressTask;
import jrm.fx.ui.web.HTMLFormatter;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.options.Descriptor;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.Session;

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
	@FXML	private Button disksDestBtn;
	@FXML	private TextField disksDest;
	@FXML	private Button swDestBtn;
	@FXML	private TextField swDest;
	@FXML	private Button swDisksDestBtn;
	@FXML	private TextField swDisksDest;
	@FXML	private Button samplesDestBtn;
	@FXML	private TextField samplesDest;
	@FXML	private Button backupDestBtn;
	@FXML	private TextField backupDest;

	@FXML	private Tab settingsTab;

	@FXML	private ComboBox<Descriptor> compressionCbx;
	@FXML	private ComboBox<Descriptor> mergeModeCbx;
	@FXML	private ComboBox<Descriptor> collisionModeCbx;

	@FXML	private Tab filterTab;
	@FXML	private Tab advFilterTab;
	@FXML	private Tab automationTab;

	@FXML	private WebView profileinfoLbl;

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
		swDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		swDisksDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		samplesDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		backupDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		infosBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png")));
		scanBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/magnifier.png")));
		reportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/report.png")));
		fixBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/tick.png")));
		importBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_refresh.png")));
		exportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_save.png")));
		Callback<ListView<Descriptor>, ListCell<Descriptor>> cellFactory = param -> new ListCell<Descriptor>()
		{
			@Override
			protected void updateItem(Descriptor item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null || empty)
					setGraphic(null);
				else
					setText(item.getDesc());
			}
		};
		compressionCbx.setItems(FXCollections.observableArrayList(FormatOptions.values()));
		compressionCbx.setCellFactory(cellFactory);
		compressionCbx.setButtonCell(cellFactory.call(null));
		mergeModeCbx.setItems(FXCollections.observableArrayList(MergeOptions.values()));
		mergeModeCbx.setCellFactory(cellFactory);
		mergeModeCbx.setButtonCell(cellFactory.call(null));
		collisionModeCbx.setItems(FXCollections.observableArrayList(HashCollisionOptions.values()));
		collisionModeCbx.setCellFactory(cellFactory);
		collisionModeCbx.setButtonCell(cellFactory.call(null));
	}

	@FXML
	void switchDisksDest(ActionEvent e)
	{
		if (e.getSource() instanceof CheckBox cb)
		{
			disksDest.setDisable(!cb.isSelected());
			disksDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML
	void switchSWDest(ActionEvent e)
	{
		if (e.getSource() instanceof CheckBox cb)
		{
			swDest.setDisable(!cb.isSelected());
			swDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML
	void switchSWDisksDest(ActionEvent e)
	{
		if (e.getSource() instanceof CheckBox cb)
		{
			swDisksDest.setDisable(!cb.isSelected());
			swDisksDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML
	void switchSamplesDest(ActionEvent e)
	{
		if (e.getSource() instanceof CheckBox cb)
		{
			samplesDest.setDisable(!cb.isSelected());
			samplesDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML
	void switchBackupDest(ActionEvent e)
	{
		if (e.getSource() instanceof CheckBox cb)
		{
			backupDest.setDisable(!cb.isSelected());
			backupDestBtn.setDisable(!cb.isSelected());
		}
	}

	@Override
	public void loadProfile(Session session, ProfileNFO profile)
	{
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
						/*
						 * if (MainFrame.getProfileViewer() != null)
						 * MainFrame.getProfileViewer().reset(session.getCurrProfile());
						 */
						MainFrame.getController().getScannerPanelTab().setDisable(profile == null);
						scanBtn.setDisable(profile == null);
						fixBtn.setDisable(true);
						if (profile != null && session.getCurrProfile() != null)
						{
							profileinfoLbl.getEngine().loadContent(HTMLFormatter.toHTML(session.getCurrProfile().getName()));
							/*
							 * scannerFilters.checkBoxListSystems.setModel(new
							 * SystmsModel(session.getCurrProfile().getSystems()));
							 * scannerFilters.checkBoxListSources.setModel(new
							 * SourcesModel(session.getCurrProfile().getSources()));
							 * initProfileSettings(session);
							 */
							MainFrame.getController().getTabPane().getSelectionModel().select(1);
						}
						this.close();
					}
					catch (InterruptedException | ExecutionException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Dialogs.showError(e);
					}
				}

				@Override
				protected void failed()
				{
					this.close();
					Dialogs.showError(getException());
				}

				@Override
				protected void cancelled()
				{
					this.close();
					Dialogs.showAlert("Cancelled");
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

}
