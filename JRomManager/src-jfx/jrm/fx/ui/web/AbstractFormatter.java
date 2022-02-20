package jrm.fx.ui.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.experimental.UtilityClass;

public @UtilityClass class AbstractFormatter
{
	private class Handler extends DefaultHandler
	{
		private Pane root;
		
		private Label current = null;
		
		private StringBuilder buffer = new StringBuilder(); 
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			switch(qName)
			{
				case "hbox":
					root = new HBox();
					System.out.println("hbox");
					break;
				case "label":
					flush();
					current = new Label();
					for(int i = 0 ; i < attributes.getLength(); i++)
					{
						switch(attributes.getQName(i))
						{
							case "color":
								current.setTextFill(Color.web(attributes.getValue(i)));
								break;
						}
					}
					break;
				case "progress":
				{
					flush();
					int width = 100;
					int value = 0;
					int max = 100;
					for (int i = 0; i < attributes.getLength(); i++)
					{
						switch (attributes.getQName(i))
						{
							case "width":
								width = Integer.parseInt(attributes.getValue(i));
								break;
							case "value":
								value = Integer.parseInt(attributes.getValue(i));
								break;
							case "max":
								max = Integer.parseInt(attributes.getValue(i));
								break;
						}
					}
					final var progress = new ProgressBar();
					progress.setPrefWidth(width);
					progress.setProgress((double) value / (double) max);
					System.out.println("progress value=%d max=%d width=%d".formatted(value, max, width));
					root.getChildren().add(progress);
					break;
				}
				/*				case "b":
				{
					Label oldCurrent = current;
					flush();
					current = new Label();
					if(oldCurrent!=null)
						current.setTextFill(oldCurrent.getTextFill());
					current.setFont(Font.font("sans-serif",FontWeight.BOLD,12));
					break;
				}
				case "i":
				{
					Label oldCurrent = current;
					flush();
					current = new Label();
					if(oldCurrent!=null)
						current.setTextFill(oldCurrent.getTextFill());
					current.setFont(Font.font("sans-serif",FontWeight.NORMAL,FontPosture.ITALIC,12));
					break;
				}*/
				default:
					break;
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			switch(qName)
			{
				case "hbox", "label":
					flush();
					break;
				default:
					break;
			}
		}
		
		private void flush()
		{
			if (buffer.length() > 0)
			{
				if (current == null)
					current = new Label();
				current.setText(buffer.toString());
				buffer.setLength(0);
				System.out.println("label '%s' color=%s".formatted(current.getText(), current.getTextFill()));
				root.getChildren().add(current);
				current = null;
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			buffer.append(ch, start, length);
		}
	}
	
	public static Node toNode(String xml, Color color)
	{
		if(xml==null)
			return new Label();
		if(xml.startsWith("<hbox>"))
		{
			System.out.println(xml);
			try (final var in = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+xml).getBytes(StandardCharsets.UTF_8)))
			{
				final var factory = SAXParserFactory.newInstance();
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				final var parser = factory.newSAXParser();
				parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
				parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
				final var handler = new Handler();
				parser.parse(in, handler);
				return handler.root;
			}
			catch (SAXException | ParserConfigurationException | IOException e)
			{
				e.printStackTrace();
			}
		}
		return new Label(xml);
	}
}
