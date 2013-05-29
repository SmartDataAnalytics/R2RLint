CREATE TABLE product (
    id integer PRIMARY KEY,
    name text,
);
INSERT INTO product VALUES (1, 'IPhone');
INSERT INTO product VALUES (2, 'Galaxy');


CREATE TABLE review(
    product_id int REFERENCES product(id),
    sequence_id int,
    text text
);

INSERT INTO review VALUES (1, 1, 'Great!');
INSERT INTO review VALUES (2, 1, 'I love it');




