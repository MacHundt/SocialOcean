--
-- PostgreSQL database dump
--

-- Dumped from database version 10.2 (Debian 10.2-1.pgdg90+1)
-- Dumped by pg_dump version 10.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: timezones; Type: TABLE; Schema: public; Owner: socialocean
--

CREATE TABLE timezones (
    user_timezone character varying(45),
    user_utcoffset integer,
    latitude double precision,
    longitude double precision
);


ALTER TABLE timezones OWNER TO postgres;

--
-- Data for Name: timezones; Type: TABLE DATA; Schema: public; Owner: socialocean
--

INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Rangoon', 23400, -32.9694300000000027, 148.624009999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Guam', 36000, 13.4404199999999996, 144.809089999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Helsinki', 7200, 60.1755599999999973, 24.9341700000000017);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Ekaterinburg', 21600, 49.9918759999999978, 14.5979709999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Brasilia', -10800, -15.7915899999999993, -47.8955800000000025);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Bucharest', 7200, 44.4322500000000034, 26.1062599999999989);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Nuku''alofa', 46800, -9.37058000000000035, 179.807850000000002);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kyiv', 10800, 50.25, 30.5);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tehran', 12600, 35.5880810000000025, 51.4317739999999972);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Urumqi', 28800, 43.8009600000000034, 87.6004599999999982);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Chihuahua', -25200, 28.8266899999999993, -106.198759999999993);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Cairo', 7200, 30.0626299999999986, 31.2496699999999983);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Dublin', 0, 53.3330600000000032, -6.24889000000000028);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Arizona', -25200, 34.5003000000000029, -111.500979999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Hawaii', -36000, 20.7502800000000001, -156.500280000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Madrid', 3600, 40.4299129999999991, -3.66924500000000009);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Canberra', 36000, -35.283459999999998, 149.128070000000008);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Volgograd', 14400, 48.7255889999999994, 44.4906450000000007);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Buenos Aires', -10800, -36.4267010000000013, -60.3313029999999983);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Novosibirsk', 25200, 55.0414999999999992, 82.9346000000000032);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kolkata', 19800, 22.5626299999999986, 88.363039999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Belgrade', 3600, 44.8040099999999981, 20.4651299999999985);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Brisbane', 36000, -27.4679399999999987, 153.028089999999992);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Midway Island', -39600, 55.2544400000000024, -133.09611000000001);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Alaska', -32400, 64.0002800000000036, -150.000280000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Jakarta', 25200, -6.21462000000000003, 106.845129999999997);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Caracas', -16200, 10.4880099999999992, -66.8791899999999941);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Monrovia', 0, 6.30053999999999981, -10.7969000000000008);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Riyadh', 10800, 24.6877299999999984, 46.7218500000000034);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Sapporo', 32400, 43.066670000000002, 141.349999999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Sydney', 36000, -34.0361180000000019, 151.192204000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Saskatchewan', -21600, 54.0001000000000033, -106.000990000000002);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Harare', 7200, -17.8552929999999996, 31.0584160000000011);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Copenhagen', 3600, 55.7044380000000032, 12.5021190000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Cape Verde Is.', -3600, 16, -24);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Baku', 14400, 40.3776700000000019, 49.8920099999999991);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Edinburgh', 0, 55.952060000000003, -3.19648000000000021);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Samoa', -39600, -13.6391399999999994, -172.438241000000005);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Georgetown', -10800, 38.9095810000000029, -77.0651399999999995);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Bangkok', 25200, 13.8815910000000002, 100.644532999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Abu Dhabi', 14400, 23.4831420000000008, 54.3788139999999984);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Newfoundland', -12600, 52, -56);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Berlin', 3600, 52.5204499999999967, 13.4073200000000003);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Karachi', 18000, 24.8608000000000011, 67.0104000000000042);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tbilisi', 14400, 41.694110000000002, 44.8336800000000011);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('St. Petersburg', 14400, 27.770859999999999, -82.6792700000000025);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Baghdad', 10800, 33.3405800000000028, 44.4008800000000008);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Dhaka', 21600, 24.4606290000000008, 90.4343640000000022);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Mexico City', -21600, 19.4284700000000008, -99.1276600000000059);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Krasnoyarsk', 28800, 56.0183899999999966, 92.8671700000000016);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Guadalajara', -21600, 20.6668200000000013, -103.391819999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Warsaw', 3600, 52.229770000000002, 21.0117800000000017);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Indiana (East)', -18000, 41.6808559999999986, -87.4411029999999982);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Chennai', 19800, 13.0889100000000003, 80.2648300000000035);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Fiji', 43200, -17.8340159999999983, 177.972003999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Solomon Is.', 39600, -8, 159);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('New Delhi', 19800, 28.6357600000000012, 77.2244500000000045);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tijuana', -28800, 32.5022299999999973, -116.972120000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Athens', 7200, 37.9794499999999999, 23.7162199999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('London', 0, 51.5096479999999985, -0.0990759999999999974);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Astana', 21600, 51.180100000000003, 71.4459800000000058);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Quito', -18000, -0.229849999999999999, -78.524950000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Muscat', 14400, 23.5841299999999983, 58.4077800000000025);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Mazatlan', -25200, 23.2329000000000008, -106.406199999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Budapest', 3600, 47.4980100000000007, 19.039909999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Ulaan Bataar', 28800, 49.3774689999999978, 105.914850999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Sri Jayawardenepura', 21600, 29.491579999999999, 78.5745900000000006);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Riga', 7200, 56.9777800000000028, 24.1216700000000017);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kathmandu', 20700, 27.7016899999999993, 85.3205999999999989);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Paris', 3600, 48.8533899999999974, 2.34864000000000006);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Irkutsk', 32400, 52.297780000000003, 104.296390000000002);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Greenland', -10800, 39.8559610000000006, -75.058211);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Lisbon', 0, 38.7263499999999965, -9.1484299999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Brussels', 3600, 50.8504500000000021, 4.34877999999999965);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('West Central Africa', 3600, 14.6617409999999992, -17.4371930000000006);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Casablanca', 0, 33.5884, -7.55785000000000018);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tashkent', 18000, 41.3195199999999971, 69.2469880000000018);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Darwin', 34200, -12.4611300000000007, 130.841849999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Stockholm', 3600, 59.5, 18);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Hobart', 36000, -42.8793599999999984, 147.329409999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Jerusalem', 7200, 31.75, 35);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Mid-Atlantic', -7200, 40.4719910000000027, -79.9596159999999969);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Marshall Is.', 43200, 8.71083699999999972, 171.237308000000013);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Islamabad', 18000, 33.7214799999999997, 73.0432899999999989);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Port Moresby', 36000, -9.44313999999999965, 147.179720000000003);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tallinn', 7200, 59.4013470000000012, 24.693912000000001);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('New Caledonia', 39600, -21.5, 165.5);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Minsk', 10800, 53.8999999999999986, 27.5666699999999985);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Sofia', 7200, 42.6666700000000034, 23.8000000000000007);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Amsterdam', 3600, 52.3724300000000014, 4.89972999999999992);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Vienna', 3600, 48.2084899999999976, 16.3720800000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Monterrey', -21600, 25.6851629999999993, -100.315237999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Prague', 3600, 50.0880399999999995, 14.4207599999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Yakutsk', 36000, 62.0338899999999995, 129.733059999999995);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('La Paz', -14400, -15.5773320000000002, -68.176368999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Wellington', 43200, -41.2833299999999994, 174.766670000000005);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Almaty', 21600, 43.2566699999999997, 76.9286100000000062);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Azores', -3600, 37.7405800000000013, -25.6728200000000015);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Tokyo', 32400, 35.661208000000002, 139.380200000000002);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Bern', 3600, 46.9413320000000027, 7.43096599999999974);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Ljubljana', 3600, 46.0626880000000014, 14.5038789999999995);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Skopje', 3600, 42, 21.4166699999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Nairobi', 10800, -1.27563399999999993, 36.8650560000000027);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Bogota', -18000, 4.60970999999999975, -74.0817499999999995);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Hanoi', 25200, 21.0244999999999997, 105.841170000000005);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Rome', 3600, 41.8919300000000021, 12.5113299999999992);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Central America', -21600, 25.3241699999999987, -99.6679699999999968);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Istanbul', 7200, 41.0350800000000007, 28.9833099999999995);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Seoul', 32400, 37.5396190000000018, 127.009675999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Zagreb', 3600, 45.8060260000000028, 15.9762179999999994);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Magadan', 43200, 59.5698340000000002, 150.801796999999993);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Melbourne', 36000, -37.8140000000000001, 144.96332000000001);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Beijing', 28800, 39.9074999999999989, 116.397229999999993);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Chongqing', 28800, 29.5627800000000001, 106.552779999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Pretoria', 7200, -25.7448599999999992, 28.1878300000000017);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Moscow', 14400, 55.7414690000000022, 37.6155609999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Vilnius', 7200, 54.6891600000000011, 25.2798000000000016);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Auckland', 43200, -36.8500000000000014, 174.783330000000007);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Bratislava', 3600, 48.3333299999999966, 17.1666699999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Perth', 28800, -31.9522399999999998, 115.861400000000003);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Taipei', 28800, 25.0477600000000002, 121.531850000000006);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Osaka', 32400, 34.6107530000000025, 135.525937999999996);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Yerevan', 14400, 40.1811099999999968, 44.5136099999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Santiago', -14400, -33.456940000000003, -70.6482699999999966);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Hong Kong', 28800, 22.2783200000000008, 114.174689999999998);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kabul', 16200, 34.5947409999999991, 69.1410230000000041);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Sarajevo', 3600, 43.8486400000000032, 18.3564399999999992);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kamchatka', 43200, 54.8500000000000014, 99.6500000000000057);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kuala Lumpur', 28800, 3.104244, 101.695232000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Lima', -18000, -11.8499999999999996, -76.4500000000000028);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Kuwait', 10800, 29.4222259999999984, 47.3300150000000031);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Adelaide', 34200, -34.9286600000000007, 138.598630000000014);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Mumbai', 19800, 19.0728299999999997, 72.8826099999999997);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Vladivostok', 39600, 43.1056200000000018, 131.873529999999988);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Singapore', 28800, 1.35768500000000003, 103.809380000000004);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Mountain Time (US & Canada)', -25200, 51.0486149999999981, -114.070846000000003);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Pacific Time (US & Canada)', -28800, 34.0522339999999986, -118.243684999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Central Time (US & Canada)', -21600, 53.5443890000000025, -113.490926999999999);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Eastern Time (US & Canada)', -18000, 43.6532259999999965, -79.383184);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('Atlantic Time (Canada)', -14400, 44.6487639999999999, -63.5752390000000034);
INSERT INTO timezones (user_timezone, user_utcoffset, latitude, longitude) VALUES ('International Date Line West', -39600, -21.1370000000000005, -175.225999999999999);


--
-- PostgreSQL database dump complete
--

