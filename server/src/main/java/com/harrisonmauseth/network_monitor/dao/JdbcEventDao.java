package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcEventDao implements EventDao {
    private JdbcTemplate jdbcTemplate;

    public JdbcEventDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT eventId, eventTime, isConnected, message FROM events ORDER BY eventTime ASC;";
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
    public Event getEventById(int id) {
        Event event = new Event();
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
            eventToCreate.setEventTime(LocalDateTime.now());
        }
        String sql = "INSERT INTO events (eventTime, isConnected, message) VALUES (?, ?, ?) RETURNING eventId;";
        try {
            int eventId = jdbcTemplate.queryForObject(
                    sql,
                    int.class,
                    eventToCreate.getEventTime(),
                    eventToCreate.isConnected(),
                    eventToCreate.getMessage()
            );
            event = getEventById(eventId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to database.");
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation");
        }
        return event;
    }

    @Override
    public List<Event> createMultipleEvents(Event[] events) {
        return null;
    }

    @Override
    public Event updateEvent(Event eventToUpdate) {
        return null;
    }

    @Override
    public int deleteEvent(int eventId) {
        return 0;
    }

    private Event mapRowToEvent(SqlRowSet rowSet) {
        Event event = new Event();
        event.setEventId(rowSet.getInt("eventId"));
        if (rowSet.getTimestamp("eventTime") != null) {
            event.setEventTime(rowSet.getTimestamp("eventTime").toLocalDateTime());
        }
        event.setConnected(rowSet.getBoolean("isConnected"));
        if (rowSet.getString("message") != null) {
            event.setMessage(rowSet.getString("message"));
        }
        return event;
    }
}
