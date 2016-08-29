package com.intergraph.cs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.sql.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.intergraph.cs.io.IOUtils;
import com.intergraph.cs.schema.CrowdSourcingSchema;

import com.intergraph.cs.model.User;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * Servlet implementation class CrowdSourcing
 */
public abstract class CrowdSourcingServlet extends HttpServlet implements CrowdSourcingSchema {
	private static final long serialVersionUID = 1L;

	public static final String PARAM_DATABASE_DRIVER = "db-driver";
	public static final String PARAM_DATABASE_URL = "db-url";
	public static final String PARAM_DATABASE_USER = "db-user";
	public static final String PARAM_DATABASE_PASSWORD = "db-password";

	public static final String PARAM_MAIL_HOST = "mail-host";
	public static final String PARAM_MAIL_PORT = "mail-port";
	public static final String PARAM_MAIL_USERNAME = "mail-username";
	public static final String PARAM_MAIL_PASSWORD = "mail-password";

	public static final String PARAM_STATUS_DEFAULT = PROPERTY_STATUS_DEFAULT;
	public static final String PARAM_ROLE_DEFAULT = PROPERTY_ROLE_DEFAULT;
	public static final String PARAM_PRIORITY_DEFAULT = PROPERTY_PRIORITY_DEFAULT;

	public static final String DATABASE_DRIVER = "org.postgresql.Driver";
	public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/cs";
	public static final String DATABASE_USER = "rszturc";
	public static final String DATABASE_PASSWORD = "rszturc";

	public static final String HEADER_MULTIPART_PREFIX = "multipart";

	public static final String CRS_PREFIX = "epsg:";
	public static final int SRID_DEFAULT = 4326;

	protected String dbDriver = DATABASE_DRIVER;
	protected String dbUrl = DATABASE_URL;
	protected String dbUser = DATABASE_USER;
	protected String dbPassword = DATABASE_PASSWORD;

	protected String mailHost;
	protected String mailPort;
	protected String mailUsername;
	protected String mailPassword;
	
	protected String statusDefault;
	protected String roleDefault;
	protected String priorityDefault;

	public CrowdSourcingServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ServletContext context = config.getServletContext();
		String value = context.getInitParameter(PARAM_DATABASE_DRIVER);
		if (value != null)
			dbDriver = value;

		value = context.getInitParameter(PARAM_DATABASE_URL);
		if (value != null)
			dbUrl = value;

		value = context.getInitParameter(PARAM_DATABASE_USER);
		if (value != null)
			dbUser = value;

		value = context.getInitParameter(PARAM_DATABASE_PASSWORD);
		if (value != null)
			dbPassword = value;

		value = context.getInitParameter(PARAM_MAIL_HOST);
		if (value != null)
			mailHost = value;

		value = context.getInitParameter(PARAM_MAIL_PORT);
		if (value != null)
			mailPort = value;

		value = context.getInitParameter(PARAM_MAIL_USERNAME);
		if (value != null)
			mailUsername = value;

		value = context.getInitParameter(PARAM_MAIL_PASSWORD);
		if (value != null)
			mailPassword = value;

