package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.postgresql.jdbc4.Jdbc4Array;

import com.intergraph.cs.schema.CrowdSourcingSchema;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Servlet implementation class EventListServlet
 */
@WebServlet("/event/list")
public class EventListServlet extends CrowdSourcingServlet implements CrowdSourcingSchema {
	private static final long serialVersionUID = 1L;
       
	public static final String LIST_QUERY = "SELECT id FROM event";
	public static final String LIST_CONDITION_FROM = " datetime >= ?";
	public static final String LIST_CONDITION_TO = " datetime <= ?";
	public static final String LIST_CONDITION_PRIORITY = " priority = ?";
	public static final String LIST_CONDITION_STATUS = " status = ?";
	public static final String LIST_CONDITION_USER_ID = " \"user\" = ?";
	public static final String LIST_CONDITION_BBOX = " location @ ST_SetSRID(ST_MakeBox2D(ST_Point(?, ?), ST_Point(?, ?)), ?)";

	public static final String SELECT_EVENT = "SELECT uuid, description, \"user\", ST_X(location), ST_Y(location), ST_SRID(location), datetime, priority, status, tags FROM event WHERE id=?";
	public static final String LIST_MEDIA = "SELECT uuid, mime_type FROM media WHERE event_id=?";
	public static final String LIST_COMMENTS = "SELECT text, \"user\", datetime FROM comment WHERE event=?";

	
	
    /**
     * @see CrowdSourcingServlet#CrowdSourcingServlet()
     */
    public EventListServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		requestURL = request.getRequestURL().toString();
		servletPath = request.getServletPath();

