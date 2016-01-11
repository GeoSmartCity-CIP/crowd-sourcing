package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.intergraph.cs.io.IOUtils;
import com.intergraph.cs.schema.CrowdSourcingSchema;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;


@WebServlet("/event/create")
public class EventCreateServlet extends CrowdSourcingServlet implements CrowdSourcingSchema {
	private static final long serialVersionUID = 7786625912220958857L;
	
//	private static final String INSERT_LOCATION = "INSERT INTO location (longitude, latitude, crs) VALUES (?, ?, ?) RETURNING id";
	private static final String INSERT_USER = "INSERT INTO \"user\" (id, email, role, organization) VALUES (?, ?, ?, ?)";
	private static final String SELECT_USER = "SELECT id FROM \"user\" WHERE id=?";
	private static final String INSERT_EVENT = "INSERT INTO event (uuid, description, \"user\", location, \"priority\", datetime, status, tags) VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), ?), ?, ?, ?, ?) RETURNING id";


	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		Exception exception = null;
		InputStream jsonInput = null;
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		
		try {
			if (!isMultipart(request))
				throw new CrowdSourcingException("Multipart request is expected");

			Collection<Part> parts = request.getParts();
			Iterator<Part> iterator = parts.iterator();
			if (!iterator.hasNext())
				throw new CrowdSourcingException("Multipart expected");
			
			Part jsonPart = iterator.next();
			jsonInput = jsonPart.getInputStream();

			connection = openDatabaseConnection();
			connection.setAutoCommit(false);

			JSONParser parser = createJSONParser();
			JSONObject data = (JSONObject)parser.parse(jsonInput);

			String id = getStringOrThrow(data, EVENT_ID);
			UUID eventUuid = UUID.fromString(id);
			String eventDescription = getString(data, EVENT_DESCRIPTION);
			JSONArray media = getJSONArray(data, EVENT_MEDIA);
			String eventPriority = getString(data, EVENT_PRIORITY);
			String eventDatetime = getString(data, EVENT_DATETIME);
			String eventStatus = getString(data, EVENT_STATUS);
			checkStatus(connection, eventStatus);
			JSONArray eventTags = getJSONArray(data, EVENT_TAGS);
			checkTags(connection, eventTags);
			
			String userId = null, userEmail = null, userRole = null, userOrganization = null;
			JSONObject user = getJSONObject(data, EVENT_USER);
			if (user != null) {
				userId = getString(user, USER_ID);
				userEmail = getString(user, USER_EMAIL);
				userRole = getString(user, USER_ROLE);
				userOrganization = getString(user, USER_ORGANIZATION);
			}
			
			JSONObject location = (JSONObject)data.get(EVENT_LOCATION);
			if (location == null) {
				throw new CrowdSourcingException("Missing property '" + EVENT_LOCATION + "'");
			}
			
			double longitude = getDoubleOrThrow(location, LOCATION_LONGITUDE);
			double latitude = getDoubleOrThrow(location, LOCATION_LATITUDE);
			int srid = getSRID(getString(location, LOCATION_CRS));
			
			statement = connection.prepareStatement(SELECT_USER);
			statement.setString(1, userId);
			result = statement.executeQuery();
			boolean userExists = result.next();
			result.close();
			statement.close();

			if (!userExists) {
				statement = connection.prepareStatement(INSERT_USER);
				statement.setString(1, userId);
				statement.setString(2, userEmail);
				statement.setString(3, userRole);
				statement.setString(4, userOrganization);
				statement.execute();
				statement.close();
			}
					
			Array tagsArray = null;
			if (eventTags != null && !eventTags.isEmpty()) {
				int ntags = eventTags.size();
				String[] tags = new String[ntags];
				for (int i = 0; i < ntags; i++)
					tags[i] = (String)eventTags.get(i);

				tagsArray = connection.createArrayOf("text", tags);
			}

			statement = connection.prepareStatement(INSERT_EVENT);
			statement.setObject(1, eventUuid);
			statement.setString(2, eventDescription);
			statement.setString(3, userId);
			statement.setDouble(4, latitude);
			statement.setDouble(5, longitude);
			statement.setInt(6, srid);
			statement.setString(7, eventPriority);
			statement.setTimestamp(8, parseISO8601(eventDatetime));
			statement.setString(9, eventStatus);
			statement.setArray(10, tagsArray);
			result = statement.executeQuery();
			result.next();
			long eventId = result.getLong(1);
			result.close();
			statement.close();

			int nmedia = media.size();
			for (int i = 0; i < nmedia; i++) {
				JSONObject object = (JSONObject)media.get(i);
				String mimeType = getString(object, EVENT_MIME_TYPE);
				checkMimeType(connection, mimeType);

				String uri = getString(object, EVENT_MEDIA_URI);
				Part part = getPartByName(parts, uri);
				storeMedia(part, eventId, mimeType, connection);
			}

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
		

}
