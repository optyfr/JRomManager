package jrm.server;

import fi.iki.elonen.NanoHTTPD.Response;

abstract class XMLResponse
{
	protected XMLRequest request;

	public XMLResponse(XMLRequest request)
	{
		this.request = request;
	}
	
	public Response processRequest() throws Exception
	{
		switch(request.operationType.toString())
		{
			case "fetch":
				return fetch();
			case "add":
				return add();
			case "update":
				return update();
			case "delete":
				return delete();
		}
		return null;
	}
	
	protected abstract Response fetch() throws Exception;
	protected abstract Response add() throws Exception;
	protected abstract Response update() throws Exception;
	protected abstract Response delete() throws Exception;

}
