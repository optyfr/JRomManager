package jrm.profile.scan.options;

import java.util.EnumSet;

import jrm.Messages;
import jrm.compressors.zipfs.ZipFileSystemProvider;

/**
 * The supported format options
 * @author optyfr
 *
 */
public enum FormatOptions
{
	/**
	 * Standard folder
	 */
	DIR(Messages.getString("FormatOptions.Directories"), Ext.DIR), //$NON-NLS-1$
	/**
	 * Zip (internally handled via {@link ZipFileSystemProvider})
	 */
	ZIP(Messages.getString("FormatOptions.Zip"), Ext.ZIP), //$NON-NLS-1$
	/**
	 * Zip (either handled by SevenzipJBinding or by an external program)
	 */
	ZIPE(Messages.getString("FormatOptions.ZipExternal"), Ext.ZIP), //$NON-NLS-1$
	/**
	 * SevenZip (either handled by SevenzipJBinding or by an external program)
	 */
	SEVENZIP(Messages.getString("FormatOptions.SevenZip"), Ext.SEVENZIP), //$NON-NLS-1$
	/**
	 * Zip (torrentzipped by jtrrntzip)
	 */
	TZIP(Messages.getString("FormatOptions.TorrentZip"), Ext.ZIP); //$NON-NLS-1$

	/**
	 * Supported file container extensions
	 */
	public enum Ext
	{
		/**
		 * folder (no extension)
		 */
		DIR(""), //$NON-NLS-1$
		/**
		 * .zip for zip format
		 */
		ZIP(".zip"), //$NON-NLS-1$
		/**
		 * .7z for sevenzip format
		 */
		SEVENZIP(".7z"); //$NON-NLS-1$

		private String ext;

		private Ext(String ext)
		{
			this.ext = ext;
		}

		@Override
		public String toString()
		{
			return ext;
		}

		/**
		 * get all extensions except the current one (and {@link #DIR})
		 * @return an {@link EnumSet} of {@link Ext}
		 */
		public EnumSet<Ext> allExcept()
		{
			return EnumSet.complementOf(EnumSet.of(this, DIR));
		}

		/**
		 * is it a directory
		 * @return true if it's a directory
		 */
		public boolean isDir()
		{
			return this == DIR;
		}
	}

	/**
	 * Format description 
	 */
	private String desc;
	/**
	 * Format extension
	 */
	private Ext ext;

	/**
	 * internal constructor
	 * @param desc the description
	 * @param ext the extension ({@link Ext})
	 */
	private FormatOptions(String desc, Ext ext)
	{
		this.desc = desc;
		this.ext = ext;
	}

	/**
	 * get the extension
	 * @return {@link Ext}
	 */
	public Ext getExt()
	{
		return ext;
	}

	/**
	 * get the description
	 * @return a description {@link String}
	 */
	public String getDesc()
	{
		return desc;
	}

	/**
	 * get all formats except current one (and {@link #DIR})
	 * @return an {@link EnumSet} of {@link FormatOptions}
	 */
	public EnumSet<FormatOptions> allExcept()
	{
		return EnumSet.complementOf(EnumSet.of(this, DIR));
	}
}
