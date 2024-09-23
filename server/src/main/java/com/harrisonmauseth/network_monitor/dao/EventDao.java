package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.model.Event;

import java.util.List;

public interface EventDao {
    /**
     * Get all events from the database.
     * @return a list of all events
     */
    List<Event> getAllEvents();

    /**
     * Get a specific event from the database.
     * @param id the id of the event you wish to retrieve
     * @return the specified event
     */
    Event getEventById(int id);

    /**
     * 
     * @param eventToCreate
     * @return
     */
    Event createEvent(Event eventToCreate);

    List<Event> createMultipleEvents(Event[] events);

    Event updateEvent(Event eventToUpdate);

    int deleteEvent(int eventId);
}
