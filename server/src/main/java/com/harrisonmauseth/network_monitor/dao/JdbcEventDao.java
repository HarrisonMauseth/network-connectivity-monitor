package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

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
        return null;
    }

    @Override
    public Event createEvent(Event eventToCreate) {
        return null;
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
