package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intergraph.cs.io.IOUtils;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Servlet implementation class UserCreateServlet
 */
@WebServlet("/user/register")
public class UserRegisterServlet extends CrowdSourcingServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String INSERT_USER = "INSERT INTO \"user\" (id, email, organization, password) VALUES (?, ?, ?, ?, ?)";
	
	/**
	 * Minimum user id length.
	 */
	public static final int USER_ID_LENGTH_MIN = 3;
	
	/**
	 * Defines user id form.
	 */
	public static final String USER_ID_REGEX = "([a-z]|[0-9]){" + USER_ID_LENGTH_MIN + ",}";
	
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public UserRegisterServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Determines whether given identifier has required form.
	 * 
	 * @param userId The user identifier.
	 * @throws CrowdSourcingException If the identifier is invalid.
	 */
	protected void checkUserIdForm(String userId) throws CrowdSourcingException {
		if (userId == null)
			throw new CrowdSourcingException("Missing '" + USER_ID + "'");
		
		if (userId.matches(Pattern.quote(USER_ID_REGEX)))
			throw new CrowdSourcingException("'" + userId + "' doesn't match '" + USER_ID_REGEX + "'");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initializeHeaders(response);
		
		Exception exception = null;
		InputStream jsonInput = null;
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = openDatabaseConnection();
			connection.setAutoCommit(false);

			jsonInput = request.getInputStream();
			JSONParser parser = createJSONParser();
			JSONObject user = (JSONObject)parser.parse(jsonInput);
			if (user == null) 
				throw new CrowdSourcingException("Missing user creation data");

			String userId = getString(user, USER_ID);
			// Throws exception if userId already exists
			checkUserIdAvailability(connection, userId);
			
			// Check if userId has valid form
			checkUserIdForm(userId);

			String userEmail = getString(user, USER_EMAIL);
			String userOrganization = getString(user, USER_ORGANIZATION);
			String userPassword = getString(user, USER_PASSWORD);

			checkItem(userEmail, USER_EMAIL);
			checkItem(userOrganization, USER_ORGANIZATION);
			checkItem(userPassword, USER_PASSWORD);

			statement = connection.prepareStatement(INSERT_USER);
			statement.setString(1, userId);
			statement.setString(2, userEmail);
			statement.setString(4, userOrganization);
			statement.setString(5, passwordAsHexString(userPassword));
			statement.execute();
			statement.close();

			connection.commit();
		}
		catch (Exception e) {
			exception = e;
			try {
				if (connection != null)
					connection.rollback();
			}
			catch (SQLException x) {
				// Do nothing...
			}
		}
		finally {
			IOUtils.closeSilently(jsonInput);
			IOUtils.closeSilently(statement);
			IOUtils.closeSilently(connection);
		}

		if (exception != null) {
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			exception.printStackTrace(response.getWriter());
		}
	}

	private void checkItem(String item, String itemName) throws CrowdSourcingException {
		if (item == null || item.trim().isEmpty())
			throw new CrowdSourcingException("Missing item '" + itemName + "'");
	}
}
