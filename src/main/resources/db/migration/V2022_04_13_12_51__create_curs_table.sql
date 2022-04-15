CREATE table curs_data
(
    id        SERIAL PRIMARY KEY NOT NULL,
    currency  VARCHAR(3),
    curs      NUMERIC(5, 2),
    curs_date DATE
);