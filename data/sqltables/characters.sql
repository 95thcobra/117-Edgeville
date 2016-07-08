-- Enable citext
CREATE EXTENSION citext;

-- Create the accounts table; this holds all the
-- username/password entries. One account can have multiple characters (e.g. rs classic, osrs, rs3)
CREATE TABLE accounts
(
  id          SERIAL PRIMARY KEY    NOT NULL,
  username    CITEXT UNIQUE         NOT NULL,
  password    VARCHAR(60)           NOT NULL, -- bcrypt password hash
  rights      SMALLINT DEFAULT 0,
  email       VARCHAR(127),
  muted       BOOLEAN  DEFAULT FALSE,
  banned      BOOLEAN  DEFAULT FALSE,
  displayname CHARACTER VARYING(12)
);

-- Create the characters table. A character must always belong to an account, which is used to login.
CREATE TABLE characters
(
  id         SERIAL PRIMARY KEY               NOT NULL,
  account_id INTEGER REFERENCES accounts (id) NOT NULL,
  service_id INTEGER  DEFAULT 0,
  x          INTEGER  DEFAULT 3086,
  z          INTEGER  DEFAULT 3494,
  level      SMALLINT DEFAULT 0,
  inventory  JSONB    DEFAULT '{"items":[]}' :: JSONB,
  equipment  JSONB    DEFAULT '{"items":[]}' :: JSONB,
  bank       JSONB    DEFAULT '{"items":[]}' :: JSONB,
  skills     JSONB    DEFAULT '{"xp": [0.0,0.0,0.0,1154.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0],"level": [1,1,1,10,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1]}' :: JSONB,
  pkp        INTEGER  DEFAULT 0,
  varps      JSONB    DEFAULT '[]'::JSONB,

  CONSTRAINT account_service UNIQUE (account_id, service_id)
);

-- Create the table holding who's online and where.
CREATE TABLE online_characters
(
  account_id   INTEGER   REFERENCES accounts (id)   UNIQUE NOT NULL, -- The account we're logged in with
  character_id INTEGER   REFERENCES characters (id) NOT NULL, -- The character we're logged in with
  service_id   INTEGER   DEFAULT 0,                           -- The service id
  world_id     INTEGER                              NOT NULL, -- The id of the world
  since        TIMESTAMP DEFAULT now(),                       -- Timestamp since when we logged in
  ip           CHARACTER VARYING(16)                          -- IP address
);