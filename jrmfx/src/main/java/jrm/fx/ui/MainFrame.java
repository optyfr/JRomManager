package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.misc.Settings;
import jrm.fx.ui.profile.ProfileViewer;
import jrm.fx.ui.profile.report.ReportFrame;
import jrm.fx.ui.progress.ProgressTask;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.EnumWithDefault;
import jrm.misc.Log;
import jrm.profile.data.ExportMode;
import jrm.profile.data.SoftwareList;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class MainFrame extends Application
{
	private static @Getter MainFrameController controller;

	private static @Getter @Setter ReportFrame reportFrame;
	
	private static @Getter @Setter ProfileViewer profileViewer;

	private static @Getter Session session = Sessions.getSingleSession();
	
	private static @Getter Application application;
	
	private static @Getter @Setter JRMScene primaryScene;
	
	public static void launch()
	{
		Application.launch();
	}

	@Override
	public void start(Stage primaryStage)
	{
		System.out.println("Starting...");
		setApplication(this);
		
		JRMScene.setSheet(session.getUser().getSettings().getEnumProperty(JRMScene.ScenePrefs.style_sheet, JRMScene.StyleSheet.class));
		final var loading = new Loading();
		Platform.runLater(() -> {
			try
			{
				primaryStage.setOnCloseRequest(e -> {
					session.getUser().getSettings().setProperty("MainFrame.Bounds", Settings.toJson(primaryStage));
					controller.getSettingsPanelController().scheduler.shutdown();
				});
				primaryStage.getIcons().add(getIcon("/jrm/resicons/rom.png"));
				primaryStage.setTitle(Messages.getString("MainFrame.Title") + " " + getVersion());
				setPrimaryScene(new JRMScene(loadMain()));
				primaryStage.setScene(primaryScene);
				setReportFrame(new ReportFrame(primaryStage));
				Settings.fromJson(session.getUser().getSettings().getProperty("MainFrame.Bounds", null), primaryStage);
				primaryStage.show();
			}
			catch (URISyntaxException | IOException e)
			{
				e.printStackTrace();
			}
			loading.hide();
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (Sessions.getSingleSession().getCurrProfile() != null)
				Sessions.getSingleSession().getCurrProfile().saveSettings();
			Sessions.getSingleSession().getUser().getSettings().saveSettings();
			System.out.println("Shutdown");
		}));
	}

	public static void applyCSS()
	{
		JRMScene.setSheet(session.getUser().getSettings().getEnumProperty(JRMScene.ScenePrefs.style_sheet, JRMScene.StyleSheet.class));
		getPrimaryScene().applySheet();
		if(reportFrame != null && reportFrame.getScene() instanceof JRMScene s)
			s.applySheet();
		if(profileViewer != null && profileViewer.getScene() instanceof JRMScene s)
			s.applySheet();
	}
	
	private static synchronized TabPane loadMain() throws URISyntaxException, IOException
	{
		final var loader = new FXMLLoader(MainFrame.class.getResource("MainFrame.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<TabPane>load();
		controller = loader.getController();
		return root;
	}
	
	private static HashMap<String, Image> iconsCache = new HashMap<>();
	private static Optional<Module> iconsModule = ModuleLayer.boot().findModule("res.icons");

	public static Image getIcon(String res)
	{
		if (!iconsCache.containsKey(res))
		{
			iconsModule.ifPresentOrElse(module -> {
				try (final var in = module.getResourceAsStream(res))
				{
					if (in != null)
						iconsCache.put(res, new Image(in));
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
			}, () -> {
				try (final var in = MainFrame.class.getResourceAsStream(res))
				{
					if (in != null)
						iconsCache.put(res, new Image(in));
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
			});
		}
		return iconsCache.get(res);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	private String getVersion()
	{
		String version = ""; //$NON-NLS-1$
		final var pkg = getClass().getPackage();
		if (pkg.getSpecificationVersion() != null)
		{
			version += pkg.getSpecificationVersion(); // $NON-NLS-1$
			if (pkg.getImplementationVersion() != null)
				version += "." + pkg.getImplementationVersion(); //$NON-NLS-1$
		}
		return version;
	}

	private static synchronized void setApplication(Application application)
	{
		MainFrame.application = application;
	}

	public static void export(Window owner, final Session session, final ExportType type, final Set<ExportMode> modes, final SoftwareList selection)
	{
		final var chooser = new FileChooser();
		Optional.ofNullable(session.getUser().getSettings().getProperty("MainFrame.ChooseExeOrDatToExport", (String) null)).map(File::new).ifPresent(chooser::setInitialDirectory);
		chooser.setTitle(Messages.getString("ProfileViewer.ChooseDestinationFile"));
		final var fnef = new FileChooser.ExtensionFilter(Messages.getString("MainFrame.DatFile"), "xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		chooser.getExtensionFilters().add(fnef);
		chooser.setSelectedExtensionFilter(fnef);
		final var chosen = chooser.showSaveDialog(owner);
		if (chosen != null)
		{
			try
			{
				final var thread = new Thread(new ProgressTask<Void>((Stage) owner)
				{
					@Override
					protected Void call() throws Exception
					{
						Export.export(session.getCurrProfile(), chosen, type, modes, selection, this);
						return null;
					}
					
					@Override
					protected void succeeded()
					{
						close();
					}

					@Override
					protected void failed()
					{
						close();
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
	}
}
