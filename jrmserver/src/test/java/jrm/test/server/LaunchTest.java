package jrm.test.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jrm.fullserver.FullServer;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LauncherTest
{
	
	
	@Test
	@Order(1)
	void tryTerminateFirst()
	{
		try
		{
			FullServer.terminate();
		}
		catch(Exception e)
		{
			fail(e);
		}
	}
	
	@Test
	@Order(2)
	void initialize()
	{
		try
		{
			FullServer.parseArgs(
				"--client=" + Paths.get(System.getProperty("JRomManager.rootPath")).resolve("WebClient").resolve("war"),
				"--cert=" + Paths.get(System.getProperty("JRomManager.rootPath")).resolve("JRomManager").resolve("certs").resolve("localhost.pfx"),
				"--debug"
			);
			FullServer.initialize();
		}
		catch(Exception e)
		{
			fail(e);
		}
	}
	
	@Test
	@Order(3)
	void initializeAgain()
	{
		try
		{
			FullServer.initialize();
		}  
		catch(Exception e)
		{
			fail(e);
		}
	}
	
	@Test
	@Order(4)
	void finallyTerminate()
	{
		try
		{
			FullServer.terminate();
		}
		catch(Exception e)
		{
			fail(e);
		}
	}


}