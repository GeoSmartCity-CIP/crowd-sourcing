package com.intergraph.cs.schema;

public interface CrowdSourcingSchema {
	String USER = "user";

	String EVENT_ID = "id";
	String EVENT_DESCRIPTION = "description";
	String EVENT_MEDIA = "media";
	String EVENT_MIME_TYPE = "mime-type";
	String EVENT_MEDIA_URI = "uri";
	String EVENT_TAGS = "tags";
	String EVENT_PRIORITY = "priority";
	String EVENT_DATETIME = "datetime";
	String EVENT_STATUS = "status";
	String EVENT_USER = USER;
	String EVENT_LOCATION = "location";
	String EVENT_COMMENTS = "comments";

	String MEDIA_ID = "id";
	String MEDIA_MIME_TYPE = "type";

	String USER_ID = "id";
	String USER_EMAIL = "email";
	String USER_ROLE = "role";
	String USER_ORGANIZATION = "organization";
	String USER_PASSWORD = "password";
	
	String LOCATION_LATITUDE = "lat";
	String LOCATION_LONGITUDE = "lon";
	String LOCATION_CRS = "crs";

	String COMMENT_USER = USER;
	String COMMENT_TEXT = "text";
	String COMMENT_DATETIME = "datetime";
	
	String LIST_FILTER = "filter";
	String LIST_FROM = "from";
	String LIST_TO = "to";
	String LIST_BBOX = "bbox";
	String LIST_BBOX_LONGITUDE_MIN = "lon-min";
	String LIST_BBOX_LATITUDE_MIN = "lat-min";
	String LIST_BBOX_LONGITUDE_MAX = "lon-max";
	String LIST_BBOX_LATITUDE_MAX = "lat-max";
	
	String PROPERTY_LOGIN_REQUIRED = "login-required";
	String PROPERTY_STATUS_DEFAULT = "status-default";
	String PROPERTY_ROLE_DEFAULT = "role-default";
	String PROPERTY_PRIORITY_DEFAULT = "priority-default";
	
	String CONFIG_TAGS = "tags";
	String CONFIG_MIME_TYPES = "mime_types";
	String CONFIG_STATUSES = "statuses";
	String CONFIG_PRIORITIES = "priorities";
	String CONFIG_LOGIN_REQUIRED = PROPERTY_LOGIN_REQUIRED;

	String ROLE_ADMIN = "admin";
}
