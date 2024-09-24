package com.harrisonmauseth.network_monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonPropertyOrder({"eventId", "eventTime", "isConnected", "message"})
public class Event {
    private int eventId;
    private LocalDateTime eventTime;
    @JsonProperty("isConnected")
    @NotNull(message = "'connected' must be a boolean")
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

    @JsonProperty("isConnected")
    public boolean isConnected() {
        return isConnected;
    }

    @JsonProperty("isConnected")
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
