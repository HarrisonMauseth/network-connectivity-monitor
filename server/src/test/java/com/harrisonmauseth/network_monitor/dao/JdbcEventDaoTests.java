package com.harrisonmauseth.network_monitor.dao;

import com.harrisonmauseth.network_monitor.model.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcEventDaoTests extends BaseDaoTests {
    public static final Event EVENT_1 = new Event(1, LocalDateTime.parse("2000-01-01T01:00:00"), false, "message 1");
    public static final Event EVENT_2 = new Event(2, LocalDateTime.parse("2000-02-02T02:00:00"), true, "message 2");
    public static final Event EVENT_3 = new Event(3, LocalDateTime.parse("2000-03-03T03:00:00"), true, "message 3");
    List<Event> events = new ArrayList<>();
    private JdbcEventDao dao;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        dao = new JdbcEventDao(jdbcTemplate);
    }

    @Test
    public void getAllEvents_returns_all_events_in_correct_order() {
        events = dao.getAllEvents();
        Assert.assertNotNull("getAllEvents() returned null instead of a list", events);
        Assert.assertEquals("getAllEvents() did not return correct number of events", 3, events.size());
        assertEventsMatch("getAllEvents() returned events in incorrect order", EVENT_3, events.get(0));
        assertEventsMatch("getAllEvents() returned events in incorrect order", EVENT_2, events.get(1));
        assertEventsMatch("getAllEvents() returned events in incorrect order", EVENT_1, events.get(2));
    }

    @Test
    public void getAllEventsLimited_returns_correct_number_of_events_in_correct_order() {
        events = dao.getAllEventsLimited(2);
        Assert.assertNotNull("getAllEventsLimited(2) returned null instead of a list", events);
        Assert.assertEquals("getAllEventsLimited(2) did not return correct number of events", 2, events.size());
        assertEventsMatch("getAllEventsLimited(2) returned events in incorrect order", EVENT_3, events.get(0));
        assertEventsMatch("getAllEventsLimited(2) returned events in incorrect order", EVENT_2, events.get(1));

        events = dao.getAllEventsLimited(1);
        Assert.assertNotNull("getAllEventsLimited(1) returned null instead of a list", events);
        Assert.assertEquals("getAllEventsLimited(1) did not return correct number of events", 1, events.size());

        events = dao.getAllEventsLimited(0);
        Assert.assertNotNull("getAllEventsLimited(0) returned null instead of a list", events);
        Assert.assertEquals("getAllEventsLimited(0) did not return all events", 3, events.size());

        events = dao.getAllEventsLimited(-1);
        Assert.assertNotNull("getAllEventsLimited(-1) returned null instead of a list", events);
        Assert.assertEquals("getAllEventsLimited(-1) did not return all events", 3, events.size());
    }

    @Test
    public void getEventById_returns_correct_event() {
        Event event1 = dao.getEventById(EVENT_1.getEventId());
        Assert.assertNotNull("getEventById() returned null instead of an event", event1);
        assertEventsMatch("getEventById(1)", EVENT_1, event1);

        Event event2 = dao.getEventById(EVENT_2.getEventId());
        Assert.assertNotNull("getEventById() returned null instead of an event", event2);
        assertEventsMatch("getEventById(2)", EVENT_2, event2);

        Event event3 = dao.getEventById(EVENT_3.getEventId());
        Assert.assertNotNull("getEventById() returned null instead of an event.", event3);
        assertEventsMatch("getEventById(3)", EVENT_3, event3);
    }

    @Test
    public void createEvent_createsEvent() {
        Event eventToCreate = new Event(0, LocalDateTime.parse("2000-05-05T05:00:00"), false, "message 4");

        Event createdEvent = dao.createEvent(eventToCreate);
        Assert.assertNotNull("createEvent() returned null", createdEvent);
        Assert.assertTrue("createEvent() did not return the eventId of the created event.", createdEvent.getEventId() > 0);

        eventToCreate.setEventId(createdEvent.getEventId());
        assertEventsMatch("createEvent()", eventToCreate, createdEvent);

        Event retrievedEvent = dao.getEventById(createdEvent.getEventId());
        assertEventsMatch("created event did not store properly within the database:", createdEvent, retrievedEvent);
    }

    @Test
    public void createMultipleEvents_creates_multiple_events() {
        Event newEvent1 = new Event(4, LocalDateTime.parse("2004-04-04T04:44:44"), false, "message 4");
        Event newEvent2 = new Event(5, LocalDateTime.parse("2005-05-05T05:55:55"), false, "message 5");
        Event newEvent3 = new Event(6, LocalDateTime.parse("2006-06-06T06:06:06"), false, "message 6");
        Event eventsToCreate[] = new Event[]{newEvent1, newEvent2, newEvent3};

        List<Event> createdEvents = dao.createMultipleEvents(eventsToCreate);
        Assert.assertNotNull("createMultipleEvents() returned null instead of a list", createdEvents);
        Assert.assertEquals("CreateMultipleEvents() did not return the correct number of events", 3, createdEvents.size());
    }

    @Test
    public void updateEvent_updates_event() {
        Event eventToUpdate = new Event(1, LocalDateTime.parse("1999-01-01T01:00:00"), true, "updated event");

        Event updatedEvent = dao.updateEvent(eventToUpdate);
        Assert.assertNotNull("updateEvent() returned null instead of updated event", updatedEvent);
        assertEventsMatch("updateEvent() returned incorrect or incomplete event:", eventToUpdate, updatedEvent);

        Event retrievedEvent = dao.getEventById(updatedEvent.getEventId());
        assertEventsMatch("updateEvent() updated the event but failed to save in the database:", updatedEvent, retrievedEvent);

        Event unmodifiedEvent = dao.getEventById(EVENT_2.getEventId());
        assertEventsMatch("updateEvent() updated either the wrong or multiple events:", EVENT_2, unmodifiedEvent);
    }

    @Test
    public void deleteEvent_deletes_event() {
        int rowsDeleted = dao.deleteEvent(EVENT_3.getEventId());
        Assert.assertEquals("deleteEvent(3) did not delete the correct number of rows.", 1, rowsDeleted);
        Event retrievedEvent = dao.getEventById(EVENT_3.getEventId());
        Assert.assertNull("deleteEvent(3) did not remove the event from the database.", retrievedEvent);
    }

    private void assertEventsMatch(String methodInvoked, Event expected, Event actual) {
        Assert.assertEquals(methodInvoked + " eventIds do not match.", expected.getEventId(), actual.getEventId());
        Assert.assertEquals(methodInvoked + " eventTimes do not match.", expected.getEventTime(), actual.getEventTime());
        Assert.assertEquals(methodInvoked + " isConnected does not match.", expected.isConnected(), actual.isConnected());
        Assert.assertEquals(methodInvoked + " messages do not match", expected.getMessage(), actual.getMessage());
    }

}
