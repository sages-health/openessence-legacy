/**
 * Set up a (PostgreSQL) database for OpenEssence.
 *
 * Note that there is some flexibility in terms of what objects OE expects a
 * DB to have. For example, you can call your user table whatever you want, as
 * long as it's mapped correctly in the ORM layer. However, try to stick to the
 * conventions defined here if possible.
 */
BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis SCHEMA public;
CREATE EXTENSION IF NOT EXISTS postgis_topology SCHEMA topology;

--if not exists for schemas is only in Postgres 9.3 devel
--create schema if not exists oe;

ALTER ROLE postgres SET search_path = oe, public;

/*****************************************************************************
 * Objects required by OpenESSENCE
 *****************************************************************************/
CREATE TABLE "user" (
  id                      SERIAL PRIMARY KEY,
  name                    TEXT    NOT NULL UNIQUE,
  password                TEXT    NOT NULL,
  enabled                 BOOLEAN NOT NULL DEFAULT FALSE,
  non_expired             BOOLEAN NOT NULL DEFAULT TRUE,
  credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
  account_non_locked      BOOLEAN NOT NULL DEFAULT TRUE
);

-- add admin/admin login
INSERT INTO "user" (name, password, enabled, non_expired, credentials_non_expired, account_non_locked)
  VALUES ('admin', '805c31ff4a51bb8641c1fd00ce3eccb680e49be3', TRUE, TRUE, TRUE, TRUE);

CREATE TABLE role (
  id          TEXT NOT NULL PRIMARY KEY,
  description TEXT
);
INSERT INTO role VALUES ('ROLE_ADMIN', 'Access to applications management features');
INSERT INTO role VALUES ('ROLE_USER', 'Access to data entry and views');

CREATE TABLE user_role (
  user_id INTEGER NOT NULL REFERENCES "user" (id),
  role_id TEXT    NOT NULL REFERENCES role (id),
  CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id)
);
INSERT INTO user_role VALUES (1, 'ROLE_USER');
INSERT INTO user_role VALUES (1, 'ROLE_ADMIN');

CREATE TABLE saved_query (
  query_id    SERIAL  NOT NULL PRIMARY KEY,
  query_name  TEXT    NOT NULL,
  user_id     INTEGER NOT NULL REFERENCES "user" (id),
  data_source TEXT    NOT NULL,
  parameters  TEXT    NOT NULL,
  CONSTRAINT saved_query_user_id_query_name_key UNIQUE (user_id, query_name)
);

CREATE OR REPLACE FUNCTION getweekofyear(TIMESTAMP WITHOUT TIME ZONE, INTEGER DEFAULT 1)
  RETURNS DOUBLE PRECISION AS
  $BODY$
  DECLARE
    dt ALIAS FOR $1;
    oset ALIAS FOR $2;
    ms1d    INTEGER;
    ms7d    INTEGER;
    _offset INTEGER;
    D3      TIMESTAMP WITHOUT TIME ZONE;
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
    Wyr := EXTRACT(YEAR FROM (TIMESTAMP WITHOUT TIME ZONE 'epoch' + (AWN * ms7d / 1000) * INTERVAL '1 second'));

-- first day of this year as milliseconds
    ms1dy := EXTRACT(EPOCH FROM to_date(((Wyr :: VARCHAR) || '-01-07'), 'YYYY-MM-DD') :: TIMESTAMPTZ) * 1000;

    result := (AWN - floor(ms1dy / ms7d) + 1);
    RETURN result;

  END;
  $BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION getyear(TIMESTAMP WITHOUT TIME ZONE, INTEGER DEFAULT 1)
  RETURNS DOUBLE PRECISION AS
  $BODY$
  DECLARE
    dt ALIAS FOR $1;
    oset ALIAS FOR $2;
    ms1d    INTEGER;
    ms7d    INTEGER;
    _offset INTEGER;
    D3      TIMESTAMP WITHOUT TIME ZONE;
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
    Wyr := EXTRACT(YEAR FROM (TIMESTAMP WITHOUT TIME ZONE 'epoch' + (AWN * ms7d / 1000) * INTERVAL '1 second'));
    RETURN Wyr;
  END;
  $BODY$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION getstartdateofweek(TIMESTAMP WITHOUT TIME ZONE, INTEGER DEFAULT 1)
  RETURNS TIMESTAMP WITHOUT TIME ZONE AS
  $BODY$
  DECLARE
    dt ALIAS FOR $1;
    oset ALIAS FOR $2;
    t INTEGER;
  BEGIN
    t := (abs(Extract(DOW FROM (dt :: DATE - oset)) - 6) - 6) :: INTEGER % 7;
    RETURN dt :: DATE + t;
  END;
  $BODY$
LANGUAGE 'plpgsql' VOLATILE;

COMMIT;
