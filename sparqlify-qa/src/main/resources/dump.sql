--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: dept; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dept (
    id integer NOT NULL,
    default_name character varying(400) NOT NULL
);


--
-- Name: dept_translation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE dept_translation (
    id integer NOT NULL,
    lang character varying(3) NOT NULL,
    name character varying(400) NOT NULL
);


--
-- Name: employee; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE employee (
    id integer NOT NULL,
    firstname character varying(40),
    lastname character varying(60) NOT NULL,
    employment_time integer,
    employment_time_unit character varying(20),
    birthday date NOT NULL,
    salary_monthly integer NOT NULL,
    web_id character varying(355),
    married boolean,
    dept integer
);


--
-- Data for Name: dept; Type: TABLE DATA; Schema: public; Owner: -
--

COPY dept (id, default_name) FROM stdin;
1	it
2	accounting
3	production
4	marketing
\.


--
-- Data for Name: dept_translation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY dept_translation (id, lang, name) FROM stdin;
1	en	IT
1	de	IT
2	en	Accounting
2	de	Buchhaltung
3	en	Production
3	de	Produktion
4	en	Marketing
4	de	Marketing
1	sx	Ei Dieh
\.


--
-- Data for Name: employee; Type: TABLE DATA; Schema: public; Owner: -
--

COPY employee (id, firstname, lastname, employment_time, employment_time_unit, birthday, salary_monthly, web_id, married, dept) FROM stdin;
1	Alf	Alliteration	3	month	1979-01-05	3600	http://alf.alliteration.name	t	1
2	Bela	B.	15	year	1968-02-28	4200	\N	f	4
3	Charly	Chaplin	2	gYear	1983-09-17	1875	\N	f	4
4	Daniel	Defoe	6	month	1990-12-04	2200	http://defoe.name/daniel	f	3
5	Edna	Erdmann	6	gYear	1960-03-03	2300	\N	t	2
6	Fred	Flintstone	5	gYear	0975-02-20	3000	http://flintstones.name/fred	t	1
7	Guiseppe	Guarino	2	gYear	1980-12-14	3200	\N	t	2
8	Hillary	Hart	4	month	1987-01-23	2600	http://hillary.hart.name	f	3
9	Iadh	Idehen	3	gYear	1985-04-12	2200	\N	t	1
10	Jiao	Jin	11	month	1979-08-07	2600	http://jin.name/ids/jiao	f	1
11	Hèléna	Höllmøllâr	3	gYear	1977-11-01	3200	\N	t	4
\.


--
-- Name: dept_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dept
    ADD CONSTRAINT dept_pkey PRIMARY KEY (id);


--
-- Name: dept_translations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dept_translation
    ADD CONSTRAINT dept_translations_pkey PRIMARY KEY (id, lang, name);


--
-- Name: employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY employee
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: dept_translations_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dept_translation
    ADD CONSTRAINT dept_translations_id_fkey FOREIGN KEY (id) REFERENCES dept(id);


--
-- Name: employees_dept_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY employee
    ADD CONSTRAINT employees_dept_fkey FOREIGN KEY (dept) REFERENCES dept(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

