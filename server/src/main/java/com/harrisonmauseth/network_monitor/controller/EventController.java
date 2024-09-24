package com.harrisonmauseth.network_monitor.controller;

import com.harrisonmauseth.network_monitor.dao.EventDao;
import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/event")
public class EventController {
    private EventDao eventDao;

    public EventController(EventDao dao) {
        this.eventDao = dao;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        List<Event> events;
        try {
            events = eventDao.getAllEvents();
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        if (!events.isEmpty()) {
            return events;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No events found.");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Event log(@Valid @RequestBody Event eventToCreate) {
        Event createdEvent;
        try {
            createdEvent = eventDao.createEvent(eventToCreate);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        if (createdEvent != null) {
            return createdEvent;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to log event.");
        }
    }
}
