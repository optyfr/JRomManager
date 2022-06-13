package jrm.fullserver.datasources;

import java.io.IOException;
import java.sql.SQLException;
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
	private static final String CAN_T_DO_THAT = "Can't do that!";
	private static final String ROLES = "Roles";
	private static final String PASSWORD = "Password";
	private static final String LOGIN = "Login";
	private static final String RESPONSE = "response";
	private static final String STATUS = "status";

	public AdminXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	@Override
	public void fetch(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			try (final var login = new Login())
			{
				val rows = login.queryHandler("SELECT * FROM USERS", new BeanListHandler<UserCredential>(UserCredential.class));
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				fetchList(operation, rows, (row, idx) -> writeRecord(row));
				writer.writeEndElement();
			}
			catch(Exception e)
			{
				failure(e.getMessage());
			}
		}
		else
			failure(CAN_T_DO_THAT);
	}

	private void writeRecord(UserCredential user) throws XMLStreamException
	{
		writer.writeStartElement("record");
		writer.writeAttribute(LOGIN, user.getLogin());
		writer.writeAttribute(PASSWORD, user.getPassword());
		writer.writeAttribute(ROLES, user.getRoles());
		writer.writeEndElement();
	}

	@Override
	public void add(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData(LOGIN) && operation.hasData(PASSWORD))
			{
				try (final var login = new Login())
				{
					login.update("INSERT INTO USERS VALUES(?, ?, ?)", operation.getData(LOGIN), CryptCredential.hash(operation.getData(PASSWORD)), Optional.ofNullable(operation.getData(ROLES)).orElse("admin"));
					fetchSingle(operation, login);
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
			failure(CAN_T_DO_THAT);
	}

	@Override
	public void update(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData(LOGIN) && operation.hasData(PASSWORD))
			{
				try (final var login = new Login())
				{
					login.update("UPDATE USERS SET PASSWORD=?, ROLES=? WHERE LOGIN=?", CryptCredential.hash(operation.getData(PASSWORD)), Optional.ofNullable(operation.getData(ROLES)).orElse("admin"), operation.getData(LOGIN));
					fetchSingle(operation, login);
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
			failure(CAN_T_DO_THAT);
	}

	@Override
	public void remove(Operation operation) throws XMLStreamException
	{
		if(request.getSession().getUser().isAdmin())
		{
			if(operation.hasData(LOGIN))
			{
				try (final var login = new Login())
				{
					if(0 != login.update("DELETE FROM USERS WHERE LOGIN=?", operation.getData(LOGIN)))
					{
						writer.writeStartElement(RESPONSE);
						writer.writeElement(STATUS, "0");
						writer.writeStartElement("data");
						writer.writeStartElement("record");
						writer.writeAttribute(LOGIN, operation.getData(LOGIN));
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
			failure(CAN_T_DO_THAT);
	}
	
	/**
	 * @param operation
	 * @param login
	 * @throws SQLException
	 * @throws XMLStreamException
	 */
	private void fetchSingle(Operation operation, final Login login) throws SQLException, XMLStreamException
	{
		val user = login.queryHandler("SELECT * FROM USERS WHERE LOGIN=?", new BeanHandler<UserCredential>(UserCredential.class), operation.getData(LOGIN));
		if(user != null)
			writeSingle(user);
		else
			failure();
	}

	/**
	 * @param user
	 * @throws XMLStreamException
	 */
	private void writeSingle(final jrm.fullserver.security.UserCredential user) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writeRecord(user);
		writer.writeEndElement();
		writer.writeEndElement();
	}


}
