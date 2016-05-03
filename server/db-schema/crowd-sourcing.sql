--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.5
-- Dumped by pg_dump version 9.4.0
-- Started on 2016-05-03 11:04:47 CEST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

DROP DATABASE cs;
--
-- TOC entry 3717 (class 1262 OID 18305)
-- Name: cs; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE cs WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect cs

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 3718 (class 1262 OID 18305)
-- Dependencies: 3717
-- Name: cs; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON DATABASE cs IS 'Crowd-sourcing';


--
-- TOC entry 5 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- TOC entry 3719 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 7 (class 2615 OID 19730)
-- Name: topology; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA topology;


--
-- TOC entry 207 (class 3079 OID 12123)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 3721 (class 0 OID 0)
-- Dependencies: 207
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 209 (class 3079 OID 18434)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 3722 (class 0 OID 0)
-- Dependencies: 209
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- TOC entry 210 (class 3079 OID 19731)
-- Name: postgis_topology; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_topology WITH SCHEMA topology;


--
-- TOC entry 3723 (class 0 OID 0)
-- Dependencies: 210
-- Name: EXTENSION postgis_topology; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis_topology IS 'PostGIS topology spatial types and functions';


--
-- TOC entry 208 (class 3079 OID 26554)
-- Name: postgres_fdw; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgres_fdw WITH SCHEMA public;


--
-- TOC entry 3724 (class 0 OID 0)
-- Dependencies: 208
-- Name: EXTENSION postgres_fdw; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgres_fdw IS 'foreign-data wrapper for remote PostgreSQL servers';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 177 (class 1259 OID 18341)
-- Name: comment; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE comment (
    id integer NOT NULL,
    event integer NOT NULL,
    text character varying,
    "user" character varying,
    datetime timestamp with time zone
);


--
-- TOC entry 176 (class 1259 OID 18339)
-- Name: comments_event_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE comments_event_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3725 (class 0 OID 0)
-- Dependencies: 176
-- Name: comments_event_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE comments_event_seq OWNED BY comment.event;


--
-- TOC entry 175 (class 1259 OID 18337)
-- Name: comments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3726 (class 0 OID 0)
-- Dependencies: 175
-- Name: comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE comments_id_seq OWNED BY comment.id;


--
-- TOC entry 178 (class 1259 OID 18353)
-- Name: event; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE event (
    uuid uuid NOT NULL,
    description character varying,
    "user" character varying,
    status character varying,
    priority character varying,
    datetime timestamp with time zone,
    id integer NOT NULL,
    location geometry,
    tags text[]
);


--
-- TOC entry 179 (class 1259 OID 18373)
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3727 (class 0 OID 0)
-- Dependencies: 179
-- Name: event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE event_id_seq OWNED BY event.id;


--
-- TOC entry 173 (class 1259 OID 18307)
-- Name: media; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE media (
    uuid uuid,
    mime_type text,
    encoding text,
    data bytea,
    id integer NOT NULL,
    event_id integer NOT NULL
);


--
-- TOC entry 181 (class 1259 OID 18419)
-- Name: media_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE media_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3728 (class 0 OID 0)
-- Dependencies: 181
-- Name: media_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE media_event_id_seq OWNED BY media.event_id;


--
-- TOC entry 180 (class 1259 OID 18401)
-- Name: media_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE media_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3729 (class 0 OID 0)
-- Dependencies: 180
-- Name: media_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE media_id_seq OWNED BY media.id;


--
-- TOC entry 203 (class 1259 OID 19911)
-- Name: mime_type; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mime_type (
    mime_type text NOT NULL
);


--
-- TOC entry 205 (class 1259 OID 19927)
-- Name: priority; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE priority (
    priority text NOT NULL,
    "default" boolean DEFAULT false
);


--
-- TOC entry 206 (class 1259 OID 31764)
-- Name: property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE property (
    key character varying NOT NULL,
    value character varying
);


--
-- TOC entry 204 (class 1259 OID 19919)
-- Name: status; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE status (
    status text NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19901)
-- Name: tag; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE tag (
    id integer NOT NULL,
    tag text
);


--
-- TOC entry 201 (class 1259 OID 19899)
-- Name: tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3730 (class 0 OID 0)
-- Dependencies: 201
-- Name: tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE tags_id_seq OWNED BY tag.id;


--
-- TOC entry 174 (class 1259 OID 18318)
-- Name: user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "user" (
    id character varying NOT NULL,
    email character varying,
    role character varying,
    organization character varying,
    password character varying
);


--
-- TOC entry 3566 (class 2604 OID 18344)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY comment ALTER COLUMN id SET DEFAULT nextval('comments_id_seq'::regclass);


--
-- TOC entry 3567 (class 2604 OID 18345)
-- Name: event; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY comment ALTER COLUMN event SET DEFAULT nextval('comments_event_seq'::regclass);


--
-- TOC entry 3568 (class 2604 OID 18375)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY event ALTER COLUMN id SET DEFAULT nextval('event_id_seq'::regclass);


--
-- TOC entry 3564 (class 2604 OID 18403)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY media ALTER COLUMN id SET DEFAULT nextval('media_id_seq'::regclass);


--
-- TOC entry 3565 (class 2604 OID 18421)
-- Name: event_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY media ALTER COLUMN event_id SET DEFAULT nextval('media_event_id_seq'::regclass);


--
-- TOC entry 3573 (class 2604 OID 19904)
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY tag ALTER COLUMN id SET DEFAULT nextval('tags_id_seq'::regclass);


--
-- TOC entry 3580 (class 2606 OID 18350)
-- Name: comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- TOC entry 3582 (class 2606 OID 18384)
-- Name: event_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_pk PRIMARY KEY (id);


--
-- TOC entry 3576 (class 2606 OID 18417)
-- Name: media_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY media
    ADD CONSTRAINT media_pk PRIMARY KEY (id);


--
-- TOC entry 3586 (class 2606 OID 19918)
-- Name: mime_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mime_type
    ADD CONSTRAINT mime_type_pkey PRIMARY KEY (mime_type);


--
-- TOC entry 3590 (class 2606 OID 19935)
-- Name: priority_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT priority_pkey PRIMARY KEY (priority);


--
-- TOC entry 3592 (class 2606 OID 31771)
-- Name: properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property
    ADD CONSTRAINT properties_pkey PRIMARY KEY (key);


--
-- TOC entry 3588 (class 2606 OID 19926)
-- Name: status_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT status_pkey PRIMARY KEY (status);


--
-- TOC entry 3584 (class 2606 OID 19906)
-- Name: tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- TOC entry 3578 (class 2606 OID 19888)
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- TOC entry 3593 (class 2606 OID 18429)
-- Name: event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY media
    ADD CONSTRAINT event_id FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 3594 (class 2606 OID 19882)
-- Name: event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT event_id FOREIGN KEY (event) REFERENCES event(id);


--
-- TOC entry 3596 (class 2606 OID 19889)
-- Name: user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT user_id FOREIGN KEY ("user") REFERENCES "user"(id);


--
-- TOC entry 3595 (class 2606 OID 19894)
-- Name: user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT user_id FOREIGN KEY ("user") REFERENCES "user"(id);


--
-- TOC entry 3720 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM rszturc;
GRANT ALL ON SCHEMA public TO rszturc;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2016-05-03 11:04:48 CEST

--
-- PostgreSQL database dump complete
--

