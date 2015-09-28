--name:save-message!
--saves message to database
INSERT INTO messages
(username, channel, message, timestamp)
VALUES (:username, :channel, :message, :timestamp)

--name:get-all-messages
--selects all messages
SELECT * FROM messages

--name:get-message-by-username
--selects all messages by username
SELECT * FROM messages WHERE username = :username

--name:get-recent-messages-by-channel
--selects 30 recent messages by channel
SELECT * FROM messages WHERE channel = :channel ORDER BY timestamp DESC LIMIT 15

--name:get-messages-by-channel-less-than-id
--selects 30 messages by channel less than given id
SELECT * FROM messages WHERE channel = :channel AND id < :id ORDER BY timestamp DESC LIMIT 15
