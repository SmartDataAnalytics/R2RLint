CREATE TABLE people(
    id integer PRIMARY KEY,
    name text
);
INSERT INTO people VALUES (1, 'Anne');
INSERT INTO people VALUES (2, 'John');


CREATE TABLE product (
    id integer PRIMARY KEY,
    name text,
);
INSERT INTO product VALUES (1, 'IPhone');
INSERT INTO product VALUES (2, 'Galaxy');

CREATE TABLE review(
    id int,
    person_id int REFERENCES person(id),
    product_id int REFERENCES product(id),
    text text
);

INSERT INTO review VALUES (1, 1, 1, 'Great!');
INSERT INTO review VALUES (2, 2, 2, 'I love it');




