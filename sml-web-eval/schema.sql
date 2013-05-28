DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS "user" (
    "id" SERIAL PRIMARY KEY,
    "ts" TIMESTAMP NOT NULL DEFAULT NOW(),
    "name" text NOT NULL,
    "email" text NOT NULL,
    "password" text NOT NULL
);

CREATE TABLE IF NOT EXISTS "eval_order" (
	-- "id" SERIAL PRIMARY KEY,
	"user_id" INT REFERENCES "users"("id"), 
	"sequence_id" INT NOT NULL, 
	"name" text NOT NULL,
	"is_finished" boolean NOT NULL DEFAULT FALSE,
	PRIMARY KEY("user_id", "sequence_id")
);


CREATE TABLE IF NOT EXISTS "submission" (
    "id" SERIAL PRIMARY KEY,
    "ts" TIMESTAMP NOT NULL DEFAULT NOW(),
    "user_id" int NOT NULL REFERENCES "user"(id),
    "request_addr" text NOT NULL,
	"task_id" text NOT NULL,
	"tool_id" text NOT NULL,
	"mapping" text NOT NULL,
	"is_solution" BOOLEAN NOT NULL DEFAULT FALSE,
	"is_working" BOOLEAN NOT NULL DEFAULT FALSE
);




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


