package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.aui.basic.AbstractSrcDstResult;
import jrm.aui.basic.ResultColUpdater;
import jrm.batch.Compressor;
import jrm.batch.CompressorFormat;
import jrm.batch.DirUpdater;
import jrm.batch.DirUpdaterResults;
import jrm.batch.TorrentChecker;
import jrm.batch.TorrentChecker.Options;
import jrm.batch.TrntChkReport;
import jrm.fx.ui.controls.ButtonCellFactory;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.controls.DropCell;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.misc.FileResult;
import jrm.fx.ui.misc.SrcDstResult;
import jrm.fx.ui.progress.ProgressTask;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.ProfileSettings;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;

public class BatchToolsPanelController extends BaseController
{
	private static final String ICON_BULLET_GO = "/jrm/resicons/icons/bullet_go.png";
	
	@FXML private Tab panelBatchToolsDat2Dir;
	@FXML private Tab panelBatchToolsDir2Torrent;
	@FXML private Tab panelBatchToolsCompressor;
	@FXML private Button btnBatchToolsDir2DatStart;
	@FXML private Button btnBatchToolsTrntChkStart;
	@FXML private Button btnBatchToolsCompressorStart;
	@FXML private Button btnBatchToolsCompressorClear;
	@FXML private ChoiceBox<TrntChkMode> cbbxBatchToolsTrntChk;
	@FXML private ChoiceBox<CompressorFormat> cbbxBatchToolsCompressorFormat;
	@FXML private CheckBox cbBatchToolsDat2DirDryRun;
	@FXML private CheckBox cbBatchToolsTrntChkDetectArchivedFolder;
	@FXML private CheckBox cbBatchToolsTrntChkRemoveUnknownFiles;
	@FXML private CheckBox cbBatchToolsTrntChkRemoveWrongSizedFiles;
	@FXML private CheckBox cbBatchToolsCompressorForce;
	@FXML private TableView<File> tvBatchToolsDat2DirSrc;
	@FXML private TableColumn<File, File> tvBatchToolsDat2DirSrcCol;
	@FXML private ContextMenu popupMenuSrc;
	@FXML private MenuItem mnDat2DirAddSrcDir;
	@FXML private MenuItem mnDat2DirDelSrcDir;
	@FXML private TableView<SrcDstResult> tvBatchToolsDat2DirDst;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDatsCol;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDirsCol;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstResultCol;
	@FXML private TableColumn<SrcDstResult, SrcDstResult> tvBatchToolsDat2DirDstDetailsCol;
	@FXML private TableColumn<SrcDstResult, Boolean> tvBatchToolsDat2DirDstSelCol;
	@FXML private ContextMenu popupMenuDst;
	@FXML private MenuItem mnDat2DirDelDstDat;
	@FXML private Menu mntmDat2DirDstPresets;
	@FXML private TableView<SrcDstResult> tvBatchToolsTorrent;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsTorrentFilesCol;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsTorrentDstDirsCol;
	@FXML private TableColumn<SrcDstResult, String> tvBatchToolsTorrentResultCol;
	@FXML private TableColumn<SrcDstResult, SrcDstResult> tvBatchToolsTorrentDetailsCol;
	@FXML private TableColumn<SrcDstResult, Boolean> tvBatchToolsTorrentSelCol;
	@FXML private ContextMenu popupMenuTorrent;
	@FXML private MenuItem mnDelTorrent;
	
	@FXML private TableView<FileResult> tvBatchToolsCompressor;
	@FXML private TableColumn<FileResult, Path> tvBatchToolsCompressorFileCol;
	@FXML private TableColumn<FileResult, String> tvBatchToolsCompressorStatusCol;
	
