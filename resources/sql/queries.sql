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

--name:get-messages-by-channel
--selects all messages by channel
SELECT * FROM messages WHERE channel = :channel
