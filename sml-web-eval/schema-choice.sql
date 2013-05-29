-- Score sheet schema


CREATE TABLE "item" (
    "id" INT PRIMARY KEY,
    "text" text NOT NULL,
);


-- An item bank is a set of items
-- E.g. one set for R2RML, and one for SML
CREATE TABLE "item_bank" (
    "id" INT PRIMARY KEY,
    "label" text NOT NULL
);

-- Relation of items to item banks
CREATE TABLE "item_bank_item"(
    "item_bank_id" INT REFERENCES "item_bank"("id"),
    "item_id" INT REFERENCES "item"("id"),
    "sequence_id" SERIAL NOT NULL,
    PRIMARY KEY("item_bank_id", "item_id")
);

CREATE TABLE "option" (
	"id" INT PRIMARY KEY,
    "text" text NOT NULL	
);

-- Predefined answers of items
CREATE TABLE "item_option" (
    "item_id" INT REFERENCES "item"("id"),
    "option_id" INT REFERENCES "option"("id"),
    "sequence_id" SERIAL NOT NULL,

    PRIMARY KEY("item_id", "option_id")
);

CREATE TABLE "user_option" (
    "user_id" INT REFERENCES "user"("id"),
    "item_id" INT REFERENCES "item"("id"),
    "option_id" INT REFERENCES "option"("id")
);



INSERT INTO "option"("id", "text") VALUES (1, '1');
INSERT INTO "option"("id", "text") VALUES (2, '2');
INSERT INTO "option"("id", "text") VALUES (3, '3');
INSERT INTO "option"("id", "text") VALUES (4, '4');
INSERT INTO "option"("id", "text") VALUES (5, '5');

INSERT INTO "item"("id", "text") VALUES (20, 'What is your experience with r2rml?');
INSERT INTO "item_option"("item_id", "option_id", "sequence_id") VALUES (20, 1, 1);
INSERT INTO "item_option"("item_id", "option_id", "sequence_id") VALUES (20, 2, 2);
INSERT INTO "item_option"("item_id", "option_id", "sequence_id") VALUES (20, 3, 3);
INSERT INTO "item_option"("item_id", "option_id", "sequence_id") VALUES (20, 4, 4);
INSERT INTO "item_option"("item_id", "option_id", "sequence_id") VALUES (20, 5, 5);



INSERT INTO "item_bank" VALUES (1, 'r2rml');
INSERT INTO "item_bank" VALUES (2, 'sml');

INSERT INTO "item_bank_item"("item_bank_id", "item_id", "sequence_id") VALUES (2, 20, 1);


