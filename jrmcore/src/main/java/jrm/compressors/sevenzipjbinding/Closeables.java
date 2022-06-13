package jrm.compressors.sevenzipjbinding;

import java.io.Closeable;
import java.util.List;

public interface Closeables
{
	public List<Closeable> getCloseables();
	public void addCloseables(Closeable closeable);
}
