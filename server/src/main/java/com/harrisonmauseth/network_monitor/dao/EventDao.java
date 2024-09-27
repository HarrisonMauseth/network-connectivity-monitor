package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.model.Event;

import java.util.List;

public interface EventDao {

    /**
     * Get all events from the database, sorted with the most recent time first.
     *
     * @return a list of all events
     */
    List<Event> getAllEvents();

    /**
     * Get all events from the database, limited by the number passed in, sorted with the most recent time first.
     *
     * @param limit the number of results you wish to see
     * @return a list of all events
     */
    List<Event> getAllEventsLimited(int limit);

    /**
     * Get a specific event from the database.
     *
     * @param id the id of the event you wish to retrieve
     * @return the specified event
     */
    Event getEventById(int id);

    /**
     * Create a new event.
     *
     * @param eventToCreate the event you wish to create
     * @return the created event that returns from the database
     */
    Event createEvent(Event eventToCreate);

    /**
     * Create multiple events from an array of events.
     *
     * @param events an array of events to create
     * @return a list of all events that have been created
     */
    List<Event> createMultipleEvents(Event[] events);

    /**
     * Update an event.
     *
     * @param eventToUpdate the updated event you wish to update in the database
     * @return the updated event that returns from the database
     */
    Event updateEvent(Event eventToUpdate);

    /**
     * Delete an event.
     *
     * @param eventId the id of the event to delete
     * @return the number of lines that were deleted
     */
    int deleteEvent(int eventId);
}
