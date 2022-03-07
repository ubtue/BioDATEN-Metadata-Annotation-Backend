CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TYPE status AS ENUM ('created', 'progress', 'finished', 'pub');

CREATE TABLE metadata
(
  mets_id UUID not null primary key,
  user_id VARCHAR NOT NULL,
  hpc_job_id BIGINT,
  metadata_status status NOT NULL,
  mets_xml TEXT NOT NULL,
  created timestamp DEFAULT now() NOT NULL,
  lastmodified timestamp DEFAULT now() NOT NULL
); 


CREATE TABLE autocomplete_mappings
(
    id UUID,
    schema INT,
    xpath VARCHAR(3000),
    ontology VARCHAR(3000),
    PRIMARY KEY (id)
);

INSERT INTO autocomplete_mappings
(
    id,
    schema,
    xpath,
    ontology
)
VALUES(
    uuid_generate_v1(),
    0,
    '/',
    '*'
);


CREATE TABLE autocomplete_schemas
(
    id UUID,
    schema VARCHAR(3000)
    PRIMARY KEY (id)
);

INSERT INTO autocomplete_schemas
(
    id,
    schema,
    ontology
)
VALUES(
    uuid_generate_v1(),
    'datacite'
);