		statusDefault = context.getInitParameter(PARAM_STATUS_DEFAULT);
		roleDefault = context.getInitParameter(PARAM_ROLE_DEFAULT);
		priorityDefault = context.getInitParameter(PARAM_PRIORITY_DEFAULT);
	}

	protected void initializeHeaders(HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
	}		

	protected Connection openDatabaseConnection() throws SQLException {
		try {
			Class.forName(dbDriver);
			return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Cannot load driver '" + dbDriver + "'", e);
		}
	}

	protected boolean isMultipart(HttpServletRequest request) {
		final String contentType = request.getContentType();
		return contentType != null && contentType.startsWith(HEADER_MULTIPART_PREFIX);
	}

	public static final String INSERT_DATA = "INSERT INTO media (event_id, mime_type, data, uuid) VALUES (?, ?, ?, ?)";
	public static final String SELECT_USER = "SELECT id, password, email, organization, role FROM \"user\" WHERE id=?";

	protected void storeMedia(Part part, long eventId, String mediaType, Connection connection)
			throws SQLException, IOException {

		InputStream input = part.getInputStream();
		PreparedStatement statement = null;

		try {
			statement = connection.prepareStatement(INSERT_DATA);
			statement.setLong(1, eventId);
			statement.setString(2, mediaType);
			statement.setBinaryStream(3, input);
			statement.setObject(4, UUID.randomUUID());
			statement.executeUpdate();
		} finally {
			IOUtils.closeSilently(statement);
			IOUtils.closeSilently(input);
		}
	}

	protected void closeQuietly(AutoCloseable closeable) {
		IOUtils.closeSilently(closeable);
	}

	protected Part getPartByName(Collection<Part> parts, String name) {
		for (Part part : parts)
			if (name.equals(part.getName()))
				return part;

		return null;
	}

	protected String getString(JSONObject object, String key) throws MissingArgumentException {
		if (object == null)
			return null;
		
		Object value = object.get(key);
		if (value == null)
			return null;

		if (!(value instanceof String))
			throw new MissingArgumentException("Value of property '" + key + "' should be of type String instead of '"
					+ value.getClass().getSimpleName() + "'");

		return (String) value;
	}

	protected JSONArray getJSONArray(JSONObject object, String key) throws MissingArgumentException {
		Object value = object.get(key);
		if (value == null)
			return null;

		if (!(value instanceof JSONArray))
			throw new MissingArgumentException("Value of property '" + key
					+ "' should be of type JSONArray instead of '" + value.getClass().getSimpleName() + "'");

		return (JSONArray) value;
	}

	protected String getStringOrThrow(JSONObject object, String key) throws MissingArgumentException {
		String value = getString(object, key);
		if (value == null)
			throw new MissingArgumentException("Missing property '" + key + "'");

		return (String) value;
	}

	protected double getDoubleOrThrow(JSONObject object, String key)
			throws MissingArgumentException, NumberFormatException {
		Object value = object.get(key);
		if (value == null)
			throw new MissingArgumentException("Missing property '" + key + "'");

		if (!(value instanceof Number))
			throw new MissingArgumentException("Value of property '" + key + "' should be of type Number instead of '"
					+ value.getClass().getSimpleName() + "'");
		return ((Number) value).doubleValue();
	}

	protected JSONObject getJSONObjectOrThrow(JSONObject object, String key) throws MissingArgumentException {

		JSONObject value = getJSONObject(object, key);
		if (value == null)
			throw new MissingArgumentException("Missing property '" + key + "'");

		return value;
	}

	protected JSONObject getJSONObject(JSONObject object, String key) throws MissingArgumentException {
		Object value = object.get(key);
		if (value == null)
			return null;

		if (!(value instanceof JSONObject))
			throw new MissingArgumentException("Value of property '" + key
					+ "' should be of type JSONObject instead of '" + value.getClass().getSimpleName() + "'");

		return (JSONObject) value;
	}

	protected JSONParser createJSONParser() {
		return new JSONParser(JSONParser.ACCEPT_SIMPLE_QUOTE | JSONParser.ACCEPT_LEADING_ZERO);
	}

	protected Timestamp getTimestamp(JSONObject object, String key) throws ParseException, MissingArgumentException {
		String date = getStringOrThrow(object, key);
		return new Timestamp(ISO8601DATEFORMAT.parse(date).getTime());
	}

	protected JSONObject getRequestObject(HttpServletRequest request)
			throws IOException, net.minidev.json.parser.ParseException {
		InputStream input = null;
		try {
			input = request.getInputStream();
			JSONParser parser = createJSONParser();
			JSONObject object = (JSONObject) parser.parse(input);

			return object;
		} finally {
			closeQuietly(input);
		}
	}

	public static final SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected Timestamp parseISO8601(String date) throws ParseException {
		return new Timestamp(ISO8601DATEFORMAT.parse(date).getTime());
	}

	protected String toISO8601(Date date) {
		return ISO8601DATEFORMAT.format(date);
	}

	protected int getSRID(String crs) {
		if (crs == null)
			return SRID_DEFAULT;

		crs = crs.toLowerCase();
		if (crs.startsWith(CRS_PREFIX))
			crs = crs.substring(CRS_PREFIX.length());

		return Integer.parseInt(crs);
	}

	protected UUID getRequestUUID(HttpServletRequest request) {
		String id = request.getPathInfo();
		if (id != null && id.startsWith("/")) {
			id = id.substring(1);
		}

		return UUID.fromString(id);
	}

	public static final String QUERY_SELECT_PROPERTY = "SELECT value FROM property WHERE key=?";

	public static final String QUERY_SELECT_USER = "SELECT * FROM \"user\" WHERE id=?";
	public static final String QUERY_SELECT_USER_COUNT = "SELECT count(*) FROM \"user\" WHERE id=?";
	public static final String QUERY_INSERT_USER = "INSERT INTO \"user\" (id, email, role, organization) VALUES (?, ?, ?, ?)";
	public static final String QUERY_SELECT_EVENT = "SELECT * FROM \"event\" WHERE uuid=?";

