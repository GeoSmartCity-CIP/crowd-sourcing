package com.intergraph.cs.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.intergraph.cs.io.IOUtils;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet("/media/upload")
public class MediaUploadServlet extends CrowdSourcingServlet {
	private static final long serialVersionUID = 1L;

	public static final String PART_NAME_JSON = "json";
	public static final String PART_NAME_DATA = "data";
	
       
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public MediaUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		initializeHeaders(response);

		Collection<Part> parts = request.getParts();
		Iterator<Part> iterator = parts.iterator();
		while (iterator.hasNext()) {
			String id = null;
			String mediaType = null;
			Part jsonPart = iterator.next();
			String name = jsonPart.getName();
			if (name == null || !name.startsWith(PART_NAME_JSON)) {
				PrintWriter writer = response.getWriter();
				writer.println("Part with key '" + PART_NAME_JSON + "' expected");
				response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
				return;
			}

			InputStream jsonStream = null;
			InputStream dataStream = null;
			OutputStream fileStream = null;
			Connection connection = null;
			PreparedStatement statement = null;
			
			Exception exception = null;
			try {
				jsonStream = jsonPart.getInputStream();
				String json = IOUtils.streamToString(jsonStream);
			
				JSONParser parser = new JSONParser(JSONParser.ACCEPT_SIMPLE_QUOTE | 
						JSONParser.ACCEPT_LEADING_ZERO);
				JSONObject object = (JSONObject)parser.parse(json);
				id = (String)object.get("id");
				mediaType = (String)object.get("mime_type");

				Part dataPart = iterator.next(); 
				dataStream = dataPart.getInputStream();
				
				UUID uuid = UUID.fromString(id);
				connection = openDatabaseConnection();
				statement = connection.prepareStatement(INSERT_DATA);
				statement.setObject(1, uuid);
				statement.setString(2, mediaType);
				statement.setBinaryStream(3, dataStream);
				statement.executeUpdate();
			}
			catch (IOException e) {
				exception = e;
			}
			catch (ParseException e) {
				exception = e;
			}
			catch (SQLException e) {
				exception = e;
			}
			finally {
				IOUtils.closeSilently(jsonStream);
				IOUtils.closeSilently(dataStream);
				IOUtils.closeSilently(fileStream);
				IOUtils.closeSilently(connection);
				IOUtils.closeSilently(statement);
			}
			
			if (exception != null) {
				response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
				exception.printStackTrace(response.getWriter());
			}
		}
	}
}
