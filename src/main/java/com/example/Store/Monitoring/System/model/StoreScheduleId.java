package com.example.Store.Monitoring.System.model;


import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalTime;

@Embeddable
@EqualsAndHashCode
public class StoreScheduleId implements Serializable {

    private String storeId;
    private byte dayOfWeek;
    private LocalTime startTimeLocal;

    public StoreScheduleId() {
    }

    public StoreScheduleId(String storeId, byte dayOfWeek, LocalTime startTimeLocal) {
        this.storeId = storeId;
        this.dayOfWeek = dayOfWeek;
        this.startTimeLocal = startTimeLocal;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public LocalTime getStartTimeLocal() {
        return startTimeLocal;
    }

    public void setStartTimeLocal(LocalTime startTimeLocal) {
        this.startTimeLocal = startTimeLocal;
    }

    public byte getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(byte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
