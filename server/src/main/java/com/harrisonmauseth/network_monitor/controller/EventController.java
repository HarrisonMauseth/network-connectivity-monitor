package com.harrisonmauseth.network_monitor.controller;

import com.harrisonmauseth.network_monitor.dao.EventDao;
import com.harrisonmauseth.network_monitor.exception.DaoException;
import com.harrisonmauseth.network_monitor.model.Event;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/events")
public class EventController {
    private final EventDao eventDao;

    public EventController(EventDao dao) {
        this.eventDao = dao;
    }

    @GetMapping
    public List<Event> getEvents(@RequestParam(defaultValue = "0") int limit) {
        List<Event> events;
        try {
            events = eventDao.getAllEventsLimited(limit);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        return events;
    }

    @GetMapping(path = "/failed")
    public List<Event> getAllDisconnectedEvents(@RequestParam(defaultValue = "0") int limit) {
        List<Event> events;
        try {
            events = eventDao.getAllDisconnectedEvents(limit);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        return events;
    }

    @GetMapping(path = "/failed/wifi")
    public List<Event> getDisconnectedWifiEvents(@RequestParam(defaultValue = "0") int limit) {
        List<Event> events;
        try {
            events = eventDao.getDisconnectedWifiEvents(limit);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        return events;
    }

    @GetMapping(path = "/failed/internet")
    public List<Event> getDisconnectedInternetEvents(@RequestParam(defaultValue = "0") int limit) {
        List<Event> events;
        try {
            events = eventDao.getDisconnectedInternetEvents(limit);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        return events;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/multiple")
    public List<Event> logMultiple(@RequestBody Event[] eventsToCreate) {
        List<Event> createdEvents;
        try {
            createdEvents = eventDao.createMultipleEvents(eventsToCreate);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
        if (createdEvents != null) {
            return createdEvents;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to log events.");
        }
    }

    @PutMapping(path = "/{id}")
    public Event updateEvent(@Valid @RequestBody Event eventToUpdate, @PathVariable int id) {
        eventToUpdate.setEventId(id);
        try {
            return eventDao.updateEvent(eventToUpdate);
        } catch (DaoException e) {
            if (e.getMessage().equals("Zero rows affected, expected at least one.")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event (eventId: " + id + ") not found.");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
            }
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteLog(@PathVariable int id) {
        try {
            int rowsDeleted = eventDao.deleteEvent(id);
            if (rowsDeleted == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event (eventId: " + id + ") not found.");
            }
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
