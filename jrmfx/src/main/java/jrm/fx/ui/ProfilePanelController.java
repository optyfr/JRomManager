package jrm.fx.ui;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jrm.fx.ui.controls.DateCellFactory;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.controls.ProfileCellFactory;
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
import jrm.profile.manager.ProfileNFOMame.MameStatus;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.AllArgsConstructor;
import lombok.Setter;


public class ProfilePanelController implements Initializable
{
	@FXML Button btnLoad;
	@FXML Button btnImportDat;
	@FXML Button btnImportSL;
	@FXML TreeView<Dir> profilesTree;
	@FXML TableView<ProfileNFO> profilesList;
	@FXML TableColumn<ProfileNFO, ProfileNFO> profileCol;
	@FXML TableColumn<ProfileNFO, String> profileVersionCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveSetsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveRomsCol;
	@FXML TableColumn<ProfileNFO, HaveNTotal> profileHaveDisksCol;
	@FXML TableColumn<ProfileNFO, Date> profileCreatedCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastScanCol;
	@FXML TableColumn<ProfileNFO, Date> profileLastFixCol;
	@FXML MenuItem createFolderMenu;
	@FXML MenuItem deleteFolderMenu;
	@FXML MenuItem deleteProfileMenu;
	@FXML MenuItem renameProfileMenu;
	@FXML MenuItem dropCacheMenu;
	@FXML MenuItem updateFromMameMenu;
	@FXML ContextMenu folderMenu;
	@FXML ContextMenu profileMenu;

	final Session session = Sessions.getSingleSession();
	private @Setter ProfileLoader profileLoader;

	private static Comparator<Long> nullSafeLongComparator = Comparator.nullsFirst(Long::compareTo); 

