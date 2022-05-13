CREATE SEQUENCE currency_types_seq;


CREATE table currency_types
(
    id       INTEGER PRIMARY KEY NOT NULL DEFAULT nextval('currency_types_seq'),
    currency VARCHAR(3)          NOT NULL
);


ALTER SEQUENCE currency_types_seq
    OWNED BY currency_types.id;


