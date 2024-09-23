package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.model.Event;

import java.util.List;

public class JdbcEventDao implements EventDao {
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
