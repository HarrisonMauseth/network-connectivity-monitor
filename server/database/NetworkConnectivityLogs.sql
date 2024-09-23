START TRANSACTION;

DROP TABLE IF EXISTS events;

CREATE TABLE events (
    eventId SERIAL,
    eventTime TIMESTAMP NOT NULL DEFAULT NOW(),
    isConnected BOOLEAN NOT NULL DEFAULT false,
    message text NULL,
    CONSTRAINT PK_events PRIMARY KEY (eventId)
);

COMMIT;