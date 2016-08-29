package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.intergraph.cs.io.IOUtils;
import com.intergraph.cs.schema.CrowdSourcingSchema;

import com.intergraph.cs.model.User;
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

		initializeHeaders(response);
		
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

			String id = getString(data, EVENT_ID);
			UUID eventUuid;
			if (isEmptyString(id))
				eventUuid = UUID.randomUUID();
			else
				eventUuid = UUID.fromString(id);

			//  Check if event with defined UUID already exists
			try
			{
				if(eventExists(connection, eventUuid))
				{
					response.setStatus(HttpServletResponse.SC_CONFLICT);
					return;
				}
			}
			catch(Throwable ex)
			{
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			String eventDescription = getString(data, EVENT_DESCRIPTION);
			JSONArray media = getJSONArray(data, EVENT_MEDIA);

			String eventPriority = getString(data, EVENT_PRIORITY);
			if (isEmptyString(eventPriority))
				eventPriority = getPriorityDefault(connection);
				
			String eventDatetime = getString(data, EVENT_DATETIME);
			Timestamp eventTimestamp;
			if (isEmptyString(eventDatetime)) 
				eventTimestamp = new Timestamp(System.currentTimeMillis());
			else
			    eventTimestamp = parseISO8601(eventDatetime);

			String eventStatus = getStatusDefault(connection);
			checkStatus(connection, eventStatus);
			JSONArray eventTags = getJSONArray(data, EVENT_TAGS);
			checkTags(connection, eventTags);
			
			User user = verifyUser(connection, data);
			
			JSONObject location = (JSONObject)data.get(EVENT_LOCATION);
			if (location == null) {
				throw new CrowdSourcingException("Missing property '" + EVENT_LOCATION + "'");
			}
			
			double longitude = getDoubleOrThrow(location, LOCATION_LONGITUDE);
			double latitude = getDoubleOrThrow(location, LOCATION_LATITUDE);
			int srid = getSRID(getString(location, LOCATION_CRS));
			
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
			if(user == null)
				statement.setNull(3, Types.VARCHAR);
			else
			statement.setString(3, user.id);
			statement.setDouble(4, longitude);
			statement.setDouble(5, latitude);
			statement.setInt(6, srid);
			statement.setString(7, eventPriority);
			statement.setTimestamp(8, eventTimestamp);
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
				if (part == null)
					throw new CrowdSourcingException("Cannot find part '" + uri + "'");

				storeMedia(part, eventId, mimeType, connection);
			}

			connection.commit();
			
			Collection<String> emails = new HashSet<String>();
			int ntags = eventTags != null ? eventTags.size() : 0;
			for (int i = 0; i < ntags; i++) {
				String tag = (String)eventTags.get(i);
				emails.addAll(getEmails(connection, tag));
			}
			
			String subject = "New event submitted";
			String body = "New event " + eventUuid.toString() + " has been submitted";
			
			for (String email : emails) {
				sendEmail(email, subject, body); 
			}
		}
		catch (Exception e) {
			exception = e;
			e.printStackTrace();
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
		
	private void sendEmail(String to, String subject, String body) {    
		if (mailHost == null)
			return;
		
	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", mailHost);
	      properties.put("mail.smtp.port", mailPort);
	      properties.put("mail.smtp.auth", "true");
		  properties.put("mail.smtp.ssl.enable", "true");
		  properties.put("mail.smtp.startssl.enable", "true");
		  properties.put("mail.smtp.socketFactory.fallback", "true"); 
		  properties.put("mail.transport.protocol", "imap");

	      // Get the default Session object.
	      Session session = Session.getInstance(properties);

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(mailUsername));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Now set the actual message
	         message.setText(body);

	         // Send message
	         Transport.send(message, mailUsername, mailPassword);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }

}
