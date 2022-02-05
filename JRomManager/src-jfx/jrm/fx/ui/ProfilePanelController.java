package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jrm.fx.ui.controls.DateCellFactory;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.controls.NameCellFactory;
import jrm.fx.ui.controls.VersionCellFactory;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.profile.manager.DirItem;
import jrm.fx.ui.profile.manager.HaveNTotalCellFactory;
import jrm.fx.ui.progress.ProgressTask;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.profile.manager.Dir;
import jrm.profile.manager.Import;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Setter;


public class ProfilePanelController implements Initializable
{
	@FXML Button btnLoad;
	@FXML Button btnImportDat;
	@FXML Button btnImportSL;
	@FXML TreeView<Dir> profilesTree;
	@FXML TableView<ProfileNFO> profilesList;
	@FXML TableColumn<ProfileNFO, String> profileCol;
	@FXML TableColumn<ProfileNFO, String> profileVersionCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveSetsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveRomsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveDisksCol;
	@FXML TableColumn<ProfileNFO, Date> profileCreatedCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastScanCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastFixCol;

	final Session session = Sessions.getSingleSession();
	private @Setter ProfileLoader profileLoader;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		btnLoad.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/add.png")));
		btnImportDat.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_go.png")));
		btnImportSL.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_go.png")));
		profilesTree.setRoot(new DirItem(session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile()));
		profilesTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> populate(newValue));
		profilesTree.getSelectionModel().select(0);
		profileCol.setCellFactory(param -> new NameCellFactory<>());
		profileCol.setCellValueFactory(param -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getName();
			}
		});
		profileVersionCol.setCellFactory(param -> new VersionCellFactory<>());
		profileVersionCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getStats().getVersion();
			}
		});
		profileHaveSetsCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveSetsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getSets();
			}
		});
		profileHaveRomsCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveRomsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getRoms();
			}
		});
		profileHaveDisksCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveDisksCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getDisks();
			}
		});
		profileCreatedCol.setCellFactory(param -> new DateCellFactory());
		profileCreatedCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getCreated();
			}
		});
		profileLastScanCol.setCellFactory(param -> new DateCellFactory());
		profileLastScanCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getScanned();
			}
		});
		profileLastFixCol.setCellFactory(param -> new DateCellFactory());
		profileLastFixCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Date getValue()
			{
				return param.getValue().getStats().getFixed();
			}
		});
		profilesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> btnLoad.setDisable(newValue == null));
		profilesList.setRowFactory(tv -> {
			final var row = new TableRow<ProfileNFO>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty())
					profileLoader.loadProfile(session, row.getItem());
			});
			return row;
		});
		new DragNDrop(profilesList).addAny(files -> {
			
		});
	}

	/**
	 * @param newValue
	 */
	private void populate(TreeItem<Dir> newValue)
	{
		profilesList.setItems(FXCollections.observableArrayList(ProfileNFO.list(session, newValue.getValue().getFile())));
	}

	@FXML void actionLoad(ActionEvent e)
	{
		final var profile = profilesList.getSelectionModel().getSelectedItem();
		if (profile != null)
			profileLoader.loadProfile(session, profile);
	}
	
	@FXML void actionImportDat(ActionEvent e)
	{
		importDat(false);
	}

	@FXML void actionImportSL(ActionEvent e)
	{
		importDat(true);
	}

	private void importDat(final boolean sl)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile();
		final var chooser = new FileChooser();
		final var filter = new ExtensionFilter(Messages.getString("MainFrame.DatFile"), "*.dat", "*.xml");
		final var filter2 = new ExtensionFilter(Messages.getString("MainFrame.MameExecutable"), "*.exe");
		chooser.getExtensionFilters().addAll(filter, filter2);
		chooser.setSelectedExtensionFilter(filter);
		Optional.ofNullable(session.getUser().getSettings().getProperty("MainFrame.ChooseExeOrDatToImport", workdir.getAbsolutePath())).map(File::new).ifPresent(chooser::setInitialDirectory);
		importDat(chooser.showOpenMultipleDialog(profilesList.getScene().getWindow()), sl);
	}
	
	private void importDat(final List<File> files, final boolean sl)
	{
		try
		{
			if(files==null)
				return ;
			final var thread = new Thread(new ProgressTask<Void>((Stage) profilesList.getScene().getWindow())
			{
				final List<Import> imprts = new ArrayList<>();

				@Override
				protected Void call() throws Exception
				{
					for(final var file : files)
					{
						setProgress(Messages.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
						imprts.add(new Import(session, file, sl, this));
					}
					return null;
				}
				
				@Override
				protected void succeeded()
				{
					this.close();
					for (final var imprt : imprts)
					{
						importDat(imprt, sl);
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
	
	/**
	 * @param imprt
	 * @param sl
	 * @throws IllegalArgumentException
	 */
	private void importDat(final Import imprt, final boolean sl) throws IllegalArgumentException
	{
		final var currDir = profilesTree.getSelectionModel().getSelectedItem().getValue().getFile();
		if (!imprt.isMame())
		{
			var fileRef = new AtomicReference<File>(new File(currDir, imprt.getFile().getName()));
			int mode = importDatExistsChoose(fileRef);
			if (mode == 3)
				return;
			if (!fileRef.get().exists() || mode == 0)
			{
				try
				{
					FileUtils.copyFile(imprt.getFile(), fileRef.get());
					populate(profilesTree.getSelectionModel().getSelectedItem());
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(), e);
				}
			}
		}
		else
		{
			final var chooser = new FileChooser();
			chooser.setTitle(Messages.getString("MainFrame.ChooseFileName"));
			chooser.setInitialDirectory(currDir);
			chooser.setInitialFileName(imprt.getFile().getName());
			final var filter = new ExtensionFilter(Messages.getString("MainFrame.DatFile"), "*.dat", "*.xml", "*.jrm");
			chooser.getExtensionFilters().add(filter);
			chooser.setSelectedExtensionFilter(filter);
			final var file = chooser.showSaveDialog(profilesList.getScene().getWindow());
			if(file!=null)
				importDat(session, sl, imprt, file);
		}
	}
	
	/**
	 * @param session
	 * @param sl
	 * @param imprt
	 * @param file
	 */
	private Void importDat(final Session session, final boolean sl, final jrm.profile.manager.Import imprt, final File file)
	{
		try
		{
			final var parent = file.getParentFile();
			FileUtils.copyFile(imprt.getFile(), file);
			if (imprt.isMame())
			{
				final var pnfo = ProfileNFO.load(session, file);
				pnfo.getMame().set(imprt.getOrgFile(), sl);
				if (imprt.getRomsFile() != null)
				{
					FileUtils.copyFileToDirectory(imprt.getRomsFile(), parent);
					pnfo.getMame().setFileroms(new File(parent, imprt.getRomsFile().getName()));
					if (imprt.getSlFile() != null)
					{
						FileUtils.copyFileToDirectory(imprt.getSlFile(), parent);
						pnfo.getMame().setFilesl(new File(parent, imprt.getSlFile().getName()));
					}
				}
				pnfo.save(session);
			}
			
			final var root = profilesTree.getRoot();
			final var theNode = getTreeViewItem(root, new Dir(parent));
			if (theNode instanceof DirItem d)
			{
				d.reload();
				profilesTree.refresh();
				populate(d);
			}
			else
				Log.err(Messages.getString("MainFrame.NodeNotFound")); //$NON-NLS-1$
		}
		catch (final IOException e)
		{
			Log.err(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param file
	 * @return
	 * @throws HeadlessException
	 * @throws IllegalArgumentException
	 */
	private int importDatExistsChoose(AtomicReference<File> file) throws IllegalArgumentException
	{
		int mode = -1;
		if (file.get().exists())
		{
			final var overwrite = new ButtonType("Overwrite");
			final var autorename = new ButtonType("Auto Rename");
			final var filechooser = new ButtonType("File Chooser");
			final var options = new ButtonType[] { overwrite, autorename, filechooser, ButtonType.CANCEL};
			final var ret = Dialogs.showConfirmation("File already exists", "File already exists, choose what to do", options);
			if(ret.isEmpty())
				mode = 3;
			else if(ret.get()==overwrite)
				mode = 0;
			else if(ret.get()==autorename)
				mode = 1;
			else if(ret.get()==filechooser)
				mode = 2;
			else
				mode = 3;
			if (mode == 1)
				file.set(autoRenameFile(file.get()));
		}
		return mode;
	}

	/**
	 * @param file
	 * @return
	 * @throws IllegalArgumentException
	 */
	private File autoRenameFile(File file) throws IllegalArgumentException
	{
		for (var i = 1;; i++)
		{
			final var testFile = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + '_' + i + '.' + FilenameUtils.getExtension(file.getName()));
			if (!testFile.exists())
				return testFile;
		}
	}

	public static <T> TreeItem<T> getTreeViewItem(TreeItem<T> item, T value)
	{
		if (item != null)
		{
			if (item.getValue().equals(value))
				return item;
			for (TreeItem<T> child : item.getChildren())
			{
				TreeItem<T> s = getTreeViewItem(child, value);
				if (s != null)
				{
					return s;
				}
			}
		}
		return null;
	}
	
}
