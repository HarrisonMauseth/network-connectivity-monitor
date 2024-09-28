package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class JdbcEventDao implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcEventDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT eventId, eventTime, isConnected, message FROM events ORDER BY eventTime DESC;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                events.add(mapRowToEvent(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        }
        return events;
    }

    @Override
    public List<Event> getAllEventsLimited(int limit) {
        List<Event> events = new ArrayList<>();
        if (limit <= 0) {
            return getAllEvents();
        }
        String sql = "SELECT eventId, eventTime, isConnected, message FROM events ORDER BY eventTime DESC LIMIT ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, limit);
            while (results.next()) {
                events.add(mapRowToEvent(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        }
        return events;
    }

    @Override
    public List<Event> getDisconnectedEvents(int limit) {
        List<Event> events = new ArrayList<>();
        if (limit <= 0) {
            String sql = "SELECT eventId, eventTime, isConnected, message FROM events WHERE isConnected = false ORDER BY eventTime DESC;";
            try {
                SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
                while (results.next()) {
                    events.add(mapRowToEvent(results));
                }
            } catch (CannotGetJdbcConnectionException e) {
                throw new DaoException("Unable to connect to database.");
            }
        } else {
            String sql = "SELECT eventId, eventTime, isConnected, message FROM events WHERE isConnected = false ORDER BY eventTime DESC LIMIT ?;";
            try {
                SqlRowSet results = jdbcTemplate.queryForRowSet(sql, limit);
                while (results.next()) {
                    events.add(mapRowToEvent(results));
                }
            } catch (CannotGetJdbcConnectionException e) {
                throw new DaoException("Unable to connect to database.");
            }
        }
        return events;
    }

    @Override
    public Event getEventById(int id) {
        Event event = null;
        String sql = "SELECT eventId, eventTime, isConnected, message FROM events WHERE eventId = ?;";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
            if (result.next()) {
                event = mapRowToEvent(result);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        }
        return event;
    }

    @Override
    public Event createEvent(Event eventToCreate) {
        Event event;
        if (eventToCreate.getEventTime() == null) {
            eventToCreate.setEventTime(LocalDateTime.now(ZoneId.of("UTC")));
        }
        String sql = "INSERT INTO events (eventTime, isConnected, message) VALUES (?, ?, ?) RETURNING eventId;";
        try {
            Integer eventId = jdbcTemplate.queryForObject(
                    sql,
                    int.class,
                    eventToCreate.getEventTime(),
                    eventToCreate.isConnected(),
                    eventToCreate.getMessage()
            );
            if (eventId != null) {
                event = getEventById(eventId);
            } else {
                throw new DaoException("Unexpected error occurred while trying to create.");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation");
        }
        return event;
    }

    @Override
    public List<Event> createMultipleEvents(Event[] events) {
        List<Event> createdEvents = new ArrayList<>();
        for (Event event : events) {
            createdEvents.add(createEvent(event));
        }
        return createdEvents;
    }

    @Override
    public Event updateEvent(Event eventToUpdate) {
        Event updatedEvent;
        String sql = "UPDATE events SET eventTime = ?, isConnected = ?, message = ? WHERE eventId = ?;";
        try {
            int numberOfRowsUpdated = jdbcTemplate.update(
                    sql,
                    eventToUpdate.getEventTime(),
                    eventToUpdate.isConnected(),
                    eventToUpdate.getMessage(),
                    eventToUpdate.getEventId()
            );
            if (numberOfRowsUpdated == 0) {
                throw new DaoException("Zero rows affected, expected at least one.");
            } else updatedEvent = getEventById(eventToUpdate.getEventId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation");
        }
        return updatedEvent;
    }

    @Override
    public int deleteEvent(int eventId) {
        int numberOfRowsDeleted;
        String sql = "DELETE FROM events WHERE eventId = ?;";
        try {
            numberOfRowsDeleted = jdbcTemplate.update(sql, eventId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation");
        }
        return numberOfRowsDeleted;
    }

    private Event mapRowToEvent(SqlRowSet rowSet) {
        Event event = new Event();
        event.setEventId(rowSet.getInt("eventId"));
        if (rowSet.getTimestamp("eventTime") != null) {
            event.setEventTime(Objects.requireNonNull(rowSet.getTimestamp("eventTime")).toLocalDateTime());
        }
        event.setConnected(rowSet.getBoolean("isConnected"));
        if (rowSet.getString("message") != null) {
            event.setMessage(rowSet.getString("message"));
        }
        return event;
    }
}