		listEvents(null, response);
	}

	protected String requestURL;
	protected String servletPath;
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		initializeHeaders(response);
		
		requestURL = request.getRequestURL().toString();
		servletPath = request.getServletPath();
		
		Exception exception = null;
		InputStream input = null;
		
		try {
			input = request.getInputStream();
			JSONParser parser = createJSONParser();
			JSONObject object = (JSONObject) parser.parse(input);
			listEvents(object, response);
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			closeQuietly(input);
		}

		if (exception != null) {
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			exception.printStackTrace(response.getWriter());
		}
	}

	
	protected void listEvents(JSONObject data, HttpServletResponse response) 
			throws ServletException, IOException {
		
		Exception exception = null;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		Writer writer = null;
		
		try {
			connection = openDatabaseConnection();
			
			JSONObject filter = data == null ? null : getJSONObject(data, LIST_FILTER);
			String query = LIST_QUERY;
			
			ArrayList<String> conditions = new ArrayList<String>();
			ArrayList<Object> params = new ArrayList<Object>();

			JSONObject bbox = filter == null ? null : getJSONObject(filter, LIST_BBOX);
			if (bbox != null) {
				double latMin = getDoubleOrThrow(bbox, LIST_BBOX_LATITUDE_MIN);
				double lonMin = getDoubleOrThrow(bbox, LIST_BBOX_LONGITUDE_MIN);
				double latMax = getDoubleOrThrow(bbox, LIST_BBOX_LATITUDE_MAX);
				double lonMax = getDoubleOrThrow(bbox, LIST_BBOX_LONGITUDE_MAX);
				int srid = getSRID(getString(bbox, LOCATION_CRS));
				
				conditions.add(LIST_CONDITION_BBOX);
				
				params.add(latMin);
				params.add(lonMin);
				params.add(latMax);
				params.add(lonMax);
				params.add(srid);
			}

			JSONObject datetime = filter == null ? null : getJSONObject(filter, EVENT_DATETIME);
			if (datetime != null) {
				String from = getString(datetime, LIST_FROM);
				if (from != null) {
					conditions.add(LIST_CONDITION_FROM);
					params.add(parseISO8601(from));
				}

				String to = getString(datetime, LIST_TO);
				if (to != null) {
					conditions.add(LIST_CONDITION_TO);
					params.add(parseISO8601(to));
				}
				
			}
			
			JSONArray priorities = filter == null ? null : getJSONArray(filter, EVENT_PRIORITY);
			int npriorities = priorities != null ? priorities.size() : 0;
			if (npriorities > 0) {
				StringBuffer condition = new StringBuffer();
				condition.append("(");
				for (int i = 0; i < npriorities; i++) {
					condition.append(LIST_CONDITION_PRIORITY);
					if (i < npriorities-1)
						condition.append(" OR");
					params.add(priorities.get(i));
				}
				condition.append(")");
				
				conditions.add(condition.toString());
			}

			String user = filter == null ? null : getString(filter, EVENT_USER);
			if (user != null) {
				conditions.add(LIST_CONDITION_USER_ID);
				params.add(user);
			}

			int nconditions = conditions.size();
			if (nconditions > 0) {
				query += " WHERE ";
				query += conditions.get(0);
				
				for (int i = 1; i < nconditions; i++)
					query += "AND " + conditions.get(i);
			}
			
			statement = connection.prepareStatement(query);
			int nparams = params.size();
			if (nparams > 0) {
				for (int i = 0; i < nparams; i++)
					statement.setObject(i+1, params.get(i));
			}

			JSONArray events = new JSONArray();

			result = statement.executeQuery();
			while (result.next()) {
				long eventId = result.getLong(1);
				JSONObject event = fetchEvent(connection, eventId);
				events.add(event);
			}

			writer = response.getWriter();
			events.writeJSONString(writer);
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
	
	private JSONObject fetchEvent(Connection connection, long eventId)
		throws ServletException, IOException, SQLException {
		
		PreparedStatement statement = null;
		ResultSet result = null;
		
		try {
			//
			// Event
			//
			statement = connection.prepareStatement(SELECT_EVENT);
			statement.setLong(1, eventId);
			result = statement.executeQuery();
			result.next();

			Object uuid = result.getObject(1);
			String description = result.getString(2);
			String user = result.getString(3);
			double longitude = result.getDouble(4);
			double latitude = result.getDouble(5);
			int srid = result.getInt(6);
			Timestamp creation = result.getTimestamp(7);
			String priority = result.getString(8);
			String status = result.getString(9);
			Array tagsArray = result.getArray(10);
			
			result.close();
			statement.close();
			
			JSONObject event = new JSONObject();
			event.put(EVENT_ID, uuid.toString());
			event.put(EVENT_DESCRIPTION, description);
			event.put(EVENT_USER, user);
			if (creation != null)
				event.put(EVENT_DATETIME, toISO8601(new Date(creation.getTime())));
			event.put(EVENT_PRIORITY, priority);
			event.put(EVENT_STATUS, status);

			JSONObject location = new JSONObject();
			location.put(LOCATION_LATITUDE, latitude);
			location.put(LOCATION_LONGITUDE, longitude);
			location.put(LOCATION_CRS, CRS_PREFIX + srid);
			
			if (tagsArray != null) {
				try {
					JSONArray tags = new JSONArray();
					for (Object tag : (Object[])tagsArray.getArray())
						tags.add(tag);

					if (tags.size() > 0)
						event.put(EVENT_TAGS, tags);
				}
				finally {
					tagsArray.free();
				}
			}
			else
				event.put(EVENT_TAGS,  new JSONArray());

			event.put(EVENT_LOCATION, location);
			
			String mediaRequestPath = requestURL.substring(0, 
					requestURL.length()-servletPath.length());
			JSONArray media = new JSONArray();
			statement = connection.prepareStatement(LIST_MEDIA);
			statement.setLong(1, eventId);
			result = statement.executeQuery();
			while (result.next()) {
				uuid = result.getObject(1);
				if (uuid != null)
					media.add(mediaRequestPath + "/media/" + uuid.toString());
			}
			
			event.put(EVENT_MEDIA, media);

			result.close();
			statement.close();
			
			statement = connection.prepareStatement(LIST_COMMENTS);
			statement.setLong(1, eventId);
			result = statement.executeQuery();
			JSONArray comments = new JSONArray();
			while (result.next()) {
				String text = result.getString(1);
				user = result.getString(2);
				Timestamp timestamp = result.getTimestamp(3);
				
				JSONObject comment = new JSONObject();
				if (user != null)
					comment.put(COMMENT_USER, user);
				comment.put(COMMENT_TEXT, text);
				comment.put(COMMENT_DATETIME, toISO8601(new Date(timestamp.getTime())));
				
				comments.add(comment);
			}
			
			if (!comments.isEmpty())
				event.put(EVENT_COMMENTS, comments);
			return event;
		}
		finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}

}
