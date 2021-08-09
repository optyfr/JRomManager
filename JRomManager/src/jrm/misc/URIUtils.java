package jrm.misc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

public @UtilityClass class URIUtils
{
	public static boolean URIExists(String path)	//NOSONAR
	{
		try
		{
			Path p = getPath(path);
			return Files.exists(p);
		}
		catch (Exception e)
		{
			return Files.exists(Paths.get(path));
		}
	}

	public static boolean URIExists(URI uri)	//NOSONAR
	{
		try
		{
			return Files.exists(getPath(uri));
		}
		catch (Exception e)
		{
			return false;
		}
	}
	

	public static Path getPath(String path)
	{
		try
		{
			var uri = URI.create(path);
			if(uri.getScheme().startsWith("jrt") && !uri.getPath().startsWith("modules"))
				uri = new URI("jrt:/modules/" + uri.getPath());
			return Path.of(uri);
		}
		catch (Exception e)
		{
			return Paths.get(path);
		}
		
	}
	
	public static Path getPath(URI uri) throws URISyntaxException
	{
		if(uri.getScheme().startsWith("jrt") && !uri.getPath().startsWith("modules"))
			uri = new URI("jrt:/modules/" + uri.getPath());
		return Path.of(uri);
	}
	
	public static String readString(String path) throws IOException
	{
		try(final var reader = Files.newBufferedReader(getPath(path), StandardCharsets.UTF_8))
		{
			return reader.lines().collect(Collectors.joining());
		}
	}

}
