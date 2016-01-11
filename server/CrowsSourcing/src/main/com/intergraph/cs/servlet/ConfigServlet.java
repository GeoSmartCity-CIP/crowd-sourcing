package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intergraph.cs.schema.CrowdSourcingSchema;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Servlet implementation class ConfigServlet
 */
@WebServlet("/config")
public class ConfigServlet extends CrowdSourcingServlet implements CrowdSourcingSchema {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public ConfigServlet() {
        super();
    }

    public static final String QUERY_GET_TAGS = "SELECT value FROM tag";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		Exception exception = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
		
		try {
			JSONObject config = new JSONObject();
			
			JSONArray tags = new JSONArray();
			
			connection = openDatabaseConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(QUERY_GET_TAGS);
			while (result.next()) {
				tags.add(result.getString(1));
			}
			
			if (tags.size() > 0)
				config.put(CONFIG_TAGS, tags);

			JSONArray mediaTypes = new JSONArray();
			mediaTypes.add("image/jpeg");
			mediaTypes.add("image/png");
			config.put(CONFIG_MIME_TYPES, mediaTypes);		

			JSONArray statuses = new JSONArray();
			statuses.add("new");
			statuses.add("assigned");
			statuses.add("approved");
			statuses.add("closed");
			config.put(CONFIG_STATUSES, statuses);		
			
			PrintWriter writer = response.getWriter();
			config.writeJSONString(writer);
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			closeQuietly(result);
			closeQuietly(statement);
			closeQuietly(connection);
		}

		if (exception != null) {
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			exception.printStackTrace(response.getWriter());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
