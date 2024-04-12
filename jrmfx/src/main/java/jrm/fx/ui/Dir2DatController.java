package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.progress.ProgressTask;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.Dir2Dat;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.DirScan.Options;
import jrm.security.Session;

public class Dir2DatController extends BaseController
{
	@FXML CheckBox includeEmptyDirs;
	@FXML CheckBox matchCurrentProfile;
	@FXML CheckBox doNotScan;
	@FXML CheckBox junkSubfolders;
	@FXML CheckBox addShamd;
	@FXML CheckBox addMd;
	@FXML CheckBox deepScanFor;
	@FXML CheckBox scanSubfolders;
	@FXML TextField name;
	@FXML TextField description;
	@FXML TextField version;
	@FXML TextField author;
	@FXML TextField comment;
	@FXML TextField category;
	@FXML TextField date;
	@FXML TextField email;
	@FXML TextField homepage;
	@FXML TextField url;
	@FXML TextField srcDir;
	@FXML TextField dstDat;
	@FXML Button srcDirBtn;
	@FXML Button dstDatBtn;
	@FXML Button generate;
	@FXML ToggleGroup format;
	@FXML RadioButton formatMame;
	@FXML RadioButton formatLogiqxDat;
	@FXML RadioButton formatSWList;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		scanSubfolders.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, Boolean.class));
		scanSubfolders.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, newValue));
		deepScanFor.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, Boolean.class)); //$NON-NLS-1$
		deepScanFor.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, newValue)); //$NON-NLS-1$
		addMd.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, Boolean.class)); //$NON-NLS-1$
		addMd.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, newValue)); //$NON-NLS-1$
		addShamd.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, Boolean.class)); //$NON-NLS-1$
		addShamd.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, newValue)); //$NON-NLS-1$
		junkSubfolders.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, Boolean.class)); //$NON-NLS-1$
		junkSubfolders.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, newValue)); //$NON-NLS-1$
		doNotScan.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, Boolean.class)); //$NON-NLS-1$
		doNotScan.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, newValue)); //$NON-NLS-1$
		matchCurrentProfile.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, Boolean.class)); //$NON-NLS-1$
		matchCurrentProfile.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, newValue)); //$NON-NLS-1$
		includeEmptyDirs.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, Boolean.class)); //$NON-NLS-1$
		includeEmptyDirs.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, newValue)); //$NON-NLS-1$
		new DragNDrop(srcDir).addDir(txt -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, txt));
		srcDir.setText(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_src_dir));
		ImageView srciv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/disk.png"));
		srciv.setPreserveRatio(true);
		srciv.getStyleClass().add("icon");
		srcDirBtn.setGraphic(srciv);
		srcDirBtn.setOnAction(e -> {
			final var workdir = session.getUser().getSettings().getWorkPath(); // $NON-NLS-1$
			final var lastsrcdir = Optional.ofNullable(session.getUser().getSettings().getProperty(SettingsEnum.dir2dat_lastsrcdir)).map(File::new).filter(File::exists).orElse(workdir.toFile());
			chooseDir(srcDirBtn, srcDir.getText(), lastsrcdir, path -> {
				session.getUser().getSettings().setProperty("MainFrame.ChooseDatSrc", path.toString()); //$NON-NLS-1$
				srcDir.setText(path.toString());
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, srcDir.getText()); // $NON-NLS-1$
				session.getUser().getSettings().setProperty(SettingsEnum.dir2dat_lastsrcdir, Optional.ofNullable(path).map(Path::toFile).map(File::getParent).orElse(null));
			});
		});
		new DragNDrop(dstDat).addNewFile(txt -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, txt));
		dstDat.setText(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_dst_file));
		ImageView dstiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/disk.png"));
		dstiv.setPreserveRatio(true);
		dstiv.getStyleClass().add("icon");
		dstDatBtn.setGraphic(dstiv);
		dstDatBtn.setOnAction(e -> {
			final var workdir = session.getUser().getSettings().getWorkPath(); // $NON-NLS-1$
			final var lastdstdir = Optional.ofNullable(session.getUser().getSettings().getProperty(SettingsEnum.dir2dat_lastdstdir)).map(File::new).filter(File::exists).orElse(workdir.toFile());
			chooseSaveFile(dstDatBtn, dstDat.getText(), lastdstdir, Collections.singletonList(new FileChooser.ExtensionFilter("Dat file","*.xml","*.dat")), path -> {
				session.getUser().getSettings().setProperty("MainFrame.ChooseDatDst", path.toString()); //$NON-NLS-1$
				dstDat.setText(path.toString());
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, dstDat.getText()); // $NON-NLS-1$
				session.getUser().getSettings().setProperty(SettingsEnum.dir2dat_lastdstdir, Optional.ofNullable(path).map(Path::toFile).map(File::getParent).orElse(null));
			});
		});
		format.selectToggle(switch(ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format)))
		{
			case MAME -> formatMame;
			case DATAFILE -> formatLogiqxDat;
			case SOFTWARELIST -> formatSWList;
			default -> formatMame;
		});
		format.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue==formatMame)
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString());
			else if(newValue==formatLogiqxDat)
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.DATAFILE.toString());
			else if(newValue==formatSWList)
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.SOFTWARELIST.toString());
		});
		generate.setOnAction(e -> dir2dat());
	}

	/**
	 * Dir2Dat
	 */
	private void dir2dat()
	{
		try
		{
			final var thread = new Thread(dir2DatTask());
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
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private ProgressTask<Void> dir2DatTask() throws IOException, URISyntaxException
	{
		return new ProgressTask<Void>((Stage)dstDat.getScene().getWindow())
		{
			@Override
			protected Void call() throws Exception
			{
				final String src = srcDir.getText();
				final String dst = dstDat.getText();
				if (src != null && src.length() > 0 && dst != null && dst.length() > 0)
				{
					final File srcdir = new File(src);
					if (srcdir.isDirectory())
					{
						final File dstdat = new File(dst);
						if (dstdat.getParentFile().isDirectory() && (dstdat.exists() || dstdat.createNewFile()))
						{
							final var options = initOptions(session);
							final var type = ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format)); //$NON-NLS-1$
							final var headers = initHeaders();
							new Dir2Dat(session, srcdir, dstdat, this, options, type, headers);
						}
					}
				}
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
	 * @param session
	 * @return
	 */
	private EnumSet<DirScan.Options> initOptions(final Session session)
	{
		EnumSet<DirScan.Options> options = EnumSet.of(Options.USE_PARALLELISM, Options.MD5_DISKS, Options.SHA1_DISKS);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, Boolean.class))) //$NON-NLS-1$
			options.add(Options.RECURSE);
		if (Boolean.FALSE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, Boolean.class))) //$NON-NLS-1$
			options.add(Options.IS_DEST);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, Boolean.class))) //$NON-NLS-1$
			options.add(Options.NEED_MD5);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, Boolean.class))) //$NON-NLS-1$
			options.add(Options.NEED_SHA1);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, Boolean.class))) //$NON-NLS-1$
			options.add(Options.JUNK_SUBFOLDERS);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, Boolean.class))) //$NON-NLS-1$
			options.add(Options.ARCHIVES_AND_CHD_AS_ROMS);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, Boolean.class))) //$NON-NLS-1$
			options.add(Options.MATCH_PROFILE);
		if (Boolean.TRUE.equals(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, Boolean.class))) //$NON-NLS-1$
			options.add(Options.EMPTY_DIRS);
		return options;
	}

	/**
	 * @return
	 */
	private HashMap<String, String> initHeaders()
	{
		HashMap<String, String> headers = new HashMap<>();
		headers.put("name", name.getText()); //$NON-NLS-1$
		headers.put("description", description.getText()); //$NON-NLS-1$
		headers.put("version", version.getText()); //$NON-NLS-1$
		headers.put("author", author.getText()); //$NON-NLS-1$
		headers.put("comment", comment.getText()); //$NON-NLS-1$
		headers.put("category", category.getText()); //$NON-NLS-1$
		headers.put("date", date.getText()); //$NON-NLS-1$
		headers.put("email", email.getText()); //$NON-NLS-1$
		headers.put("homepage", homepage.getText()); //$NON-NLS-1$
		headers.put("url", url.getText()); //$NON-NLS-1$
		return headers;
	}
}
