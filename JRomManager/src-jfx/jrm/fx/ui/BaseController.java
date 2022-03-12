package jrm.fx.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
		final var chooser = new DirectoryChooser();
		Optional.ofNullable(initialValue).filter(t -> !t.isBlank()).map(t -> PathAbstractor.getAbsolutePath(session, t).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, () -> chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}

	protected void chooseOpenFile(Control parent, String initialValue, File defdir, Callback cb)
	{
		final var chooser = new FileChooser();
		Optional.ofNullable(initialValue).filter(t -> !t.isBlank()).map(t -> PathAbstractor.getAbsolutePath(session, t).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, () -> chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showOpenDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}

	protected void chooseOpenFileMulti(Control parent, String initialValue, File defdir, CallbackMulti cb)
	{
		final var chooser = new FileChooser();
		Optional.ofNullable(initialValue).filter(t -> !t.isBlank()).map(t -> PathAbstractor.getAbsolutePath(session, t).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, () -> chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showOpenMultipleDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(chosen.stream().map(f -> PathAbstractor.getRelativePath(session, f.toPath())).toList());
	}

	protected void chooseSaveFile(Control parent, String initialValue, File defdir, Callback cb)
	{
		final var chooser = new FileChooser();
		Optional.ofNullable(initialValue).filter(t -> !t.isBlank()).map(t -> PathAbstractor.getAbsolutePath(session, t).toFile()).filter(File::isDirectory).ifPresentOrElse(chooser::setInitialDirectory, () -> chooser.setInitialDirectory(defdir));
		final var chosen = chooser.showSaveDialog(parent.getScene().getWindow());
		if (chosen != null)
			cb.call(PathAbstractor.getRelativePath(session, chosen.toPath()));
	}
}
