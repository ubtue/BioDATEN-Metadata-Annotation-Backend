CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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