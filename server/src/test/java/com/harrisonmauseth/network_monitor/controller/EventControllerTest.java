package com.harrisonmauseth.network_monitor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harrisonmauseth.network_monitor.dao.BaseDaoTests;
import com.harrisonmauseth.network_monitor.dao.EventDao;
import com.harrisonmauseth.network_monitor.dao.JdbcEventDao;
import com.harrisonmauseth.network_monitor.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@WebMvcTest(EventController.class)
public class EventControllerTest {

    private final String BASE_ENDPOINT = "/api/events";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventDao eventDao;
    @Autowired
    private ObjectMapper mapper;


    @Test
    public void testGetEvents_returns_status_code_200_when_events_exist() throws Exception {
        Event mockEvent = new Event(1, LocalDateTime.parse("2000-01-01T01:00:00"), false, "message 1");
        List<Event> mockEvents = Collections.singletonList(mockEvent);

        when(eventDao.getAllEventsLimited(anyInt())).thenReturn(mockEvents);

        mockMvc.perform(get(BASE_ENDPOINT)
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonArray(mockEvents)));

    }

    @Test
    public void testGetEvents_returns_status_code_404_not_found_when_no_events_found() throws Exception {
        when(eventDao.getAllEventsLimited(anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_ENDPOINT)
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private String toJsonArray(List<Event> events) throws JsonProcessingException {
        return mapper.writeValueAsString(events);
    }

    private String toJson(Event event) throws JsonProcessingException {
        return mapper.writeValueAsString(event);
    }

}