//	protected String getUserId(Connection connection, JSONObject object)
//			throws MissingArgumentException, SQLException, UserAlreadyExists {
//
//		JSONObject user = getJSONObjectOrThrow(object, EVENT_USER);
//		String id = getString(user, USER_ID);
//		String email = getString(user, USER_EMAIL);
//		String role = getString(user, USER_ROLE);
//		String organization = getString(user, USER_ORGANIZATION);
//
//		PreparedStatement statement = null;
//		ResultSet result = null;
//		try {
//			statement = connection.prepareStatement(QUERY_SELECT_USER_COUNT);
//			statement.setString(1, id);
//			result = statement.executeQuery();
//			result.next();
//			int count = result.getInt(1);
//
//			if (count > 0) {
//				if (email != null || role != null || organization != null)
//					throw new UserAlreadyExists("User '" + id + "' already exists");
//
//				return id;
//			}
//
//			closeQuietly(statement);
//			statement = connection.prepareStatement(QUERY_INSERT_USER);
//			statement.setString(1, id);
//			statement.setString(2, email);
//			statement.setString(3, role);
//			statement.setString(4, organization);
//			statement.executeUpdate();
//
//			return id;
//		} finally {
//			closeQuietly(result);
//			closeQuietly(statement);
//		}
//	}

	public static final String QUERY_SELECT_TAGS = "SELECT tag FROM tag";
	public static final String QUERY_SELECT_MIME_TYPES = "SELECT mime_type FROM mime_type";
	public static final String QUERY_SELECT_STATUSES = "SELECT status FROM status";
	public static final String QUERY_SELECT_PRIORITIES = "SELECT priority FROM priority";
	public static final String QUERY_SELECT_EMAILS = "SELECT email FROM notification JOIN tag ON tag.id = notification.tag_id WHERE tag.tag = ?";

	protected Collection<String> getEmails(Connection connection, String tag) throws SQLException {
		PreparedStatement statement = null;
		ResultSet result = null;
		ArrayList<String> tags = new ArrayList<String>();

		try {
			statement = connection.prepareStatement(QUERY_SELECT_EMAILS);
			statement.setString(1, tag);
			result = statement.executeQuery();
			while (result.next())
				tags.add(result.getString(1));

			return tags;
		} finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}
	
	protected Collection<String> getAvailableTags(Connection connection) throws SQLException {
		return getFirstColumn(connection, QUERY_SELECT_TAGS);
	}

	protected Collection<String> getSupportedMimeTypes(Connection connection) throws SQLException {
		return getFirstColumn(connection, QUERY_SELECT_MIME_TYPES);
	}

	protected Collection<String> getAvailableStatuses(Connection connection) throws SQLException {
		return getFirstColumn(connection, QUERY_SELECT_STATUSES);
	}

	protected Collection<String> getAvailablePriorities(Connection connection) throws SQLException {
		return getFirstColumn(connection, QUERY_SELECT_PRIORITIES);
	}

	protected Collection<String> getFirstColumn(Connection connection, String query) throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		ArrayList<String> tags = new ArrayList<String>();

		try {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			while (result.next())
				tags.add(result.getString(1));

			return tags;
		} finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}
	
	protected void checkMimeType(Connection connection, String mimeType) 
			throws UnsupportedValueException, SQLException {
		if (mimeType == null)
			return;
		
		Collection<String> mimeTypes = getSupportedMimeTypes(connection);
		if (!mimeTypes.contains(mimeType))
			throw new UnsupportedValueException("Unsupported mime-type '" +
					mimeType + "'");
	}

	protected void checkTags(Connection connection, JSONArray jsonTags) 
			throws UnsupportedValueException, SQLException {
		if (jsonTags == null || jsonTags.isEmpty())
			return;
		
		Collection<String> tags = getAvailableTags(connection);
		int size = jsonTags.size();
		for (int i = 0; i < size; i++) {
			String tag = (String)jsonTags.get(i);
			if (!tags.contains(tag))
				throw new UnsupportedValueException("Unsupported tag '" +
						tag + "'");
		}
	}

	protected void checkStatus(Connection connection, String status) 
			throws UnsupportedValueException, SQLException {
		if (status == null)
			return;
		
		Collection<String> statuses = getAvailableStatuses(connection);
		if (!statuses.contains(status))
			throw new UnsupportedValueException("Unsupported status '" +
					status + "'");
	}

	protected void checkPriority(Connection connection, String priority) 
			throws UnsupportedValueException, SQLException {
		if (priority == null)
			return;
		
		Collection<String> priorities = getAvailablePriorities(connection);
		if (!priorities.contains(priority))
			throw new UnsupportedValueException("Unsupported priority '" +
					priority + "'");
	}

	protected User getUser(Connection connection, JSONObject data) 
			throws CrowdSourcingException, SQLException {
		
		// Initialize login-required status
		initializeLoginRequired(connection);
		
		if (data == null)
			return null;
		
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			JSONObject user = getJSONObject(data, EVENT_USER);
			if (user == null)
				return null;
			
			String id = getString(user, USER_ID);
			if (id == null)
				return null;

			statement = connection.prepareStatement(SELECT_USER);
			statement.setString(1, id);
			result = statement.executeQuery();
			if (!result.next()) 
				return null;
			
			int i = 2;
			User u = new User();
			u.id = id;
			u.password = result.getString(i++);
			u.email = result.getString(i++);
			u.organization = result.getString(i++);
			u.roles = new String[] {result.getString(i++)};
			
			return u;
		} 
		finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
		
	}

	protected void checkUserIdAvailability(Connection connection, String userId) 
			throws CrowdSourcingException, SQLException {
		if (userId == null)
			throw new CrowdSourcingException("Missing user identification");
		
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(QUERY_SELECT_USER);
			statement.setString(1, userId);
			result = statement.executeQuery();
			if (result.next()) 
				throw new CrowdSourcingException("User '" + userId + "' already exists");

		} 
		finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}
	
