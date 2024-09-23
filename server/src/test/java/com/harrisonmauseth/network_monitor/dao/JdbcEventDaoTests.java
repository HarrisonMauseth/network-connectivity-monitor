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
    public void getAllEvents_returns_correct_number_of_events() {
        events = dao.getAllEvents();
        Assert.assertNotNull("getAllEvents() returned null instead of an empty list", events);
        Assert.assertEquals("getAllEvents() did not return correct number of events", 3, events.size());
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

    private void assertEventsMatch(String methodInvoked, Event expected, Event actual) {
        Assert.assertEquals(methodInvoked + " eventIds do not match.", expected.getEventId(), actual.getEventId());
        Assert.assertEquals(methodInvoked + " eventTimes do not match.", expected.getEventTime(), actual.getEventTime());
        Assert.assertEquals(methodInvoked + " isConnected does not match.", expected.isConnected(), actual.isConnected());
        Assert.assertEquals(methodInvoked + " messages do not match", expected.getMessage(), actual.getMessage());
    }

}
