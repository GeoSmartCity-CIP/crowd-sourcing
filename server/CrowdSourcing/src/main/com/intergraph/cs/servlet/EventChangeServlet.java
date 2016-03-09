package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.User;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Servlet implementation class EventComment
 */
@WebServlet("/event/change/*")
public class EventChangeServlet extends CrowdSourcingServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public EventChangeServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	public static final String QUERY_UPDATE_EVENT_1 = "UPDATE event SET ";
	public static final String QUERY_UPDATE_EVENT_2 = " WHERE uuid=?";

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		initializeHeaders(response);

		Exception exception = null;
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = openDatabaseConnection();

//			UUID uuid = getRequestUUID(request);
			JSONObject object = getRequestObject(request);
			User user = getUser(connection, object);
			verifyUser(user, object);
			verifyRole(user, ROLE_ADMIN);

			String eventId = getString(object, EVENT_ID);
			if (isEmptyString(eventId))
				throw new MissingArgumentException("Missing event identification");

			List<String> parameters = new ArrayList<>();
			List<String> values = new ArrayList<>();

			String priority = getString(object, EVENT_PRIORITY);
			if (priority != null) {
				parameters.add(EVENT_PRIORITY);
				values.add(priority);
			}
			
			String status = getString(object, EVENT_STATUS);
			if (status != null) {
				parameters.add(EVENT_STATUS);
				values.add(status);
			}
			
			int size = parameters.size();
			if (size == 0)
				return;
			
			StringBuilder update = new StringBuilder(QUERY_UPDATE_EVENT_1);
			for (int i = 0; i < size; i++) {
				update.append(parameters.get(i));
				update.append("=?");
				if (i < size-1)
					update.append(",");
			}
			update.append(QUERY_UPDATE_EVENT_2);
			
			statement = connection.prepareStatement(update.toString());
			for (int i = 0; i < size; i++) 
				statement.setString(i+1, values.get(i));

			statement.setObject(size+1,  UUID.fromString(eventId));
			statement.executeUpdate();
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			closeQuietly(statement);
			closeQuietly(connection);
		}

		if (exception != null) {
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			exception.printStackTrace(response.getWriter());
		}
	}

}
