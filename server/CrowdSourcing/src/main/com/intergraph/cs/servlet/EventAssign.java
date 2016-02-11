package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Servlet implementation class EventComment
 */
@WebServlet("/event/assign/*")
public class EventAssign extends CrowdSourcingServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public EventAssign() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	public static final String QUERY_INSERT_COMMENT = "INSERT INTO comment (event, text, \"user\", datetime) VALUES ((SELECT id FROM event WHERE uuid=?), ?, ?, ?)";
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		initializerHelper(response);

		Exception exception = null;
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = openDatabaseConnection();

			UUID uuid = getRequestUUID(request);
			JSONObject object = getRequestObject(request);
			String user = getUserId(connection, object);
			String text = getString(object, COMMENT_TEXT);
			Timestamp timestamp = getTimestamp(object, COMMENT_DATETIME);
			
			statement = connection.prepareStatement(QUERY_INSERT_COMMENT);
			statement.setObject(1, uuid);
			statement.setString(2, text);
			statement.setString(3, user);
			statement.setTimestamp(4, timestamp);
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
