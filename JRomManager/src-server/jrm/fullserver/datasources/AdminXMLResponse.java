package jrm.fullserver.datasources;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import jrm.fullserver.security.CryptCredential;
import jrm.fullserver.security.Login;
import jrm.fullserver.security.UserCredential;
import jrm.server.shared.datasources.XMLRequest;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.server.shared.datasources.XMLResponse;
import lombok.val;

public class AdminXMLResponse extends XMLResponse
{
	public AdminXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void fetch(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			try (Login login = new Login())
			{
				val rows = login.queryHandler("SELECT * FROM USERS", new BeanListHandler<UserCredential>(UserCredential.class));
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeElement("startRow", "0");
				writer.writeElement("endRow", Integer.toString(rows.size()));
				writer.writeElement("totalRows", Integer.toString(rows.size()));
				writer.writeStartElement("data");
				for(val row : rows)
					writeRecord(row);
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch(Exception e)
			{
				failure(e.getMessage());
			}
		}
		else
			failure("Can't do that!");
	}

	private void writeRecord(UserCredential user) throws XMLStreamException
	{
		writer.writeStartElement("record");
		writer.writeAttribute("Login", user.getLogin());
		writer.writeAttribute("Password", user.getPassword());
		writer.writeAttribute("Roles", user.getRoles());
		writer.writeEndElement();
	}

	@Override
	public void add(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData("Login") && operation.hasData("Password"))
			{
				try (Login login = new Login())
				{
					login.update("INSERT INTO USERS VALUES(?, ?, ?)", operation.getData("Login"), CryptCredential.hash(operation.getData("Password")), Optional.ofNullable(operation.getData("Roles")).orElse("admin"));
					val user = login.queryHandler("SELECT * FROM USERS WHERE LOGIN=?", new BeanHandler<UserCredential>(UserCredential.class), operation.getData("Login"));
					if(user != null)
					{
						writer.writeStartElement("response");
						writer.writeElement("status", "0");
						writer.writeStartElement("data");
						writeRecord(user);
						writer.writeEndElement();
						writer.writeEndElement();
					}
					else
						failure();
				}
				catch(Exception e)
				{
					failure(e.getMessage());
				}
			}
			else
				failure();
		}
		else
			failure("Can't do that!");
	}

	@Override
	public void update(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData("Login") && operation.hasData("Password"))
			{
				try (Login login = new Login())
				{
					login.update("UPDATE USERS SET PASSWORD=?, ROLES=? WHERE LOGIN=?", CryptCredential.hash(operation.getData("Password")), Optional.ofNullable(operation.getData("Roles")).orElse("admin"), operation.getData("Login"));
					val user = login.queryHandler("SELECT * FROM USERS WHERE LOGIN=?", new BeanHandler<UserCredential>(UserCredential.class), operation.getData("Login"));
					if(user != null)
					{
						writer.writeStartElement("response");
						writer.writeElement("status", "0");
						writer.writeStartElement("data");
						writeRecord(user);
						writer.writeEndElement();
						writer.writeEndElement();
					}
					else
						failure();
				}
				catch(Exception e)
				{
					failure(e.getMessage());
				}
			}
			else
				failure();
		}
		else
			failure("Can't do that!");
	}

	@Override
	public void remove(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData("Login"))
			{
				try (Login login = new Login())
				{
					if(0 != login.update("DELETE FROM USERS WHERE LOGIN=?", operation.getData("Login")))
					{
						writer.writeStartElement("response");
						writer.writeElement("status", "0");
						writer.writeStartElement("data");
						writer.writeStartElement("record");
						writer.writeAttribute("Login", operation.getData("Login"));
						writer.writeEndElement();
						writer.writeEndElement();
						writer.writeEndElement();
					}
					else
						failure();
				}
				catch(Exception e)
				{
					failure(e.getMessage());
				}
			}
			else
				failure();
		}
		else
			failure("Can't do that!");
	}
}
