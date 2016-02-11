package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intergraph.cs.io.IOUtils;

@WebServlet("/media/*")
public class MediaGetServlet extends CrowdSourcingServlet {
	private static final long serialVersionUID = 8290610281380922366L;
	
	
	public static final String GET_DATA = "SELECT data, mime_type FROM media WHERE uuid=?";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		initializerHelper(response);
		
		URI uri = URI.create(request.getRequestURI());
		String id = request.getPathInfo();
		if (id != null && id.startsWith("/")) {
			id = id.substring(1);
		}

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		
		InputStream blobInput = null;
		OutputStream responseOutput =  null;
		Exception exception = null;
		try {
			UUID uuid = UUID.fromString(id);
			connection = openDatabaseConnection();
			statement = connection.prepareStatement(GET_DATA);
			statement.setObject(1, uuid);
			result = statement.executeQuery();
			if (!result.next()) {
				PrintWriter writer = response.getWriter();
				writer.println("Event '" + id + "' not found");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			blobInput = result.getBinaryStream(1);
			responseOutput = response.getOutputStream();
			IOUtils.copy(blobInput, responseOutput);
		}
		catch (IOException e) {
			exception = e;
		}
		catch (SQLException e) {
			exception = e;
		}
		finally {
			IOUtils.closeSilently(blobInput);
			IOUtils.closeSilently(responseOutput);
			IOUtils.closeSilently(connection);
			IOUtils.closeSilently(statement);
			IOUtils.closeSilently(result);
		}
		
		if (exception != null) {
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			exception.printStackTrace(response.getWriter());
		}
	}
}
