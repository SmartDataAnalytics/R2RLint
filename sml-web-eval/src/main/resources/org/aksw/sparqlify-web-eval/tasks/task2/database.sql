CREATE TABLE product (
    id integer PRIMARY KEY,
    name text,
    producer text,

    comment text,

    resolution_x integer,
    resolution_y integer,
    color text,

    publish_date date
);


INSERT INTO product VALUES (1, 'IPhone 5', 'Apple Inc.', 'available', 1136, 640, 'white', '2012-10-21');
INSERT INTO product VALUES (2, 'Galaxy S III', 'Samsung', 'available', 1280, 720, 'black', '2012-10-21');


