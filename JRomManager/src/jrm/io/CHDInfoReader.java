package jrm.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class CHDInfoReader implements CHDHeaderIntf
{
	CHDHeaderIntf header;

	public CHDInfoReader(final File chdfile) throws FileNotFoundException, IOException
	{
		try(FileInputStream is = new FileInputStream(chdfile))
		{
			final MappedByteBuffer bb = is.getChannel().map(MapMode.READ_ONLY, 0, Math.min(1024, chdfile.length()));
			final CHDHeader header = new CHDHeader(bb);
			this.header = header;
			if(header.isValidTag())
			{
				switch(header.getVersion())
				{
					case 1:
						this.header = new CHDHeaderV1(bb, header);
						break;
					case 2:
						this.header = new CHDHeaderV2(bb, header);
						break;
					case 3:
						this.header = new CHDHeaderV3(bb, header);
						break;
					case 4:
						this.header = new CHDHeaderV4(bb, header);
						break;
					case 5:
						this.header = new CHDHeaderV5(bb, header);
						break;
				}
			}
		}
	}

	@Override
	public String getSHA1()
	{
		return header.getSHA1();
	}

	@Override
	public String getMD5()
	{
		return header.getMD5();
	}

	@Override
	public boolean isValidTag()
	{
		return header.isValidTag();
	}

	@Override
	public int getLen()
	{
		return header.getLen();
	}

	@Override
	public int getVersion()
	{
		return header.getVersion();
	}

}
