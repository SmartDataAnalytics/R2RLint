/*
DROP TABLE "node_user_tags";
DROP TABLE "way_user_tags";
DROP TABLE "relation_user_tags";
*/

CREATE TABLE "node_user_tags" (
    "user_id" BIGINT NOT NULL, -- REFERENCES "users"("id"),
	"node_id" BIGINT NOT NULL,
	"k" text NOT NULL,
	"v" text NOT NULL
);
CREATE INDEX "idx_node_user_tags_user_id" ON "node_user_tags"("user_id");
CREATE INDEX "idx_node_user_tags_node_id" ON "node_user_tags"("node_id");
CREATE INDEX "idx_node_user_tags_k" ON "node_user_tags"("k");
CREATE INDEX "idx_node_user_tags_v" ON "node_user_tags"("v");


CREATE TABLE "way_user_tags" (
    "user_id" BIGINT NOT NULL, -- REFERENCES "users"("id"),
	"way_id" BIGINT NOT NULL,
	"k" text NOT NULL,
	"v" text NOT NULL
);
CREATE INDEX "idx_way_user_tags_user_id" ON "way_user_tags"("user_id");
CREATE INDEX "idx_way_user_tags_way_id" ON "way_user_tags"("way_id");
CREATE INDEX "idx_way_user_tags_k" ON "way_user_tags"("k");
CREATE INDEX "idx_way_user_tags_v" ON "way_user_tags"("v");

CREATE TABLE "relation_user_tags" (
    "user_id" BIGINT NOT NULL, -- REFERENCES "users"("id"),
	"relation_id" BIGINT NOT NULL,
	"k" text NOT NULL,
	"v" text NOT NULL
);
CREATE INDEX "idx_relation_user_tags_user_id" ON "relation_user_tags"("user_id");
CREATE INDEX "idx_relation_user_tags_relation_id" ON "relation_user_tags"("relation_id");
CREATE INDEX "idx_relation_user_tags_k" ON "relation_user_tags"("k");
CREATE INDEX "idx_relation_user_tags_v" ON "relation_user_tags"("v");