	private static Comparator<HaveNTotal> haveNTotalComparator = Comparator
		.comparing(HaveNTotal::getHave, nullSafeLongComparator)
	        .thenComparing(HaveNTotal::getHave, nullSafeLongComparator);

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		btnLoad.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/add.png")));
		btnImportDat.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_go.png")));
		btnImportSL.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_go.png")));
		createFolderMenu.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/folder_add.png")));
		deleteFolderMenu.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/folder_delete.png")));
		deleteProfileMenu.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_delete.png")));
		renameProfileMenu.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_edit.png")));
		dropCacheMenu.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bin.png")));
		folderMenu.setOnShowing(e -> {
			final var selected = profilesTree.getSelectionModel().getSelectedItem();
			deleteFolderMenu.setDisable(selected == null);
			createFolderMenu.setDisable(selected == null);
		});
		profileMenu.setOnShowing(e -> {
			final var selected = profilesList.getSelectionModel().getSelectedItem();
			deleteProfileMenu.setDisable(selected == null);
			renameProfileMenu.setDisable(selected == null);
			dropCacheMenu.setDisable(selected == null);
			updateFromMameMenu.setDisable(selected == null || !selected.isJRM());
		});
		profilesTree.setCellFactory(p -> new TextFieldTreeCell<>(new StringConverter<>()
		{
			private Dir dir;
			
			@Override
			public String toString(Dir dir)
			{
				this.dir = dir;
				return dir.toString();
			}

			@Override
			public Dir fromString(String string)
			{
				return dir.renameTo(dir.getFile().toPath().getParent().resolve(string).toFile());
			}
		}));
		profilesTree.setOnEditCommit(this::editCommitProfileDir);
		profilesTree.setRoot(new DirItem(session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile()));
		profilesTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> populate(newValue));
		profilesTree.getSelectionModel().select(0);
		profileCol.setEditable(true);
		profileCol.setCellFactory(param -> new ProfileCellFactory());
		profileCol.setOnEditCommit(this::editCommitProfile);
		profileCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public ProfileNFO getValue()
			{
				return param.getValue();
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
		profileHaveSetsCol.setComparator(haveNTotalComparator);
		profileHaveRomsCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveRomsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getRoms();
			}
		});
		profileHaveRomsCol.setComparator(haveNTotalComparator);
		profileHaveDisksCol.setCellFactory(param -> new HaveNTotalCellFactory<>());
		profileHaveDisksCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public HaveNTotal getValue()
			{
				return param.getValue().getStats().getDisks();
			}
		});
		profileHaveDisksCol.setComparator(haveNTotalComparator);
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
				{
					profileLoader.loadProfile(session, row.getItem());
				}
			});
			return row;
		});
		profilesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> profilesList.refresh());
		profilesList.setEditable(false);
		profilesList.setFixedCellSize(Region.USE_COMPUTED_SIZE);
		new DragNDrop(profilesList).addAny(files -> importDat(files, true));
	}
	
	public void resizeColumns()
	{
		Platform.runLater(()->{
			final var columns = profilesList.getColumns();
		    for (int i = 0 ; i < columns.size(); i++)
		    {
				try
				{
		        	final var th = (TableColumnHeader) profilesList.queryAccessibleAttribute(AccessibleAttribute.COLUMN_AT_INDEX, i);;
					final var columnToFitMethod = TableColumnHeader.class.getDeclaredMethod("resizeColumnToFitContent", int.class);
		            columnToFitMethod.setAccessible(true); // NOSONAR
		            columnToFitMethod.invoke(th, -1);
				}
				catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
				{
					e.printStackTrace();
				}
	
		    }
		});
	}

	/**
	 * 
	 */
	private void editCommitProfileDir(TreeView.EditEvent<Dir> e)
	{
		Platform.runLater(() -> {
			if (getTreeViewItem(profilesTree.getRoot(), e.getNewValue()) instanceof DirItem newItem)
				newItem.reload();
		});
	}

	/**
	 * @param e
	 */
	private void editCommitProfile(CellEditEvent<ProfileNFO, ProfileNFO> e)
	{
		final ProfileNFO pnfo = e.getRowValue();
		AtomicInteger err = new AtomicInteger();
		Arrays.asList("", ".properties", ".cache").forEach(ext -> { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			final var oldfile = new File(pnfo.getFile().getParentFile(), pnfo.getName() + ext);
			final var newfile = new File(pnfo.getFile().getParentFile(), e.getNewValue().getNewName() + ext);
			final var success = !oldfile.equals(newfile)&&oldfile.renameTo(newfile);
			err.set((err.get() << 1) | (success ? 0 : 1));
			if (!success)
				Log.warn(() -> "Can't rename " + oldfile.getName() + " to " + newfile.getName());
		});
		if (err.get() != 0)
		{
			Dialogs.showAlert("Can't rename " + e.getOldValue().getName() + " to " + e.getNewValue().getNewName());
			e.getTableView().refresh();
		}
		else
		{
			final var newNfoFile = new File(pnfo.getFile().getParentFile(), e.getNewValue().getNewName());
			if (session.getCurrProfile() != null && session.getCurrProfile().getNfo().getFile().equals(pnfo.getFile()))
				session.getCurrProfile().getNfo().relocate(session, newNfoFile);
			pnfo.relocate(session, newNfoFile);
		}
	}

	/**
	 * @param newValue
	 */
	private void populate(TreeItem<Dir> newValue)
	{
		if(newValue==null)
			return;
		profilesList.setItems(FXCollections.observableArrayList(ProfileNFO.list(session, newValue.getValue().getFile())));
		resizeColumns();
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
		final var filter2 = new ExtensionFilter(Messages.getString("MainFrame.MameExecutable"), SystemUtils.IS_OS_WINDOWS?"*mame*.exe":"*mame*");
		chooser.getExtensionFilters().addAll(filter, filter2);
		chooser.setSelectedExtensionFilter(filter);
		Optional.ofNullable(session.getUser().getSettings().getProperty("MainFrame.ChooseExeOrDatToImport", workdir.getAbsolutePath())).map(File::new).ifPresent(chooser::setInitialDirectory);
		final var files = chooser.showOpenMultipleDialog(profilesList.getScene().getWindow());
		importDat(files, sl);
		if (files != null)
			session.getUser().getSettings().setProperty("MainFrame.ChooseExeOrDatToImport", files.stream().filter(File::exists).map(File::getParent).findFirst().orElse(null));
	}
	
	private final class ImportDatTask extends ProgressTask<Void>
	{
		private final List<File> files;
		private final boolean sl;
		final List<ImportWithBaseFile> imprts = new ArrayList<>();

		private ImportDatTask(Stage owner, List<File> files, boolean sl) throws IOException, URISyntaxException
		{
			super(owner);
			this.files = files;
			this.sl = sl;
		}

		@Override
		protected Void call() throws Exception
		{
			for(final var basefile : files)
			{
				for(final var file : searchDats(basefile))
				{
					setProgress(Messages.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
					imprts.add(new ImportWithBaseFile(new Import(session, file, sl, this), basefile));
				}
			}
			return null;
		}

		@Override
		protected void succeeded()
		{
			this.close();
			for (final var imprt : imprts)
			{
				try
				{
					importDat(imprt, sl);
				}
				catch(IOException e)
				{
					Log.err(e.getMessage(), e);
				}
			}
			
			final var theNode = profilesTree.getSelectionModel().getSelectedItem();
			if (theNode instanceof DirItem d)
			{
				d.reload();
				populate(d);
			}
			else
				Log.err(Messages.getString("MainFrame.NodeNotFound")); //$NON-NLS-1$
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

		/**
		 * @param imprt
		 * @param sl
		 * @throws IllegalArgumentException
		 */
		private void importDat(final ImportWithBaseFile imprt, final boolean sl) throws IllegalArgumentException, IOException
		{
			final var selDir = profilesTree.getSelectionModel().getSelectedItem().getValue().getFile().toPath();
			final var currDir = selDir.resolve(imprt.basefile.toPath().getParent().relativize(imprt.imprt.getOrgFile().toPath().getParent())).toFile();
			Files.createDirectories(currDir.toPath());
			if (!imprt.imprt.isMame())
			{
				var fileRef = new AtomicReference<File>(new File(currDir, imprt.imprt.getFile().getName()));
				int mode = importDatExistsChoose(fileRef);
				if (mode == 3)
					return;
				if (!fileRef.get().exists() || mode == 0)
				{
					try
					{
						FileUtils.copyFile(imprt.imprt.getFile(), fileRef.get());
					}
					catch (IOException e)
					{
						Log.err(e.getMessage(), e);
					}
				}
			}
			else
			{
				final var layout = new VBox();
				layout.setPrefWidth(300);
				final var label = new Label("Choose a name to save JRM file for import of " + imprt.imprt.getOrgFile());
				label.setWrapText(true);
				layout.getChildren().add(label);
				final var nameField = new TextField(imprt.imprt.getFile().getName());
				layout.getChildren().add(nameField);
				final var result = Dialogs.showConfirmation("Choose a name to save JRM file", layout, ButtonType.APPLY);
				final var fileName = result.filter(t -> t == ButtonType.APPLY)
						.map(t -> nameField.getText())
						.filter(t -> !t.isBlank())
						.map(t -> t.endsWith(".jrm") ? t : (t + ".jrm"))
						.orElse(imprt.imprt.getFile().getName());
				importDat(session, sl, imprt.imprt, currDir.toPath().resolve(fileName).toFile());
			}
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

		private List<File> searchDats(File file)
		{
			return searchDats(file, new ArrayList<>());
		}
		
		private List<File> searchDats(File file, List<File> files)
		{
			if(file.isFile())
			{
				if (FilenameUtils.isExtension(file.getName(), "xml", "dat") || (file.getName().toLowerCase().contains("mame") && (FilenameUtils.isExtension(file.getName(), "exe") || file.canExecute())))
					files.add(file);
			}
			else if(file.isDirectory())
			{
				try(final var stream = Files.newDirectoryStream(file.toPath()))
				{
					stream.forEach(p -> searchDats(p.toFile(), files));
				}
				catch(IOException e)
				{
					Log.warn(e.getMessage());
				}
			}
			return files;
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
			}
			catch (final IOException e)
			{
				Log.err(e.getMessage(), e);
			}
			return null;
		}

	}

	private final class UpdateFromMameTask extends ProgressTask<Import>
	{
		private final ProfileNFO nfo;

		private UpdateFromMameTask(Stage owner, ProfileNFO nfo) throws IOException, URISyntaxException
		{
			super(owner);
			this.nfo = nfo;
		}

		@Override
		protected Import call() throws Exception
		{
			return new Import(session, nfo.getMame().getFile(), nfo.getMame().isSL(), this);
		}

		@Override
		protected void succeeded()
		{
			try
			{
				this.close();
				updateFromMame(session, nfo, get());
			}
			catch (InterruptedException e)
			{
				Log.err(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			catch (ExecutionException | IOException e)
			{
				Log.err(e.getMessage(), e);
				Dialogs.showError(e);
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

		/**
		 * @param session
		 * @param nfo
		 * @param imprt
		 * @throws IOException
		 */
		private void updateFromMame(final Session session, final ProfileNFO nfo, Import imprt) throws IOException
		{
			nfo.getMame().delete();
			nfo.getMame().setFileroms(new File(nfo.getFile().getParentFile(), imprt.getRomsFile().getName()));
			Files.copy(imprt.getRomsFile().toPath(), nfo.getMame().getFileroms().toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			if (nfo.getMame().isSL())
			{
				nfo.getMame().setFilesl(new File(nfo.getFile().getParentFile(), imprt.getSlFile().getName()));
				Files.copy(imprt.getSlFile().toPath(), nfo.getMame().getFilesl().toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			}
			nfo.getMame().setUpdated();
			nfo.getStats().reset();
			nfo.save(session);
			profilesList.refresh();
		}
	}

	@AllArgsConstructor
	private static final class ImportWithBaseFile
	{
		Import imprt;
		File basefile;
	}

	
	private void importDat(final List<File> files, final boolean sl)
	{
		try
		{
			if(files==null)
				return ;
			final var thread = new Thread(new ImportDatTask((Stage) profilesList.getScene().getWindow(), files, sl));
			thread.setDaemon(true);
			thread.start();
		}
		catch (IOException | URISyntaxException e)
		{
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
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

	public void refreshList()
	{
		profilesList.refresh();
	}
	
	@FXML private void createFolder(ActionEvent e)
	{
		final var selectedItem = profilesTree.getSelectionModel().getSelectedItem();
		if(selectedItem instanceof DirItem d)
		{
			final var newDir = new Dir(new File(selectedItem.getValue().getFile(), Messages.getString("MainFrame.NewFolder")));
			d.reload();
			final var newItem = getTreeViewItem(d, newDir);
			profilesTree.getSelectionModel().clearSelection();
			profilesTree.getSelectionModel().select(newItem);
			profilesTree.layout();
			profilesTree.edit(newItem);
		}
	}
	
	@FXML private void deleteFolder(ActionEvent e)
	{
		final var selectedItem = profilesTree.getSelectionModel().getSelectedItem();
		if(selectedItem instanceof DirItem d)
		{
			try
			{
				boolean empty = false;
				try (final var entries = Files.list(d.getValue().getFile().toPath()))
				{
					empty = !entries.findFirst().isPresent();
				}
				boolean doit = empty;
				if (!empty)
					doit = Dialogs.showConfirmation("Dir not empty", "This directory is not empty, delete?", ButtonType.YES, ButtonType.NO).map(t -> t == ButtonType.YES).orElse(false);
				if (doit)
				{
					FileUtils.deleteDirectory(d.getValue().getFile());
					selectedItem.getParent().getChildren().remove(d);
				}
			}
			catch(IOException ex)
			{
				Log.err(ex.getMessage(), ex);
				Dialogs.showAlert(ex.getMessage());
			}
		}
	}

	@FXML private void deleteProfile(ActionEvent e)
	{
		final var nfo = profilesList.getSelectionModel().getSelectedItem();
		if (nfo != null && (session.getCurrProfile() == null || !session.getCurrProfile().getNfo().equals(nfo)) && nfo.delete())
			profilesList.getItems().remove(nfo);
	}

	@FXML private void renameProfile(ActionEvent e)
	{
		profilesList.setEditable(true);
		profilesList.edit(profilesList.getSelectionModel().getSelectedIndex(), profileCol);
		profilesList.setEditable(false);
	}

	@FXML private void dropCache(ActionEvent e)
	{
		final var nfo = profilesList.getSelectionModel().getSelectedItem();
		if (nfo !=null)
			try
			{
				Files.deleteIfExists(Paths.get(nfo.getFile().getAbsolutePath() + ".cache"));
			}
			catch (IOException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
	}

	@FXML private void updateFromMame(ActionEvent e)
	{
		final var nfo = profilesList.getSelectionModel().getSelectedItem();
		if (nfo !=null)
		{
			try
			{
				final var chooser = new FileChooser();
				chooser.setTitle(Messages.getString("MainFrame.ChooseMameNewLocation"));
				chooser.setInitialDirectory(nfo.getFile().getParentFile());
				chooser.setInitialFileName(nfo.getFile().getName());
				if (nfo.getMame().getStatus() == MameStatus.NEEDUPDATE || (EnumSet.of(MameStatus.NOTFOUND, MameStatus.UNKNOWN).contains(nfo.getMame().getStatus()) && updateFromMameRelocate(nfo, chooser.showOpenDialog(profilesList.getScene().getWindow())) == MameStatus.NEEDUPDATE))
				{
					final var thread = new Thread(new UpdateFromMameTask((Stage) profilesList.getScene().getWindow(), nfo));
					thread.setDaemon(true);
					thread.start();
				}
			}
			catch(IOException | URISyntaxException ex)
			{
				Log.err(ex.getMessage(), ex);
				Dialogs.showError(ex);
			}
		}
	}
	
	/**
	 * @param nfo
	 * @param chooser
	 * @return
	 */
	private MameStatus updateFromMameRelocate(final ProfileNFO nfo, File mame)
	{
		if (mame.exists())
			return nfo.getMame().relocate(mame);
		return MameStatus.NOTFOUND;
	}
}
