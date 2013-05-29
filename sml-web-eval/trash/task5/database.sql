CREATE TABLE product (
    id integer PRIMARY KEY,
    name text,
    producer text,

    resolution_x integer,
    resolution_y integer,

    publish_date date
);


INSERT INTO product VALUES (1, 'IPhone 5', 'Apple Inc.', 'available', 1136, 640, 'white', '2012-10-21');
INSERT INTO product VALUES (2, 'Galaxy S III', 'Samsung', 'available', 1280, 720, 'black', '2012-10-21');



CREATE TABLE review(
    id int,
    person_id int REFERENCES person(id),
    product_id int REFERENCES product(id),
    text text
);

INSERT INTO review VALUES (1, 1, 1, 'Great!');
INSERT INTO review VALUES (2, 2, 2, 'I love it');




