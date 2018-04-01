package jrm.compressors;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface Archive extends Closeable, AutoCloseable
{
	public File getTempDir() throws IOException;
	public File extract(String entry) throws IOException;
	public InputStream extract_stdout(String entry) throws IOException;
	public int add(String entry) throws IOException;
	public int add(File baseDir, String entry) throws IOException;
	public int add_stdin(InputStream src, String entry) throws IOException;
	public int delete(String entry) throws IOException;
	public int rename(String entry, String newname) throws IOException;
}
