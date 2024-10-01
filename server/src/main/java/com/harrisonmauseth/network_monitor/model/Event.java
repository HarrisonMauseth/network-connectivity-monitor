package com.harrisonmauseth.network_monitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonPropertyOrder({"eventId", "eventTime", "isConnectedToWifi", "message"})
public class Event {
    private int eventId;
    private LocalDateTime eventTime;
    @JsonProperty("isConnectedToWifi")
    @NotNull(message = "'connected' must be a boolean")
    private boolean isConnectedToWifi;
    private boolean isConnectedToInternet;
    private String message;

    public Event() {
    }

    public Event(int eventId, LocalDateTime eventTime, boolean isConnectedToWifi, boolean isConnectedToInternet, String message) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.isConnectedToWifi = isConnectedToWifi;
        this.isConnectedToInternet = isConnectedToInternet;
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

    @JsonProperty("isConnectedToWifi")
    public boolean isConnectedToWifi() {
        return isConnectedToWifi;
    }

    @JsonProperty("isConnectedToWifi")
    public void setConnectedToWifi(boolean connectedToWifi) {
        isConnectedToWifi = connectedToWifi;
    }

    public boolean isConnectedToInternet() {
        return isConnectedToInternet;
    }

    public void setConnectedToInternet(boolean connectedToInternet) {
        isConnectedToInternet = connectedToInternet;
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
                ", isConnected=" + isConnectedToWifi +
                ", message='" + message + '\'' +
                '}';
    }
}
