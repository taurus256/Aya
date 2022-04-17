-- --
-- -- PostgreSQL database dump
-- --
--
-- -- Dumped from database version 13.5 (Ubuntu 13.5-0ubuntu0.21.04.1)
-- -- Dumped by pg_dump version 13.5 (Ubuntu 13.5-0ubuntu0.21.04.1)
--
-- SET statement_timeout = 0;
-- SET lock_timeout = 0;
-- SET idle_in_transaction_session_timeout = 0;
-- SET client_encoding = 'UTF8';
-- SET standard_conforming_strings = on;
-- SELECT pg_catalog.set_config('search_path', '', false);
-- SET check_function_bodies = false;
-- SET xmloption = content;
-- SET client_min_messages = warning;
-- SET row_security = off;
--
-- --
-- -- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
-- --
--
-- CREATE SCHEMA public;
--
--
-- ALTER SCHEMA public OWNER TO postgres;
--
-- --
-- -- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
-- --
--
-- COMMENT ON SCHEMA public IS 'standard public schema';
--
--
-- SET default_tablespace = '';
--
-- SET default_table_access_method = heap;
--
-- --
-- -- Name: event; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.event (
--                               id bigint NOT NULL,
--                               author bigint,
--                               description character varying(255),
--                               duration_h double precision,
--                               enddate timestamp without time zone,
--                               eventwindowstyle character varying(255),
--                               executor bigint,
--                               icon character varying(255),
--                               index integer,
--                               is_graph boolean,
--                               lane character varying(255),
--                               name character varying(255),
--                               priority integer,
--                               rgroup bigint,
--                               ruser bigint,
--                               start timestamp without time zone,
--                               startdate timestamp without time zone,
--                               state integer,
--                               task_id bigint,
--                               user_correct_spent_time boolean,
--                               wgroup bigint,
--                               wuser bigint,
--                               spent_time double precision
-- );
--
--
-- ALTER TABLE public.event OWNER TO aya_usr;
--
-- --
-- -- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE SEQUENCE public.event_id_seq
--     START WITH 1
--     INCREMENT BY 1
--     NO MINVALUE
--     NO MAXVALUE
--     CACHE 1;
--
--
-- ALTER TABLE public.event_id_seq OWNER TO aya_usr;
--
-- --
-- -- Name: event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aya_usr
-- --
--
-- ALTER SEQUENCE public.event_id_seq OWNED BY public.event.id;
--
--
-- --
-- -- Name: groups; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.groups (
--                                id integer NOT NULL,
--                                description character varying(255),
--                                name character varying(255)
-- );
--
--
-- ALTER TABLE public.groups OWNER TO aya_usr;
--
-- --
-- -- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE SEQUENCE public.groups_id_seq
--     AS integer
--     START WITH 1
--     INCREMENT BY 1
--     NO MINVALUE
--     NO MAXVALUE
--     CACHE 1;
--
--
-- ALTER TABLE public.groups_id_seq OWNER TO aya_usr;
--
-- --
-- -- Name: groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aya_usr
-- --
--
-- ALTER SEQUENCE public.groups_id_seq OWNED BY public.groups.id;
--
--
-- --
-- -- Name: lane; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.lane (
--                              id bigint NOT NULL,
--                              author bigint,
--                              description character varying(255),
--                              isfolder boolean,
--                              lane boolean,
--                              lane_order integer,
--                              name character varying(255),
--                              parent integer,
--                              rgroup integer,
--                              ruser integer,
--                              visible boolean,
--                              wgroup integer,
--                              wuser integer,
--                              analysed boolean
-- );
--
--
-- ALTER TABLE public.lane OWNER TO aya_usr;
--
-- --
-- -- Name: lane_id_seq; Type: SEQUENCE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE SEQUENCE public.lane_id_seq
--     START WITH 1
--     INCREMENT BY 1
--     NO MINVALUE
--     NO MAXVALUE
--     CACHE 1;
--
--
-- ALTER TABLE public.lane_id_seq OWNER TO aya_usr;
--
-- --
-- -- Name: lane_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aya_usr
-- --
--
-- ALTER SEQUENCE public.lane_id_seq OWNED BY public.lane.id;
--
--
-- --
-- -- Name: relation_user_group; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.relation_user_group (
--                                             userid bigint NOT NULL,
--                                             groupid integer NOT NULL
-- );
--
--
-- ALTER TABLE public.relation_user_group OWNER TO aya_usr;
--
-- --
-- -- Name: task; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.task (
--                              id bigint NOT NULL,
--                              author bigint,
--                              description character varying,
--                              end_date timestamp without time zone,
--                              executor integer,
--                              lane character varying(255),
--                              name character varying(255),
--                              pause_days integer,
--                              priority integer,
--                              process_time double precision,
--                              rgroup integer,
--                              ruser integer,
--                              start timestamp without time zone,
--                              start_date timestamp without time zone,
--                              wgroup integer,
--                              wuser integer,
--                              event_count integer,
--                              planned_duration double precision,
--                              state integer,
--                              show_in_backlog boolean,
--                              spent_time double precision,
--                              external_jira_task_id character varying(255)
-- );
--
--
-- ALTER TABLE public.task OWNER TO aya_usr;
--
-- --
-- -- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE SEQUENCE public.task_id_seq
--     START WITH 1
--     INCREMENT BY 1
--     NO MINVALUE
--     NO MAXVALUE
--     CACHE 1;
--
--
-- ALTER TABLE public.task_id_seq OWNER TO aya_usr;
--
-- --
-- -- Name: task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aya_usr
-- --
--
-- ALTER SEQUENCE public.task_id_seq OWNED BY public.task.id;
--
--
-- --
-- -- Name: users; Type: TABLE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE TABLE public.users (
--                               id bigint NOT NULL,
--                               firstname character varying(255),
--                               mobphone character varying(255),
--                               nickname character varying(255),
--                               password_hash character varying(255),
--                               patronymic character varying(255),
--                               showed_name character varying(255),
--                               surname character varying(255),
--                               usid character varying(255),
--                               workphone character varying(255),
--                               created timestamp without time zone,
--                               jira_login character varying(255),
--                               jira_pass character varying(255),
--                               use_jira boolean
-- );
--
--
-- ALTER TABLE public.users OWNER TO aya_usr;
--
-- --
-- -- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: aya_usr
-- --
--
-- CREATE SEQUENCE public.users_id_seq
--     START WITH 1
--     INCREMENT BY 1
--     NO MINVALUE
--     NO MAXVALUE
--     CACHE 1;
--
--
-- ALTER TABLE public.users_id_seq OWNER TO aya_usr;
--
-- --
-- -- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: aya_usr
-- --
--
-- ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
--
--
-- --
-- -- Name: event id; Type: DEFAULT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.event ALTER COLUMN id SET DEFAULT nextval('public.event_id_seq'::regclass);
--
--
-- --
-- -- Name: groups id; Type: DEFAULT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.groups ALTER COLUMN id SET DEFAULT nextval('public.groups_id_seq'::regclass);
--
--
-- --
-- -- Name: lane id; Type: DEFAULT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.lane ALTER COLUMN id SET DEFAULT nextval('public.lane_id_seq'::regclass);
--
--
-- --
-- -- Name: task id; Type: DEFAULT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);
--
--
-- --
-- -- Name: users id; Type: DEFAULT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);
--
--
-- --
-- -- Data for Name: event; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.event (id, author, description, duration_h, enddate, eventwindowstyle, executor, icon, index, is_graph, lane, name, priority, rgroup, ruser, start, startdate, state, task_id, user_correct_spent_time, wgroup, wuser, spent_time) FROM stdin;
-- 211	\N	\N	\N	2020-07-05 23:59:59.356	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2020-07-05 00:00:00	1	159	f	\N	\N	0
-- 208	\N	\N	\N	2020-07-09 23:59:59	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2020-07-08 00:00:00	1	157	f	\N	\N	-42.7
-- 270	\N	\N	\N	2021-04-04 23:59:59.082	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2021-04-04 00:00:00	1	178	f	\N	\N	0
-- 201	\N	\N	\N	2020-06-21 23:59:59	s3_event_new	\N	tree/task_normal.png	0	t	\N	\N	\N	\N	\N	\N	2020-06-21 00:00:00	0	154	f	\N	\N	1.3
-- 186	\N	\N	\N	2020-05-16 23:59:59	\N	\N	tree/task0.png	0	t	\N	\N	\N	\N	\N	\N	2020-05-15 00:00:00	0	139	f	\N	\N	0
-- 269	\N	\N	\N	2021-04-02 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2021-04-02 00:00:00	0	178	f	\N	\N	0
-- 206	\N	\N	\N	2020-07-01 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-01 00:00:00	0	157	f	\N	\N	22.1
-- 209	\N	\N	\N	2020-06-30 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-06-30 00:00:00	0	159	f	\N	\N	1.2
-- 188	\N	\N	\N	2020-06-09 23:59:59	\N	\N	tree/task_normal.png	0	t	\N	\N	\N	\N	\N	\N	2020-06-08 00:00:00	0	136	f	\N	\N	0
-- 195	\N	\N	\N	2020-06-16 23:59:59	\N	\N	tree/task_normal.png	0	t	\N	\N	\N	\N	\N	\N	2020-06-15 00:00:00	0	142	f	\N	\N	0
-- 271	\N	\N	\N	2021-04-09 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2021-04-03 00:00:00	0	179	f	\N	\N	0.6
-- 272	\N	\N	\N	2021-04-12 23:59:59.221	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2021-04-11 00:00:00	1	179	f	\N	\N	8.5
-- 200	\N	\N	\N	2020-06-17 23:59:59	\N	\N	tree/task_normal.png	0	t	\N	\N	\N	\N	\N	\N	2020-06-17 00:00:00	4	148	f	\N	\N	0
-- 254	\N	\N	\N	2020-07-17 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-17 00:00:00	0	163	f	\N	\N	0
-- 182	\N	\N	\N	2020-04-23 23:59:59	\N	\N	tree/task3.png	0	t	\N	\N	\N	\N	\N	\N	2020-04-22 00:00:00	0	140	f	\N	\N	4
-- 274	\N	\N	\N	2021-08-31 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2021-08-31 00:00:00	0	181	f	\N	\N	0.2833333333333333
-- 203	\N	\N	\N	2020-06-21 23:23:59	s3_event_new	\N	tree/task_normal.png	0	t	\N	\N	\N	\N	\N	\N	2020-06-21 00:00:00	0	156	f	\N	\N	0
-- 267	\N	\N	\N	2020-07-03 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-01 00:00:00	0	176	f	\N	\N	0
-- 205	\N	\N	\N	2020-06-26 23:59:59	\N	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-06-25 00:00:00	4	155	f	\N	\N	5
-- 207	\N	\N	\N	2020-07-02 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-02 00:00:00	0	158	f	\N	\N	0
-- 268	\N	\N	\N	2020-07-17 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-13 00:00:00	0	177	f	\N	\N	0
-- 243	\N	\N	\N	2020-07-05 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2020-07-04 00:00:00	0	160	f	\N	\N	6
-- 283	\N	\N	\N	2022-04-11 23:59:59.543	\N	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2022-04-03 00:00:00	3	152	f	\N	\N	2.2
-- 275	\N	\N	\N	2021-09-09 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2021-09-09 00:00:00	0	182	f	\N	\N	0
-- 277	\N	\N	\N	2021-09-09 23:59:59	\N	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2021-09-09 00:00:00	1	183	f	\N	\N	0.1
-- 278	\N	\N	\N	2022-03-14 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2022-03-14 00:00:00	0	184	f	\N	\N	0
-- 279	\N	\N	\N	2022-03-20 23:59:59.547	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2022-03-20 00:00:00	1	184	f	\N	\N	0.35
-- 281	\N	\N	\N	2022-04-16 23:59:59	s3_event_new	\N	\N	0	t	\N	\N	\N	\N	\N	\N	2022-04-15 00:00:00	4	185	f	\N	\N	0
-- 285	\N	\N	\N	2022-04-16 23:59:59	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2022-04-15 00:00:00	4	185	f	\N	\N	0.2
-- 284	\N	\N	\N	2022-04-10 23:59:59	s3_process	\N	\N	1	t	\N	\N	\N	\N	\N	\N	2022-04-10 00:00:00	1	152	f	\N	\N	8.2
-- \.
--
--
-- --
-- -- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.groups (id, description, name) FROM stdin;
-- 2	1234	managers
-- 1		admins
-- 3	demo group	demo
-- \.
--
--
-- --
-- -- Data for Name: lane; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.lane (id, author, description, isfolder, lane, lane_order, name, parent, rgroup, ruser, visible, wgroup, wuser, analysed) FROM stdin;
-- 2	1		\N	\N	1	Aya - Баги	\N	-1	\N	t	-1	\N	t
-- 9	1		\N	\N	2	Разработка	\N	-1	\N	t	-1	\N	t
-- 8	1		\N	\N	3	Тестирование	\N	-1	\N	t	-1	\N	t
-- 7	1		\N	\N	4	Отладка	\N	-1	\N	t	-1	\N	f
-- 1	1		\N	\N	0	Aya - UI	\N	-1	\N	t	-1	\N	t
-- \.
--
--
-- --
-- -- Data for Name: relation_user_group; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.relation_user_group (userid, groupid) FROM stdin;
-- 1	2
-- 1	1
-- 13	3
-- 15	3
-- \.
--
--
-- --
-- -- Data for Name: task; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.task (id, author, description, end_date, executor, lane, name, pause_days, priority, process_time, rgroup, ruser, start, start_date, wgroup, wuser, event_count, planned_duration, state, show_in_backlog, spent_time, external_jira_task_id) FROM stdin;
-- 136	1		2020-06-09 23:59:59	1	Aya - Баги	asdasdasd	\N	1	\N	\N	\N	\N	2020-06-08 00:00:00	\N	\N	\N	0	0	f	0	\N
-- 160	1	ПНЕВМОСЛОН\n	2020-07-05 23:59:59	1	Разработка	ЛОРД	\N	1	\N	\N	\N	\N	2020-07-04 00:00:00	\N	\N	\N	16	2	f	6	\N
-- 159	1		2020-07-05 23:59:59.356	1	Разработка	Чучундра!	\N	1	\N	\N	\N	2020-07-05 20:47:44.352	2020-06-30 00:00:00	\N	\N	\N	1	2	f	1.2	\N
-- 155	1		2020-06-26 23:59:59	1	Разработка	Новая задача!	\N	1	\N	2	\N	2020-06-25 13:49:07.319	2020-06-25 00:00:00	-1	\N	\N	5	2	f	5	\N
-- 156	1		2020-06-21 23:59:59	1	Тестирование	Новая задача	\N	1	\N	\N	\N	2020-06-22 11:36:10.703	2020-06-21 00:00:00	\N	\N	\N	0	3	f	0	\N
-- 157	1		2020-07-09 23:59:59	1	Тестирование	Новая задача	\N	1	\N	\N	\N	2020-07-06 13:25:01.39	2020-07-01 00:00:00	\N	\N	\N	5	3	f	-20.6	\N
-- 154	1		2020-06-21 23:59:59	1	Aya - Баги	НОВАЯ ТЕСТОВАЯ ЗАДАЧА	\N	1	\N	\N	\N	2020-06-22 11:27:21.729	2020-06-21 00:00:00	\N	\N	\N	1	2	f	1.3	\N
-- 158	1		\N	2	Отладка	Старая задача	\N	1	\N	\N	\N	\N	\N	\N	\N	\N	2	0	f	0	\N
-- 161	1		2020-07-16 23:59:59	1	Aya - Баги	Горячие клавиши вызова панелей	\N	1	\N	\N	\N	2020-07-16 10:14:01.713	2020-07-11 00:00:00	\N	\N	\N	3	3	t	0	\N
-- 147	1		2020-07-17 23:59:59	1	Разработка	EEEEE	\N	0	\N	\N	\N	2020-07-16 10:13:41.273	2020-07-16 00:00:00	\N	\N	\N	7	3	f	5.4	\N
-- 146	1		2020-04-22 23:59:59	1	Aya - Баги	DADAD	\N	0	\N	\N	\N	\N	2020-04-20 00:00:00	\N	\N	\N	\N	0	f	0	\N
-- 139	1		2020-05-16 23:59:59	2	Aya - Баги	555	\N	1	\N	\N	\N	\N	2020-05-15 00:00:00	\N	\N	\N	0	0	f	0	\N
-- 178	1		2021-04-04 23:59:59.082	1	Тестирование	Новая задача	\N	1	\N	\N	\N	2021-04-04 17:13:08.33	2021-04-02 00:00:00	\N	\N	\N	0	3	f	0	PFRPUV-12287
-- 145	1		2020-04-22 23:59:59	1	Aya - Баги	KK	\N	1	\N	\N	\N	\N	2020-04-21 00:00:00	\N	\N	\N	\N	0	t	0	\N
-- 181	1		2021-08-31 23:59:59	1	Разработка	Новая задача	\N	1	\N	\N	\N	2021-08-31 20:11:57.514	2021-08-31 00:00:00	\N	\N	\N	2	2	f	0.2833333333333333	PFRPUV-26878
-- 148	1		2020-06-17 23:59:59	1	Aya - Баги	SQSQSQ	\N	1	\N	\N	\N	\N	2020-06-17 00:00:00	\N	\N	\N	4	0	f	0	\N
-- 163	1		2020-07-17 23:59:59	1	Разработка	Сменить иконки статусов	\N	1	\N	\N	\N	2020-07-17 14:53:17.98	2020-07-17 00:00:00	\N	\N	\N	1	3	f	0	\N
-- 140	1		2020-04-23 23:59:59	1	Aya - UI	fFFFFF	\N	1	\N	\N	\N	\N	2020-04-22 00:00:00	\N	\N	\N	4	2	f	4	\N
-- 142	1		2020-04-22 23:59:59	1	Aya - UI	AAA	\N	1	\N	\N	\N	\N	2020-04-21 00:00:00	\N	\N	\N	\N	0	f	0	\N
-- 176	1		2020-07-03 23:59:59	1	Aya - Баги	Новая задача	\N	1	\N	\N	\N	\N	2020-07-01 00:00:00	\N	\N	\N	5	0	f	0	\N
-- 179	1		2021-04-12 23:59:59.221	1	Разработка	Новая задача	\N	1	\N	\N	\N	2021-04-12 10:30:34.007	2021-04-03 00:00:00	\N	\N	\N	0	2	f	9.1	PFRPUV-13503
-- 177	1		2020-07-17 23:59:59	1	Разработка	Новая задача	\N	1	\N	\N	\N	\N	2020-07-13 00:00:00	\N	\N	\N	5	0	f	0	\N
-- 182	1		2021-09-09 23:59:59	1	Разработка	Новая задача	\N	1	\N	\N	\N	2021-09-09 18:14:47.033	2021-09-09 00:00:00	\N	\N	\N	5	3	f	0	PFRPUV-23884
-- 152	1		2022-04-11 23:59:59.543	1	Тестирование	HAHA	\N	2	\N	\N	\N	2022-04-11 21:09:37.54	2022-04-03 00:00:00	\N	\N	\N	2	2	f	10.399999999999999
-- 183	1		2021-09-09 23:59:59	1	Тестирование	Новая задача	\N	1	\N	\N	\N	2021-09-09 18:21:07.782	2021-09-09 00:00:00	\N	\N	\N	0	2	f	0.1	PFRPUV-27822
-- 184	1		2022-03-20 23:59:59.547	1	Отладка	TEST_STRETCH	\N	1	\N	\N	\N	2022-03-20 17:08:12.536	2022-03-14 00:00:00	\N	\N	\N	1	2	f	0.35
-- 185	1		2022-04-16 23:59:59	1	Aya - Баги	Новая задача	\N	1	\N	\N	\N	2022-04-16 18:53:17.456	2022-04-15 00:00:00	\N	\N	\N	2	2	f	0.2
-- \.
--
--
-- --
-- -- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: aya_usr
-- --
--
-- COPY public.users (id, firstname, mobphone, nickname, password_hash, patronymic, showed_name, surname, usid, workphone, created, jira_login, jira_pass, use_jira) FROM stdin;
-- 13	Demo	\N	demo	\N	\N	\N	User	0c39aa1c-e7c5-44cf-836f-939343edf9b8	\N	2020-07-20 17:35:58.286	\N	\N	\N
-- 2	tbk2		tbk	$2a$12$hgIsNqc5exzEN2H86eigGenTeSud/.6JTuINnYGNSFFQxOllZEyyW	tbk	tbk	tbk	abc5f926-630a-4e62-93e7-c95ae406bdcf		\N	\N	\N	\N
-- 15	Demo	\N	demo	\N	\N	\N	User	9c0c21e3-25e1-4c6c-a707-8f0011da9c38	\N	2022-04-16 08:07:51.075	\N	\N	\N
-- 1	Evgene		taurus	$2a$12$mn.pSTdruwaiDEMjdBThqOv9oS/BTsFuhNyjNnVjzs9EtnkpieuGO	 	Evgeny Ostapenko	Ostapenko	c813cebf-0903-469e-b0e7-0d9193d00862		\N	ostapenko.evgenii	1dbsKfOm10	f
-- \.
--
--
-- --
-- -- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aya_usr
-- --
--
-- SELECT pg_catalog.setval('public.event_id_seq', 285, true);
--
--
-- --
-- -- Name: groups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aya_usr
-- --
--
-- SELECT pg_catalog.setval('public.groups_id_seq', 13, true);
--
--
-- --
-- -- Name: lane_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aya_usr
-- --
--
-- SELECT pg_catalog.setval('public.lane_id_seq', 11, true);
--
--
-- --
-- -- Name: task_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aya_usr
-- --
--
-- SELECT pg_catalog.setval('public.task_id_seq', 185, true);
--
--
-- --
-- -- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: aya_usr
-- --
--
-- SELECT pg_catalog.setval('public.users_id_seq', 15, true);
--
--
-- --
-- -- Name: event event_pkey; Type: CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.event
--     ADD CONSTRAINT event_pkey PRIMARY KEY (id);
--
--
-- --
-- -- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.groups
--     ADD CONSTRAINT groups_pkey PRIMARY KEY (id);
--
--
-- --
-- -- Name: lane lane_pkey; Type: CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.lane
--     ADD CONSTRAINT lane_pkey PRIMARY KEY (id);
--
--
-- --
-- -- Name: task task_pkey; Type: CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.task
--     ADD CONSTRAINT task_pkey PRIMARY KEY (id);
--
--
-- --
-- -- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.users
--     ADD CONSTRAINT users_pkey PRIMARY KEY (id);
--
--
-- --
-- -- Name: task fk7jrsebuukba6rbmk4p1svb246; Type: FK CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.task
--     ADD CONSTRAINT fk7jrsebuukba6rbmk4p1svb246 FOREIGN KEY (executor) REFERENCES public.users(id);
--
--
-- --
-- -- Name: relation_user_group fkcxb0if0nh4l1fkdfywxk5xgif; Type: FK CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.relation_user_group
--     ADD CONSTRAINT fkcxb0if0nh4l1fkdfywxk5xgif FOREIGN KEY (userid) REFERENCES public.users(id);
--
--
-- --
-- -- Name: relation_user_group fki5lhosk74nrhd48ca9neq4k1f; Type: FK CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.relation_user_group
--     ADD CONSTRAINT fki5lhosk74nrhd48ca9neq4k1f FOREIGN KEY (groupid) REFERENCES public.groups(id);
--
--
-- --
-- -- Name: event fkpbluy7bmxphfadi2csk5kyrqr; Type: FK CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.event
--     ADD CONSTRAINT fkpbluy7bmxphfadi2csk5kyrqr FOREIGN KEY (executor) REFERENCES public.users(id);
--
--
-- --
-- -- Name: event fkpxqrrwy7eae3ulr9fj38cyc89; Type: FK CONSTRAINT; Schema: public; Owner: aya_usr
-- --
--
-- ALTER TABLE ONLY public.event
--     ADD CONSTRAINT fkpxqrrwy7eae3ulr9fj38cyc89 FOREIGN KEY (task_id) REFERENCES public.task(id);
--
--
-- --
-- -- PostgreSQL database dump complete
-- --
--
