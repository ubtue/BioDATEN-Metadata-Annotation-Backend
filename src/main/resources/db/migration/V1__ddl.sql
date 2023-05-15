CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TYPE status AS ENUM ('created', 'progress', 'finished', 'pub');

CREATE TABLE metadata
(
  mets_id UUID not null primary key,
  user_id VARCHAR NOT NULL,
  hpc_job_id BIGINT,
  metadata_status status NOT NULL,
  mets_xml XML NOT NULL,
  created timestamp DEFAULT now() NOT NULL,
  lastmodified timestamp DEFAULT now() NOT NULL
); 


CREATE TABLE autocomplete_mappings
(
    id UUID primary key,
    schema VARCHAR(3000),
    xpath VARCHAR(3000),
    ontology VARCHAR(3000),
    active BOOLEAN
);


CREATE TABLE autocomplete_schemas
(
    id UUID primary key,
    schema VARCHAR(3000),
    file_name VARCHAR(3000),
    tab_name VARCHAR(3000),
    active BOOLEAN
);


CREATE TABLE render_options
(
    id UUID primary key,
    schema VARCHAR(3000),
    xpath VARCHAR(3000),
    label TEXT,
    placeholder TEXT,
    prefilled TEXT,
    readonly BOOLEAN,
    hide BOOLEAN,
    active BOOLEAN
);


CREATE TABLE user_information
(
    id UUID primary key,
    user_id VARCHAR(3000) NOT NULL,
    fdat_key VARCHAR(3000)
);