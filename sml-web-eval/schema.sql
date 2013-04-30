DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS "users" (
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


CREATE TABLE IF NOT EXISTS "submissions" (
    "id" SERIAL PRIMARY KEY,
    "ts" TIMESTAMP NOT NULL DEFAULT NOW(),
    "user_id" int NOT NULL REFERENCES users(id),
	"task_id" text NOT NULL,
	"tool_id" text NOT NULL,
	"mapping" text NOT NULL,
	"is_solution" BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE IF NOT EXISTS "scores" (
	"user_id" int NOT NULL REFERENCES "users"(id)
);
