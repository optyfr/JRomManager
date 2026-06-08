package jrm.server.handlers;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jrm.misc.Log;

/** Servlet to handle session-related requests. This servlet is used to create a new session and return the session ID to the client. The session ID is stored in a cookie, so the client can use it for subsequent requests. */
@SuppressWarnings("serial")
public class SessionServlet extends AbstractSessionServlet {
    @Override
    /** Handles POST requests to create a new session and return the session ID to the client. The session ID is stored in a cookie, so the client can use it for subsequent requests. The response is a JSON object containing the session ID. If an error occurs, a 500 Internal Server Error status is returned.
     * @param req the HttpServletRequest object that contains the request the client made to the servlet
     * @param resp the HttpServletResponse object that contains the response the servlet returns to the client
     * @throws ServletException if the request could not be handled
     * @throws IOException if an I/O error occurs while handling the request
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            fillAndSendJSO(req, resp, new JsonObject());
        } catch (Exception e) {
            Log.err(e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
