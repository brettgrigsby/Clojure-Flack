CREATE TABLE messages
(id BIGSERIAL PRIMARY KEY,
username VARCHAR(30),
channel VARCHAR(30),
message VARCHAR(420),
timestamp TIMESTAMP);
