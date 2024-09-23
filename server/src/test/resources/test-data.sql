BEGIN TRANSACTION;

INSERT INTO events (eventTime, isConnected, message) VALUES ('2000-01-01 01:00:00', false, 'message 1');
INSERT INTO events (eventTime, isConnected, message) VALUES ('2000-02-02 02:00:00', true, 'message 2');
INSERT INTO events (eventTime, isConnected, message) VALUES ('2000-03-03 03:00:00', true, 'message 3');

COMMIT TRANSACTION;