package jrm.fx.ui.misc;

import java.nio.file.Path;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileResult
{
	private ObjectProperty<Path> file;
	private StringProperty result;
	
	public FileResult(Path file)
	{
		this.file = new SimpleObjectProperty<>(file);
		this.result = new SimpleStringProperty();
	}
	
	public final ObjectProperty<Path> fileProperty()
	{
		return this.file;
	}
	
	public final Path getFile()
	{
		return this.fileProperty().get();
	}
	
	public final void setFile(final Path file)
	{
		this.fileProperty().set(file);
	}
	
	public final StringProperty resultProperty()
	{
		return this.result;
	}
	
	public final String getResult()
	{
		return this.resultProperty().get();
	}
	
	public final void setResult(final String result)
	{
		this.resultProperty().set(result);
	}
	
	
	
}