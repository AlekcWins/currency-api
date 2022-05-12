ALTER TABLE curs_data
    ADD COLUMN currency_type_id INTEGER;


UPDATE curs_data
SET currency_type_id = 1
WHERE currency = 'EUR';

UPDATE curs_data
SET currency_type_id = 2
WHERE currency = 'USD';


UPDATE curs_data
SET currency_type_id = 3
WHERE currency = 'RUB';

insert into currency_types(currency)
values ('EUR'),
       ('USD'),
       ('RUB');

ALTER TABLE curs_data
    ADD FOREIGN KEY (currency_type_id)
        REFERENCES currency_types (id);


ALTER TABLE curs_data
    DROP COLUMN currency;