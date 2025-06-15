package com.rababodrif.bank2app.models;

import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HistoryEvent {
    private String id;
    private String eventId;
    private HistoryEventType eventType;
    private String timestamp;
    private String serviceName;
    private String entityId;
    private Map<String, Object> eventData;
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public HistoryEventType getEventType() { return eventType; }
    public void setEventType(HistoryEventType eventType) { this.eventType = eventType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public Map<String, Object> getEventData() { return eventData; }
    public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestampAsDateTime() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDateTime.parse(timestamp, formatter);
        } catch (Exception e) {
            Log.e("DATE_PARSE", "Erreur parsing date: " + timestamp, e);
            return LocalDateTime.now();
        }
    }
}