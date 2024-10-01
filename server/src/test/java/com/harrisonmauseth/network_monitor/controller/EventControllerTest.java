package com.harrisonmauseth.network_monitor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harrisonmauseth.network_monitor.dao.EventDao;
import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@WebMvcTest(EventController.class)
public class EventControllerTest {

    public static final Event EVENT_1 = new Event(1, LocalDateTime.parse("2000-01-01T01:00:00"), false, false, "message 1");
    public static final Event EVENT_2 = new Event(2, LocalDateTime.parse("2000-02-02T02:00:00"), true, false, "message 2");
    public static final Event EVENT_3 = new Event(3, LocalDateTime.parse("2000-03-03T03:00:00"), true, true, "message 3");
    public static final Event EVENT_4 = new Event(4, LocalDateTime.parse("2000-04-04T04:00:00"), false, true, "message 4");
    private final String BASE_ENDPOINT = "/api/events";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventDao eventDao;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getEvents_returns_status_code_200_when_events_exist() throws Exception {
        List<Event> mockEvents = Arrays.asList(EVENT_4, EVENT_3, EVENT_2, EVENT_1);

        when(eventDao.getAllEventsLimited(anyInt())).thenReturn(mockEvents);

        mockMvc.perform(get(BASE_ENDPOINT)
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonArray(mockEvents)));
    }

    @Test
    public void getEvents_returns_empty_array_when_no_events_exist() throws Exception {
        when(eventDao.getAllEventsLimited(anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void getDisconnectedEvents_returns_status_code_200_when_events_exist() throws Exception {
        List<Event> mockEvents = Arrays.asList(EVENT_4, EVENT_1);

        when(eventDao.getDisconnectedEvents(anyInt())).thenReturn(mockEvents);

        mockMvc.perform(get(BASE_ENDPOINT + "/failed")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonArray(mockEvents)));
    }

    @Test
    public void getDisconnectedEvents_returns_empty_array_when_no_events_exist() throws Exception {
        when(eventDao.getDisconnectedEvents(anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT + "/failed")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void logEvent_returns_201_when_created() throws Exception {
        when(eventDao.createEvent(any(Event.class))).thenReturn(EVENT_1);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(toJson(EVENT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(toJson(EVENT_1)));
    }

    @Test
    public void logEvent_returns_status_code_400_when_failing_to_create_event() throws Exception {
        when(eventDao.createEvent(any(Event.class))).thenReturn(null);

        mockMvc.perform(post(BASE_ENDPOINT)
                        .content(toJson(EVENT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void logMultipleEvents_returns_201_when_events_are_created() throws Exception {
        List<Event> createdEvents = Arrays.asList(EVENT_4, EVENT_3, EVENT_2, EVENT_1);

        when(eventDao.createMultipleEvents(any(Event[].class))).thenReturn(createdEvents);

        mockMvc.perform(post(BASE_ENDPOINT + "/multiple")
                        .content(toJsonArray(createdEvents))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(toJsonArray(createdEvents)));
    }

    @Test
    public void logMultipleEvents_returns_status_code_400_when_creation_fails() throws Exception {
        List<Event> eventsToCreate = Arrays.asList(EVENT_4, EVENT_3, EVENT_2, EVENT_1);

        when(eventDao.createMultipleEvents(any(Event[].class))).thenReturn(null);

        mockMvc.perform(post(BASE_ENDPOINT + "/multiple")
                        .content(toJsonArray(eventsToCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateEvent_returns_status_code_200_when_event_is_updated() throws Exception {
        when(eventDao.updateEvent(any(Event.class))).thenReturn(EVENT_1);

        mockMvc.perform(put(BASE_ENDPOINT + "/1")
                        .content(toJson(EVENT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(EVENT_1)));
    }

    @Test
    public void updateEvent_returns_status_code_400_when_unable_to_retrieve_created_event() throws Exception {
        when(eventDao.updateEvent(any(Event.class))).thenThrow(new DaoException("Zero rows affected, expected at least one."));

        mockMvc.perform(put(BASE_ENDPOINT + "/1")
                        .content(toJson(EVENT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteLog_returns_status_code_204_when_event_is_deleted() throws Exception {
        when(eventDao.deleteEvent(anyInt())).thenReturn(1);

        mockMvc.perform(delete(BASE_ENDPOINT + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteLog_returns_status_code_404_when_event_is_not_found() throws Exception {
        when(eventDao.deleteEvent(anyInt())).thenReturn(0);

        mockMvc.perform(delete(BASE_ENDPOINT + "/1"))
                .andExpect(status().isNotFound());
    }

    private String toJsonArray(List<Event> events) throws JsonProcessingException {
        return mapper.writeValueAsString(events);
    }

    private String toJson(Event event) throws JsonProcessingException {
        return mapper.writeValueAsString(event);
    }

}
