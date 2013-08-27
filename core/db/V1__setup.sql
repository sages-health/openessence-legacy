/**
 * Set up a (PostgreSQL) database for OpenEssence.
 *
 * Prerequisites:
 * 1. Create an oe user (CREATE USER oe WITH PASSWORD 'PASSWORD')
 * 2. Create database (CREATE DATABASE openessence)
 * 3. Grant privileges to oe (GRANT ALL PRIVILEGES ON DATABASE openessence TO oe). Feel free to grant finer grained
 *     permissions (e.g. readonly on public) if you want more security.
 * 4. Create oe schema (CREATE SCHEMA oe AUTHORIZATION oe)
 * 5. Run this script
 *
 * Note that there is some flexibility in terms of what objects OE expects a
 * DB to have. For example, you can call your user table whatever you want, as
 * long as it's mapped correctly in the ORM layer. However, try to stick to the
 * conventions defined here if possible.
 */
BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis SCHEMA public;

-- postgis_topology creates the topology schema
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- if not exists for schemas is only in Postgres 9.3 devel
-- create schema if not exists oe;

-- if you're lazy and didn't make a dedicated oe user, run this to add the oe schema to the search path
-- ALTER ROLE postgres SET search_path = oe, public;

/*****************************************************************************
 * Objects required by OpenESSENCE
 *****************************************************************************/
CREATE TABLE oe."user" (
  id                      SERIAL PRIMARY KEY,
  name                    TEXT    NOT NULL UNIQUE,
  password                TEXT    NOT NULL,
  salt                    TEXT    NOT NULL,
  algorithm               TEXT    NOT NULL,
  enabled                 BOOLEAN NOT NULL DEFAULT FALSE,
  non_expired             BOOLEAN NOT NULL DEFAULT TRUE,
  credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
  account_non_locked      BOOLEAN NOT NULL DEFAULT TRUE
);

-- add admin/admin login
INSERT INTO oe."user" (name, password, salt, algorithm, enabled, non_expired, credentials_non_expired, account_non_locked)
  VALUES ('admin', '805c31ff4a51bb8641c1fd00ce3eccb680e49be3', 'bACzDYExfadmin203948576', 'SHA-1', TRUE, TRUE, TRUE, TRUE);

CREATE TABLE oe.role (
  id          TEXT NOT NULL PRIMARY KEY,
  description TEXT
);
INSERT INTO oe.role VALUES ('ROLE_ADMIN', 'Access to applications management features');
INSERT INTO oe.role VALUES ('ROLE_USER', 'Access to data entry and views');

CREATE TABLE oe.user_role (
  user_id INTEGER NOT NULL REFERENCES "user" (id),
  role_id TEXT    NOT NULL REFERENCES role (id),
  CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id)
);
INSERT INTO oe.user_role VALUES (1, 'ROLE_USER');
INSERT INTO oe.user_role VALUES (1, 'ROLE_ADMIN');

CREATE TABLE oe.saved_query (
  query_id    SERIAL  NOT NULL PRIMARY KEY,
  query_name  TEXT    NOT NULL,
  user_id     INTEGER NOT NULL REFERENCES "user" (id),
  data_source TEXT    NOT NULL,
  query_type  TEXT    NOT NULL,
  parameters  TEXT    NOT NULL,
  CONSTRAINT saved_query_user_id_query_name_key UNIQUE (user_id, query_name)
);

CREATE OR REPLACE FUNCTION oe.getweekofyear(TIMESTAMPTZ, INTEGER DEFAULT 1)
  RETURNS DOUBLE PRECISION AS
  $BODY$
  DECLARE
    dt ALIAS FOR $1;
    oset ALIAS FOR $2;
    ms1d    INTEGER;
    ms7d    INTEGER;
    _offset INTEGER;
    D3      TIMESTAMPTZ;
    DC3     REAL;
    AWN     REAL;
    Wyr     INTEGER;
    ms1dy   REAL;
    result  DOUBLE PRECISION;
  BEGIN
    ms1d := 86400000;
-- milliseconds in a day
    ms7d := 7 * ms1d;
-- milliseconds in a week
 -- Used to compute offset based on epi week start day (0 sunday -> 6 saturday), defaults to 1
    _offset := (((abs(oset - 6)) + 5) % 7);
-- epi week start day offset

    D3 := (dt :: DATE) + _offset;
    DC3 := (EXTRACT(EPOCH FROM D3) * 1000) / ms1d;
    AWN := floor(DC3 / 7);
-- an Absolute Week Number

    Wyr := EXTRACT(YEAR FROM (TIMESTAMPTZ 'epoch' + (AWN * ms7d / 1000) * INTERVAL '1 second'));

-- first day of this year as milliseconds
    ms1dy := EXTRACT(EPOCH FROM to_date(((Wyr :: VARCHAR) || '-01-07'), 'YYYY-MM-DD') :: TIMESTAMPTZ) * 1000;

    result := (AWN - floor(ms1dy / ms7d) + 1);
    RETURN result;
  END;
  $BODY$
-- STABLE because time zones can change
LANGUAGE 'plpgsql' STABLE;

CREATE OR REPLACE FUNCTION oe.getyear(TIMESTAMPTZ, INTEGER DEFAULT 1)
  RETURNS DOUBLE PRECISION AS
  $BODY$
  DECLARE
    dt ALIAS FOR $1;
    oset ALIAS FOR $2;
    ms1d    INTEGER;
    ms7d    INTEGER;
    _offset INTEGER;
    D3      TIMESTAMPTZ;
    DC3     REAL;
    AWN     REAL;
    Wyr     DOUBLE PRECISION;
  BEGIN
    ms1d := 86400000;
-- milliseconds in a day
    ms7d := 7 * ms1d;
-- milliseconds in a week
      -- Used to compute offset based on epi week start day (0 sunday -> 6 saturday), defaults to 1
    _offset := (((abs(oset - 6)) + 5) % 7);
-- epi week start day offset
    D3 := (dt :: DATE) + _offset;
    DC3 := (EXTRACT(EPOCH FROM D3) * 1000) / ms1d;
    AWN := floor(DC3 / 7);
-- an Absolute Week Number
    Wyr := EXTRACT(YEAR FROM (TIMESTAMPtz 'epoch' + (AWN * ms7d / 1000) * INTERVAL '1 second'));
    RETURN Wyr;
  END;
  $BODY$
-- STABLE because time zones can change
LANGUAGE 'plpgsql' STABLE;

COMMIT;
