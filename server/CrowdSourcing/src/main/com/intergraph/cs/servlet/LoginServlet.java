package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intergraph.cs.model.User;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginServlet extends CrowdSourcingServlet implements Servlet {

    /**
     * Default constructor. 
     */
    public LoginServlet() {
    	super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		initializeHeaders(response);
		
		Exception exception = null;
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = openDatabaseConnection();

			JSONObject object = getRequestObject(request);
			User user = verifyUser(connection, object);
			
			JSONObject userProperties = new JSONObject();
			userProperties.put(USER_ID, user.id);
			userProperties.put(USER_EMAIL, user.email);
			userProperties.put(USER_ORGANIZATION, user.organization);
			
			JSONArray roles = new JSONArray();
			for (String role : user.roles)
				roles.add(role);
			userProperties.put(USER_ROLE, roles);
			
			JSONObject result = new JSONObject();
			result.put(USER, userProperties);
			
			PrintWriter writer = response.getWriter();
			result.writeJSONString(writer);
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
