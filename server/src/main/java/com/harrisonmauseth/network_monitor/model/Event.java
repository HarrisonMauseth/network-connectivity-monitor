package com.harrisonmauseth.network_monitor.model;

import java.time.LocalDateTime;

public class Event {
    private int eventId;
    private LocalDateTime eventTime;
    private boolean isConnected;
    private String message;

    public Event() {
    }

    public Event(int eventId, LocalDateTime eventTime, boolean isConnected, String message) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.isConnected = isConnected;
        this.message = message;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventTime=" + eventTime +
                ", isConnected=" + isConnected +
                ", message='" + message + '\'' +
                '}';
    }
}