	private Font font = new Font(10);
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		initDat2Dir();
		initTorrent();
		initCompressor();
	}
	
	/**
	 * 
	 */
	private void initCompressor()
	{
		panelBatchToolsCompressor.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/compress.png")));

		tvBatchToolsCompressor.setFixedCellSize(18);
		new DragNDrop(tvBatchToolsCompressor).addAny(this::addFilesToCompressorList);
		tvBatchToolsCompressorFileCol.setCellFactory(param -> new TableCell<FileResult, Path>()
		{
			@Override
			protected void updateItem(Path item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				if(empty)
					setText("");
				else
				{
					setText(item.toString());
					setTooltip(new Tooltip(getText()));
				}
			}
		});
		tvBatchToolsCompressorFileCol.setCellValueFactory(param -> param.getValue().fileProperty());
		tvBatchToolsCompressorStatusCol.setCellFactory(param -> new TableCell<FileResult, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				if(empty)
					setText("");
				else
				{
					setText(item);
					setTooltip(new Tooltip(item));
				}
			}
		});
		tvBatchToolsCompressorStatusCol.setCellValueFactory(param -> param.getValue().resultProperty());
		
		cbbxBatchToolsCompressorFormat.setItems(FXCollections.observableArrayList(CompressorFormat.values()));
		cbbxBatchToolsCompressorFormat.getSelectionModel().select(CompressorFormat.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.compressor_format))); // $NON-NLS-1$
		cbbxBatchToolsCompressorFormat.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_format, newValue.toString()));

		cbBatchToolsCompressorForce.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.compressor_force, Boolean.class));
		cbBatchToolsCompressorForce.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_force, newValue));

		btnBatchToolsCompressorClear.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bin.png")));
		
		btnBatchToolsCompressorStart.setGraphic(new ImageView(MainFrame.getIcon(ICON_BULLET_GO)));
		btnBatchToolsCompressorStart.setOnAction(e -> {
			try
			{
				final var thread = new Thread(startCompression());
				thread.setDaemon(true);
				thread.start();
			}
			catch (IOException | URISyntaxException e1)
			{
				e1.printStackTrace();
			}			
		});
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private ProgressTask<Void> startCompression() throws IOException, URISyntaxException
	{
		return new ProgressTask<Void>((Stage)btnBatchToolsCompressorStart.getScene().getWindow())
		{

			@Override
			protected Void call() throws Exception
			{
				final var cnt = new AtomicInteger();
				final var compressor = new Compressor(session, cnt, tvBatchToolsCompressor.getItems().size(), this);
				final var use_parallelism = session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.compressor_parallelism, Boolean.class);
				final var nThreads = Boolean.TRUE.equals(use_parallelism) ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, Integer.class) : 1;

				setInfos(nThreads <= 0 ? Runtime.getRuntime().availableProcessors() : nThreads, true);
				tvBatchToolsCompressor.getItems().forEach(fr -> fr.setResult(""));

				new MultiThreading<FileResult>(nThreads, fr -> {
					if (isCancel())
						return;
					compress(cnt, compressor, fr);
				}).start(tvBatchToolsCompressor.getItems().stream());
				return null;
			}
			
			@Override
			public void succeeded()
			{
				close();
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
		};
	}

	/**
	 * @param cnt
	 * @param compressor
	 * @param fr
	 * @throws IllegalArgumentException
	 */
	private void compress(final AtomicInteger cnt, final Compressor compressor, FileResult fr) throws IllegalArgumentException
	{
		var file = fr.getFile().toFile();
		cnt.incrementAndGet();
		Compressor.UpdResultCallBack cb = fr::setResult;
		Compressor.UpdSrcCallBack scb = src -> fr.setFile(src.toPath());
		switch (cbbxBatchToolsCompressorFormat.getSelectionModel().getSelectedItem())
		{
			case SEVENZIP:
			{
				toSevenZip(compressor, file, cb, scb);
				break;
			}
			case ZIP:
			{
				toZip(compressor, file, cb, scb);
				break;
			}
			case TZIP:
			{
				toTZip(compressor, file, cb, scb);
				break;
			}
		}
	}

	/**
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void toSevenZip(final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		switch (FilenameUtils.getExtension(file.getName()))
		{
			case "zip":
				compressor.zip2SevenZip(file, cb, scb);
				break;
			case "7z":
				if (cbBatchToolsCompressorForce.isSelected())
					compressor.sevenZip2SevenZip(file, cb, scb);
				else
					cb.apply("Skipped");
				break;
			default:
				compressor.sevenZip2SevenZip(file, cb, scb);
				break;
		}
	}


	/**
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void toZip(final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		if("zip".equals(FilenameUtils.getExtension(file.getName())))
		{
			if (cbBatchToolsCompressorForce.isSelected())
				compressor.zip2Zip(file, cb, scb);
			else
				cb.apply("Skipped");
		}
		else
			compressor.sevenZip2Zip(file, false, cb, scb);
	}


	/**
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void toTZip(final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		if("zip".equals(FilenameUtils.getExtension(file.getName())))
			compressor.zip2TZip(file, cbBatchToolsCompressorForce.isSelected(), cb);
		else
		{
			file = compressor.sevenZip2Zip(file, true, cb, scb);
			if (file != null && file.exists())
				compressor.zip2TZip(file, cbBatchToolsCompressorForce.isSelected(), cb);
		}
	}


	
	/**
	 * @param files
	 */
	private void addFilesToCompressorList(List<File> files)
	{
		final var extensions = new String[] { "zip", "7z", "rar", "arj", "tar", "lzh", "lha", "tgz", "tbz", "tbz2", "rpm", "iso", "deb", "cab" };
		final var set = new LinkedHashSet<>(tvBatchToolsCompressor.getItems());
		final var ffiles = files.stream().flatMap(f -> {
			try
			{
				return f.isDirectory() ? Files.find(f.toPath(), Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile() && FilenameUtils.isExtension(p.getFileName().toString(), extensions)) : Stream.of(f.toPath());
			}
			catch (IOException e)
			{
				return null;
			}
		}).map(FileResult::new).toList();
		set.addAll(ffiles);
		tvBatchToolsCompressor.setItems(FXCollections.observableArrayList(set));
	}

	/**
	 * 
	 */
	private void initTorrent()
	{
		panelBatchToolsDir2Torrent.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_web.png")));
		
		initTorrentList();

		cbbxBatchToolsTrntChk.setItems(FXCollections.observableArrayList(TrntChkMode.values()));
		cbbxBatchToolsTrntChk.getSelectionModel().select(TrntChkMode.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_mode)));
		cbbxBatchToolsTrntChk.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			session.getUser().getSettings().setProperty(SettingsEnum.trntchk_mode, newValue.toString());
			cbBatchToolsTrntChkRemoveWrongSizedFiles.setDisable(newValue == TrntChkMode.FILENAME);
		});
		
		cbBatchToolsTrntChkDetectArchivedFolder.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_detect_archived_folders, Boolean.class));
		cbBatchToolsTrntChkDetectArchivedFolder.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_detect_archived_folders, newValue));

		cbBatchToolsTrntChkRemoveWrongSizedFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_wrong_sized_files, Boolean.class));
		cbBatchToolsTrntChkRemoveWrongSizedFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_wrong_sized_files, newValue));

		cbBatchToolsTrntChkRemoveUnknownFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_unknown_files, Boolean.class));
		cbBatchToolsTrntChkRemoveUnknownFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_unknown_files, newValue));

		btnBatchToolsTrntChkStart.setOnAction(e -> startTorrent());
		btnBatchToolsTrntChkStart.setGraphic(new ImageView(MainFrame.getIcon(ICON_BULLET_GO)));
}

	/**
	 * 
	 */
	private void initTorrentList()
	{
		tvBatchToolsTorrent.setFixedCellSize(18);
		tvBatchToolsTorrent.getItems().setAll(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr)));
		tvBatchToolsTorrentFilesCol.setCellFactory(param -> new DropCell(tvBatchToolsTorrent, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveTorrentDst();
		}, file -> {
			if (Files.isRegularFile(file.toPath()))
				return file.getName().endsWith(".torrent");
			return false;
		}));
		tvBatchToolsTorrentFilesCol.setCellValueFactory(param ->  param.getValue().srcProperty());
		tvBatchToolsTorrentDstDirsCol.setCellFactory(param -> new DropCell(tvBatchToolsTorrent, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setDst(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveTorrentDst();
		}, File::isDirectory));
		tvBatchToolsTorrentDstDirsCol.setCellValueFactory(param -> param.getValue().dstProperty());
		tvBatchToolsTorrentResultCol.setCellFactory(param -> new TableCell<SrcDstResult, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				if(empty)
					setText("");
				else
				{
					setText(item);
					setTooltip(new Tooltip(item));
				}
			}
		});
		tvBatchToolsTorrentResultCol.setCellValueFactory(param -> param.getValue().resultProperty());
		tvBatchToolsTorrentSelCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
			final var sdr = tvBatchToolsTorrent.getItems().get(param);
			BooleanProperty observable = new SimpleBooleanProperty(sdr.isSelected());
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				sdr.setSelected(isNowSelected);
				saveTorrentDst();
			});
			return observable;
		}));
		tvBatchToolsTorrentDetailsCol.setCellFactory(param -> new ButtonCellFactory<>("Detail", cell -> {
			final AbstractSrcDstResult sdr = tvBatchToolsTorrent.getItems().get(cell.getIndex());
			final var results = TrntChkReport.load(session, PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile());
			try
			{
				new BatchTorrentResults((Stage)tvBatchToolsTorrent.getScene().getWindow(), results);
			}
			catch (URISyntaxException | IOException e1)
			{
				e1.printStackTrace();
			}
		}));
		popupMenuTorrent.setOnShowing(e -> 	mnDelTorrent.setDisable(tvBatchToolsTorrent.getSelectionModel().isEmpty()));
	}

	/**
	 * 
	 */
	private void initDat2Dir()
	{
		panelBatchToolsDat2Dir.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_cascade.png")));
		initDat2DirSrc();
		initDat2DirDst();
		cbBatchToolsDat2DirDryRun.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_dry_run, Boolean.class));
		cbBatchToolsDat2DirDryRun.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_dry_run, newValue));
		btnBatchToolsDir2DatStart.setGraphic(new ImageView(MainFrame.getIcon(ICON_BULLET_GO)));
		btnBatchToolsDir2DatStart.setOnAction(e -> startDir2Dat());
	}

	/**
	 * 
	 */
	private void initDat2DirDst()
	{
		tvBatchToolsDat2DirDst.setFixedCellSize(18);
		tvBatchToolsDat2DirDst.getItems().setAll(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr)));
		tvBatchToolsDat2DirDstDatsCol.setCellFactory(param -> new DropCell(tvBatchToolsDat2DirDst, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveDat2DirDst();
		}, file -> {
			if (Files.isRegularFile(file.toPath()))
				return file.getName().endsWith(".xml") || file.getName().endsWith(".dat");
			if (file.isDirectory())
				try
				{
					return Files.list(file.toPath()).map(Path::toFile).anyMatch(f -> f.getName().endsWith(".xml") || f.getName().endsWith(".dat"));
				}
				catch (IOException e1)
				{
					// do nothing
				}
			return false;
		}));
		tvBatchToolsDat2DirDstDatsCol.setCellValueFactory(param -> param.getValue().srcProperty());
		tvBatchToolsDat2DirDstDirsCol.setCellFactory(param -> new DropCell(tvBatchToolsDat2DirDst, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setDst(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveDat2DirDst();
		}, File::isDirectory));
		tvBatchToolsDat2DirDstDirsCol.setCellValueFactory(param -> param.getValue().dstProperty());
		tvBatchToolsDat2DirDstResultCol.setCellFactory(param -> new TableCell<SrcDstResult, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				if(empty)
					setText("");
				else
				{
					setText(item);
					setTooltip(new Tooltip(item));
				}
			}
		});
		tvBatchToolsDat2DirDstResultCol.setCellValueFactory(param -> param.getValue().resultProperty());
		tvBatchToolsDat2DirDstDetailsCol.setCellFactory(param -> getDat2DirDetailButtonCellFactory());
		tvBatchToolsDat2DirDstSelCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
			final var sdr = tvBatchToolsDat2DirDst.getItems().get(param);
			BooleanProperty observable = new SimpleBooleanProperty(sdr.isSelected());
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				sdr.setSelected(isNowSelected);
				saveDat2DirDst();
			});
			return observable;
		}));
		popupMenuDst.setOnShowing(e -> {
			mntmDat2DirDstPresets.setDisable(tvBatchToolsDat2DirDst.getSelectionModel().isEmpty());
			mnDat2DirDelDstDat.setDisable(tvBatchToolsDat2DirDst.getSelectionModel().isEmpty());
		});
	}

	/**
	 * @return
	 */
	private ButtonCellFactory<SrcDstResult, SrcDstResult> getDat2DirDetailButtonCellFactory()
	{
		return new ButtonCellFactory<>("Detail", cell -> {
			final AbstractSrcDstResult sdr = tvBatchToolsDat2DirDst.getItems().get(cell.getIndex());
			final var results = DirUpdaterResults.load(session, new File(sdr.getSrc()));
			try
			{
				new BatchDirUpd8rResults((Stage)tvBatchToolsDat2DirDst.getScene().getWindow(), results);
			}
			catch (URISyntaxException | IOException e1)
			{
				e1.printStackTrace();
			}
		});
	}

	/**
	 * 
	 */
	private void initDat2DirSrc()
	{
		new DragNDrop(tvBatchToolsDat2DirSrc).addDirs(dirs -> {
			tvBatchToolsDat2DirSrc.getItems().addAll(dirs);
			saveDat2DirSrc();
		});
		tvBatchToolsDat2DirSrc.setFixedCellSize(18);
		tvBatchToolsDat2DirSrc.getItems().setAll(Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs), '|')).filter(s->!s.isBlank()).map(File::new).toList());
		tvBatchToolsDat2DirSrcCol.setCellFactory(param -> new TableCell<File, File>()
		{
			@Override
			protected void updateItem(File item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				setText(empty?"":item.toString());
			}
		});
		tvBatchToolsDat2DirSrcCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public File getValue()
			{
				return param.getValue();
			}
		});
		popupMenuSrc.setOnShowing(e -> mnDat2DirDelSrcDir.setDisable(tvBatchToolsDat2DirSrc.getSelectionModel().isEmpty()));
		mnDat2DirDelSrcDir.setOnAction(e -> {
			tvBatchToolsDat2DirSrc.getItems().removeAll(tvBatchToolsDat2DirSrc.getSelectionModel().getSelectedItems());
			saveDat2DirSrc();
		});
		mnDat2DirAddSrcDir.setOnAction(e -> chooseDir(tvBatchToolsDat2DirSrc, null, null, dir -> {
			tvBatchToolsDat2DirSrc.getItems().add(dir.toFile());
			saveDat2DirSrc();
		}));
	}

	/**
	 * 
	 */
	private void startDir2Dat()
	{
		if (!tvBatchToolsDat2DirSrc.getItems().isEmpty())
		{
			final List<SrcDstResult> sdrl = tvBatchToolsDat2DirDst.getItems();
			if (sdrl.stream().filter(sdr -> !session.getUser().getSettings().getProfileSettingsFile(PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile()).exists()).count() > 0)
				Dialogs.showAlert(Messages.getString("MainFrame.AllDatsPresetsAssigned"));
			else
			{
				try
				{
					final var updater = new ResultColUpdater()
					{
						@Override
						public void updateResult(int row, String result)
						{
							tvBatchToolsDat2DirDst.getItems().get(row).setResult(result);
						}

						@Override
						public void clearResults()
						{
							for(final AbstractSrcDstResult item : tvBatchToolsDat2DirDst.getItems())
								item.setResult("");
						}
					};
					final var thread = new Thread(buildDir2DatTask(sdrl, updater));
					thread.setDaemon(true);
					thread.start();
				}
				catch(URISyntaxException|IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
		else
			Dialogs.showAlert(Messages.getString("MainFrame.AtLeastOneSrcDir"));
	}

	/**
	 * 
	 */
	private void startTorrent()
	{
		if (!tvBatchToolsTorrent.getItems().isEmpty())
		{
			final List<SrcDstResult> sdrl = tvBatchToolsTorrent.getItems();

			final TrntChkMode mode = cbbxBatchToolsTrntChk.getSelectionModel().getSelectedItem();
			final ResultColUpdater updater = new ResultColUpdater()
			{
				@Override
				public void updateResult(int row, String result)
				{
					tvBatchToolsTorrent.getItems().get(row).setResult(result);
				}
				
				@Override
				public void clearResults()
				{
					for(final AbstractSrcDstResult item : tvBatchToolsTorrent.getItems())
						item.setResult("");
				}
			};
			final var opts = EnumSet.noneOf(TorrentChecker.Options.class);
			if (cbBatchToolsTrntChkRemoveUnknownFiles.isSelected())
				opts.add(TorrentChecker.Options.REMOVEUNKNOWNFILES);
			if (cbBatchToolsTrntChkRemoveWrongSizedFiles.isSelected())
				opts.add(TorrentChecker.Options.REMOVEWRONGSIZEDFILES);
			if (cbBatchToolsTrntChkDetectArchivedFolder.isSelected())
				opts.add(TorrentChecker.Options.DETECTARCHIVEDFOLDERS);

			try
			{
				final var thread = new Thread(buildTorrentTask(sdrl, mode, updater, opts));
				thread.setDaemon(true);
				thread.start();
			}
			catch(URISyntaxException|IOException ex)
			{
				Log.err(ex.getMessage(), ex);
				Dialogs.showError(ex);
			}
		}
		else
			Dialogs.showAlert(Messages.getString("MainFrame.AtLeastOneSrcDir"));
	}

	/**
	 * @param sdrl
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private ProgressTask<DirUpdater> buildDir2DatTask(final List<SrcDstResult> sdrl, ResultColUpdater updater) throws IOException, URISyntaxException
	{
		return new ProgressTask<DirUpdater>((Stage)tvBatchToolsDat2DirDst.getScene().getWindow())
		{

			@Override
			protected DirUpdater call() throws Exception
			{
				final var srclist = tvBatchToolsDat2DirSrc.getItems().stream().map(f -> PathAbstractor.getAbsolutePath(session, f.toString()).toFile()).toList();
				return new DirUpdater(session, sdrl, this, srclist, updater, cbBatchToolsDat2DirDryRun.isSelected());
			}
			
			@Override
			public void succeeded()
			{
				close();
				saveDat2DirDst();
				session.setCurrProfile(null);
				session.setCurrScan(null);
				session.getReport().setProfile(session.getCurrProfile());
				if (MainFrame.getProfileViewer() != null)
				{
					MainFrame.getProfileViewer().hide();
					MainFrame.setProfileViewer(null);
				}
				if (MainFrame.getReportFrame() != null)
					MainFrame.getReportFrame().hide();
				MainFrame.getController().getTabPane().getTabs().get(1).setDisable(true);
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

			
		};
	}

	/**
	 * @param sdrl
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private ProgressTask<TorrentChecker<SrcDstResult>> buildTorrentTask(final List<SrcDstResult> sdrl, TrntChkMode mode, ResultColUpdater updater, EnumSet<Options> opts) throws IOException, URISyntaxException
	{
		return new ProgressTask<TorrentChecker<SrcDstResult>>((Stage)tvBatchToolsDat2DirDst.getScene().getWindow())
		{

			@Override
			protected TorrentChecker<SrcDstResult> call() throws Exception
			{
				return new TorrentChecker<>(session, this, sdrl, mode, updater, opts);
			}
			
			@Override
			public void succeeded()
			{
				close();
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

			
		};
	}

	private void saveDat2DirDst()
	{
		session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(tvBatchToolsDat2DirDst.getItems()));
	}
	
	private void saveTorrentDst()
	{
		session.getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(tvBatchToolsTorrent.getItems()));
	}
	
	private void saveDat2DirSrc()
	{
		session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, String.join("|", tvBatchToolsDat2DirSrc.getItems().stream().map(File::getAbsolutePath).toList()));
	}
	
	@FXML void onCustomPresets(ActionEvent e)
	{
		try
		{
			if(!tvBatchToolsDat2DirDst.getSelectionModel().isEmpty())
				new CustomPresets((Stage)tvBatchToolsDat2DirDst.getScene().getWindow());
		}
		catch (IOException | URISyntaxException e1)
		{
			e1.printStackTrace();
		}
	}
	
	@FXML void onDelDat2DirDst(ActionEvent e)
	{
		tvBatchToolsDat2DirDst.getItems().removeAll(tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems());
		saveDat2DirDst();
	}
	
	@FXML void onAddDat2DirDstDat(ActionEvent e)
	{
		chooseOpenFileMulti(tvBatchToolsDat2DirDst, null, null, Arrays.asList(new FileChooser.ExtensionFilter("DAT files", "*.dat", "*.xml")), paths -> DropCell.process(tvBatchToolsDat2DirDst, tvBatchToolsDat2DirDst.getSelectionModel().getSelectedIndex(), paths.stream().map(Path::toFile).toList(), (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveDat2DirDst();
		}));
	}

	@FXML void onAddDat2DirDstDatDir(ActionEvent e)
	{
		chooseDir(tvBatchToolsDat2DirDst, null, null, path -> DropCell.process(tvBatchToolsDat2DirDst, tvBatchToolsDat2DirDst.getSelectionModel().getSelectedIndex(), Arrays.asList(path.toFile()), (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveDat2DirDst();
		}));
	}
	
	@FXML void onAddDat2DirDstDir(ActionEvent e)
	{
		chooseDir(tvBatchToolsDat2DirDst, null, null, path -> DropCell.process(tvBatchToolsDat2DirDst, tvBatchToolsDat2DirDst.getSelectionModel().getSelectedIndex(), Arrays.asList(path.toFile()), (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setDst(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveDat2DirDst();
		}));
	}
	
	@FXML void onTZIPPresets(ActionEvent e)
	{
		for (final AbstractSrcDstResult sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
			ProfileSettings.TZIP(session, PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile());
	}
	
	@FXML void onDIRPresets(ActionEvent e)
	{
		for (final AbstractSrcDstResult sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
			ProfileSettings.DIR(session, PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile());
	}
	
	@FXML void onAddTorrent(ActionEvent e)
	{
		chooseOpenFileMulti(tvBatchToolsTorrent, null, null, Arrays.asList(new FileChooser.ExtensionFilter("Torrent files", "*.torrent")), paths -> DropCell.process(tvBatchToolsTorrent, tvBatchToolsTorrent.getSelectionModel().getSelectedIndex(), paths.stream().map(Path::toFile).toList(), (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveTorrentDst();
		}));
	}
	
	@FXML void onAddTorrentDstDir(ActionEvent e)
	{
		chooseDir(tvBatchToolsTorrent, null, null, path -> DropCell.process(tvBatchToolsTorrent, tvBatchToolsTorrent.getSelectionModel().getSelectedIndex(), Arrays.asList(path.toFile()), (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setDst(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			saveTorrentDst();
		}));
	}
	
	@FXML void onDelTorrent(ActionEvent e)
	{
		tvBatchToolsTorrent.getItems().removeAll(tvBatchToolsTorrent.getSelectionModel().getSelectedItems());
		saveTorrentDst();
	}
	

	@FXML void onAddArchive(ActionEvent e)
	{
		chooseOpenFileMulti(tvBatchToolsCompressor, null, null, Arrays.asList(new FileChooser.ExtensionFilter("Archive files", "*.zip", "*.7z", "*.rar", "*.arj", "*.tar", "*.lzh", "*.lha", "*.tgz", "*.tbz", "*.tbz2", "*.rpm", "*.iso", "*.deb", "*.cab")), paths -> paths.stream().map(FileResult::new).forEachOrdered(tvBatchToolsCompressor.getItems()::add));
	}
	
	@FXML void onDelArchive(ActionEvent e)
	{
		tvBatchToolsCompressor.getItems().removeAll(tvBatchToolsCompressor.getSelectionModel().getSelectedItems());
	}
	
	public class CustomPresets extends Stage
	{
		ScannerPanelSettingsController controller;
		
		public CustomPresets(Stage parent) throws IOException, URISyntaxException
		{
			super();
			initOwner(parent);
			initModality(Modality.WINDOW_MODAL);
			getIcons().add(parent.getIcons().get(0));
			setOnShowing(e -> {
			});
			setOnCloseRequest(e -> hide());
			final var loader = new FXMLLoader(getClass().getResource("ScannerPanelSettings.fxml").toURI().toURL(), Messages.getBundle());
			final var settings = loader.<ScrollPane>load();
			controller = loader.getController();
			final var root = new BorderPane(settings);
			final var cancel = new Button("Cancel");
			cancel.setOnAction(e -> close());
			final var ok = new Button("OK");
			ok.setOnAction(e -> save());
			final var bar = new HBox(cancel, ok);
			bar.setPadding(new Insets(5));
			bar.setSpacing(5);
			bar.setAlignment(Pos.CENTER_RIGHT);
			root.setBottom(bar);
			setScene(new Scene(root, 600, 400));
			sizeToScene();

			AbstractSrcDstResult entry = tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems().get(0);
			controller.initProfileSettings(session.getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session, entry.getSrc()).toFile(), null));

			show();
		}
		
		private void save()
		{
			for (final AbstractSrcDstResult sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
				session.getUser().getSettings().saveProfileSettings(PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile(), controller.getSettings());
			close();
		}
	}
}
