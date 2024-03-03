package jrm.fx.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.security.Sessions;

abstract class BaseController implements Initializable
{
	protected final Session session = Sessions.getSingleSession();

	interface Callback
	{
		void call(Path path);
	}

	interface CallbackMulti
	{
		void call(List<Path> paths);
	}

	protected void chooseDir(Control parent, String initialValue, File defdir, Callback cb)
	{
		final var chooser = initDirectoryChooser(initialValue, defdir);
		final var chosen = chooser.showDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}

	protected void chooseOpenFile(Control parent, String initialValue, File defdir, Collection<ExtensionFilter> filters, Callback cb)
	{
		final var chooser = initFileChooser(initialValue, defdir);
		if (filters != null)
			chooser.getExtensionFilters().addAll(filters);
		final var chosen = chooser.showOpenDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}

	protected void chooseOpenFileMulti(Control parent, String initialValue, File defdir, Collection<ExtensionFilter> filters, CallbackMulti cb)
	{
		final var chooser = initFileChooser(initialValue, defdir);
		if (filters != null)
			chooser.getExtensionFilters().addAll(filters);
		final var chosen = chooser.showOpenMultipleDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(chosen.stream().map(f -> PathAbstractor.getRelativePath(session, f.toPath())).toList());
	}

	protected void chooseSaveFile(Control parent, String initialValue, File defdir, Collection<ExtensionFilter> filters, Callback cb)
	{
		final var chooser = initFileChooser(initialValue, defdir);
		if (filters != null)
			chooser.getExtensionFilters().addAll(filters);
		final var chosen = chooser.showSaveDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}

	private FileChooser initFileChooser(String initialValue, File defdir) //NOSONAR
	{
		final var chooser = new FileChooser();
		var initialized = false;
		if (initialValue != null && !initialValue.isBlank())
		{
			final var initial = PathAbstractor.getAbsolutePath(session, initialValue);
			if (initial != null)
			{
				if (Files.exists(initial) && Files.isDirectory(initial))
				{
					chooser.setInitialDirectory(initial.toFile());
					initialized = true;
				}
				else
				{
					final var parent = initial.getParent();
					if (Files.exists(parent) && Files.isDirectory(parent))
					{
						chooser.setInitialDirectory(initial.getParent().toFile());
						initialized = true;
					}
					chooser.setInitialFileName(initial.getFileName().toString());
				}
			}
		}
		if (!initialized && defdir != null && defdir.exists())
			chooser.setInitialDirectory(defdir);
		return chooser;
	}

	private DirectoryChooser initDirectoryChooser(String initialValue, File defdir)
	{
		final var chooser = new DirectoryChooser();
		if (initialValue != null && !initialValue.isBlank())
		{
			final var initial = PathAbstractor.getAbsolutePath(session, initialValue);
			if (initial != null &&  (Files.exists(initial) && Files.isDirectory(initial)))
				chooser.setInitialDirectory(initial.toFile());
			else if (defdir != null && defdir.exists())
				chooser.setInitialDirectory(defdir);
		}
		else if (defdir != null && defdir.exists())
			chooser.setInitialDirectory(defdir);
		return chooser;
	}

}
