package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.model.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcEventDao implements EventDao {
    private JdbcTemplate jdbcTemplate;

    public JdbcEventDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getAllEvents() {
        return null;
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
}