//	protected void verifyUser_Old(Connection connection, JSONObject data) 
//			throws CrowdSourcingException, SQLException {
//
//		String id = null, password = null;
//		JSONObject user = getJSONObject(data, EVENT_USER);
//		if (user != null) {
//			id = getString(user, USER_ID);
//			password = getString(user, USER_PASSWORD);
//		}
//		
//		verifyUser(connection, id, password);
//	}

//	protected void verifyUser(Connection connection, String user, String password) 
//			throws CrowdSourcingException, SQLException {
//
//		if (!isLoginRequired())
//			return;
//		
//		checkUser(connection, user, password);
//	}

//	protected void verifyRole(Connection connection, JSONObject data)
//			throws CrowdSourcingException, SQLException {
//
//		String id = null, password = null;
//		JSONObject user = getJSONObject(data, EVENT_USER);
//		if (user != null) {
//			id = getString(user, USER_ID);
//			password = getString(user, USER_PASSWORD);
//		}
//		
//		checkUser(connection, id, password);
//		
//	}

	protected User verifyUser(Connection connection, JSONObject object) 
			throws CrowdSourcingException, SQLException {

		User user = getUser(connection, object);
		verifyUser(user, object);
		
		return user;
	}


	protected void verifyUser(User user, JSONObject object) 
			throws PermissionException, MissingArgumentException {
		if (!isLoginRequired())
			return;

		JSONObject u = getJSONObject(object, EVENT_USER);
		String userId = getString(u, USER_ID);
		String password = getString(u, USER_PASSWORD);

		verifyUser(user, userId, password);
	}

	protected void verifyUser(User user, String userId, String password) 
			throws PermissionException {
		if (!isLoginRequired())
			return;
		
		if (user == null)
			throw new PermissionException("Invalid username or password");

		if (isEmptyString(userId) || isEmptyString(password))
			throw new PermissionException("Invalid username or password");
		
		if (!userId.equals(user.id))
			throw new PermissionException("Invalid username or password");
		
		if (!user.password.equals(passwordAsHexString(password)))
			throw new PermissionException("Invalid username or password");
	}

	protected void verifyRole(User user, String role) throws PermissionException {
		if (user == null)
			throw new PermissionException("Login required");

		for (String r : user.roles) 
			if (role.equalsIgnoreCase(r))
				return;
		
		throw new PermissionException("Role '" + role + "' required to perform this action");
	}

	protected boolean eventExists(final Connection connection, final UUID uuid) throws SQLException
	{
		if (uuid == null)
			return false;

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(QUERY_SELECT_EVENT);
			statement.setObject(1, uuid);
			result = statement.executeQuery();
			return result.next();

		}
		finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}

	/**
	 * Converts user password to SHA-256 hex string.
	 * 
	 * @param password The password.
	 * @return SHA-256 as a hex string.
	 */
	protected String passwordAsHexString(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
	}

	protected String getProperty(Connection connection, String key) throws SQLException {
		return getProperty(connection, key, null);
	}

	protected String getProperty(Connection connection, String key, String defValue) throws SQLException {
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(QUERY_SELECT_PROPERTY);
			statement.setString(1, key);
			result = statement.executeQuery();
			return result.next() ? result.getString(1) : defValue;
		} finally {
			closeQuietly(result);
			closeQuietly(statement);
		}
	}

	protected static boolean loginRequired;
	protected static boolean loginRequiredInitialized;

	protected boolean isLoginRequired() {
		return loginRequiredInitialized ? loginRequired : false;
	}

	protected void initializeLoginRequired(Connection connection) throws SQLException {
		if (!loginRequiredInitialized) {
			String required = getProperty(connection, PROPERTY_LOGIN_REQUIRED);
			loginRequired = Boolean.parseBoolean(required);
			loginRequiredInitialized = true;
		}
	}
	
	protected String getStatusDefault(Connection connection) throws SQLException {
		return getProperty(connection, PROPERTY_STATUS_DEFAULT, statusDefault);
	}

	protected String getRoleDefault(Connection connection) throws SQLException {
		return getProperty(connection, PROPERTY_ROLE_DEFAULT, roleDefault);
	}

	protected String getPriorityDefault(Connection connection) throws SQLException {
		return getProperty(connection, PROPERTY_PRIORITY_DEFAULT, priorityDefault);
	}
	
	static protected boolean isEmptyString(String string) {
		return string == null || string.trim().isEmpty();
	}
}
