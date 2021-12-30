package jrm.fx.ui.misc;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javafx.stage.Stage;
import jrm.misc.Log;

public class Settings
{
	private Settings()
	{
		// Do not instantiate
	}
	
	private static Map<String, Marshaller> marshallers = new HashMap<>();
	
	private static Marshaller getMarshaller(Class<?> c) throws JAXBException
	{
		if(!marshallers.containsKey(c.getSimpleName()))
		{
			final var context = JAXBContext.newInstance(c);
			final var m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
			marshallers.put(c.getSimpleName(), m);
			return m;
		}
		return marshallers.get(c.getSimpleName());
	}
	
	private static Map<String, Unmarshaller> unmarshallers = new HashMap<>();
	
	private static Unmarshaller getUnmarshaller(Class<?> c) throws JAXBException
	{
		if(!unmarshallers.containsKey(c.getSimpleName()))
		{
			final var context = JAXBContext.newInstance(c);
			final var u = context.createUnmarshaller();
			u.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			u.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
			unmarshallers.put(c.getSimpleName(), u);
			return u;
		}
		return unmarshallers.get(c.getSimpleName());
	}
	
	public static String toJson(Stage window)
	{
		try
		{
			final var sw = new StringWriter();
			getMarshaller(WindowState.class).marshal(WindowState.getInstance(window), sw);
			return sw.toString();
		}
		catch (JAXBException e)
		{
			Log.err(e.getMessage(), e);
		}
		return null;
	}

	public static void fromJson(String json, Stage window)
	{
		if(json==null || json.isEmpty())
			return;
		try
		{
			final var ss = new StreamSource(new StringReader(json));
			final var state = getUnmarshaller(WindowState.class).unmarshal(ss,WindowState.class).getValue();
			state.restore(window);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
		}
	}
}
