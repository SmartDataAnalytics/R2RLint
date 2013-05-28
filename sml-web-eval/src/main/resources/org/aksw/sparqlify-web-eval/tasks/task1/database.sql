CREATE TABLE person (
    id integer PRIMARY KEY,
    first_name text,
    last_name text,
    age integer,
    mbox text,
    country text
);

INSERT INTO person VALUES (1, 'Anne' , 'Miller', 20, 'anne@example.org', 'us');
--INSERT INTO person VALUES (2, 'John' , 'Wayne', 30, 'john@example.org', 'us');
--INSERT INTO person VALUES (3, 'Frank', 'Sinatra', 40, 'frank@example.org', 'de');
