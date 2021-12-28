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
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.stage.Stage;
import jrm.misc.Log;
import lombok.Data;

public class Settings
{
	private Settings()
	{
		// Do not instantiate
	}
	
	@XmlRootElement
	private static @Data class WindowState
	{
		private double x;
		private double y;
		private double w;
		private double h;
		private boolean m = false;
		private boolean f = false;
		private boolean i = false;

		public static WindowState get(Stage window)
		{
			final var w = new WindowState();
			w.m = window.isMaximized();
			w.f = window.isFullScreen();
			w.i = window.isIconified();
			window.setIconified(false);
			window.setFullScreen(false);
			window.setMaximized(false);
			w.x = window.getX();
			w.y = window.getY();
			w.w = window.getWidth();
			w.h = window.getHeight();
			return w;
		}
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
	
	public static String marshal(Stage window)
	{
		try
		{
			final var sw = new StringWriter();
			getMarshaller(WindowState.class).marshal(WindowState.get(window), sw);
			return sw.toString();
		}
		catch (JAXBException e)
		{
			Log.err(e.getMessage(), e);
		}
		return null;
	}

	public static void unmarshal(String json, Stage window)
	{
		if(json==null || json.isEmpty())
			return;
		try
		{
			final var ss = new StreamSource(new StringReader(json));
			final var state = getUnmarshaller(WindowState.class).unmarshal(ss,WindowState.class).getValue();
			window.setX(state.x);
			window.setY(state.y);
			window.setWidth(state.w);
			window.setHeight(state.h);
			window.setMaximized(state.m);
			window.setFullScreen(state.f);
			window.setIconified(state.i);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
		}
	}
}
