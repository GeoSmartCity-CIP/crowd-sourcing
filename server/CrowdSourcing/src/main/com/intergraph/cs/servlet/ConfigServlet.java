package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;

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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		initializeHeaders(response);
		
		Exception exception = null;
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = openDatabaseConnection();
			initializeLoginRequired(connection);

			JSONObject config = new JSONObject();
			addToConfig(config, CONFIG_TAGS, getAvailableTags(connection));
			addToConfig(config, CONFIG_MIME_TYPES, getSupportedMimeTypes(connection));
			addToConfig(config, CONFIG_STATUSES, getAvailableStatuses(connection));
			addToConfig(config, CONFIG_PRIORITIES, getAvailablePriorities(connection));
			addToConfig(config, CONFIG_LOGIN_REQUIRED, isLoginRequired());

			PrintWriter writer = response.getWriter();
			config.writeJSONString(writer);
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

	private void addToConfig(JSONObject config, String key, Collection<String> values) {
			JSONArray array = new JSONArray();
			array.addAll(values);
			config.put(key, array);
	}

	private void addToConfig(JSONObject config, String key, Object value) {
			config.put(key, value);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
