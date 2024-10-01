BEGIN TRANSACTION;

INSERT INTO events (eventTime, isConnectedToWifi, isConnectedToInternet, message) VALUES ('2000-01-01 01:00:00', false, false, 'message 1');
INSERT INTO events (eventTime, isConnectedToWifi, isConnectedToInternet, message) VALUES ('2000-02-02 02:00:00', true, false, 'message 2');
INSERT INTO events (eventTime, isConnectedToWifi, isConnectedToInternet, message) VALUES ('2000-03-03 03:00:00', true, true, 'message 3');
INSERT INTO events (eventTime, isConnectedToWifi, isConnectedToInternet, message) VALUES ('2000-04-04 04:00:00', false, true, 'message 4');

COMMIT TRANSACTION;