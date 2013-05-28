

CREATE TABLE review(
    id int,
    person_id int REFERENCES person(id),
    product_id int REFERENCES product(id),
    text text
);

INSERT INTO review VALUES (1, 1, 1, 'So awesome');
INSERT INTO review VALUES (2, 2, 2, 'I love it');




