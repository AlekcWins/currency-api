CREATE SEQUENCE curs_request_seq;


CREATE table curs_request
(
    id             INTEGER PRIMARY KEY NOT NULL DEFAULT nextval('curs_request_seq'),
    curs_date      DATE,
    request_date   TIMESTAMP,
    correlation_id VARCHAR,
    status         varchar(10)
);


ALTER SEQUENCE curs_request_seq
    OWNED BY curs_request.id;
