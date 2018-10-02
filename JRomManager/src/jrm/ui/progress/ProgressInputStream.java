package jrm.ui.progress;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ProgressInputStream.
 *
 * @author optyfr
 */
public final class ProgressInputStream extends FilterInputStream
{
	
	/** The value. */
	private int value;
	
	/** Progress handler inteface */
	private ProgressHandler progress;

	/**
	 * Instantiates a new progress input stream.
	 *
	 * @param in the in
	 * @param len the len
	 */
	public ProgressInputStream(final InputStream in, final Integer len, final ProgressHandler progress)
	{
		super(in);
		this.progress = progress;
		progress.setProgress(null, (value = 0), len);
	}

	@Override
	public int read() throws IOException
	{
		final int ret = super.read();
		if (ret != -1)
			progress.setProgress(null, ++value);
		return ret;
	}

	@Override
	public int read(final byte[] b) throws IOException
	{
		final int ret = super.read(b);
		if (ret != -1)
			progress.setProgress(null, (value += ret));
		return ret;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException
	{
		final int ret = super.read(b, off, len);
		if (ret != -1)
			progress.setProgress(null, (value += ret));
		return ret;
	}

	@Override
	public long skip(final long n) throws IOException
	{
		final long ret = super.skip(n);
		if (ret != -1)
			progress.setProgress(null, (value += ret));
		return ret;
	}
	
	@Override
	public void close() throws IOException
	{
		// TODO Auto-generated method stub
		super.close();
	}
}