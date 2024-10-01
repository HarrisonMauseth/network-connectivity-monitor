START TRANSACTION;

DROP TABLE IF EXISTS events;

CREATE TABLE events (
    eventId SERIAL,
    eventTime TIMESTAMP NOT NULL DEFAULT NOW(),
    isConnectedToWifi BOOLEAN NOT NULL DEFAULT false,
    isConnectedToInternet BOOLEAN NOT NULL DEFAULT false,
    message text NULL,
    CONSTRAINT PK_events PRIMARY KEY (eventId)
);

COMMIT;