package com.example.Store.Monitoring.System.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "store_schedule")
@Data
public class StoreSchedule {

    @EmbeddedId
    private StoreScheduleId id;

    @Column(name = "end_time_local")
    private LocalTime endTimeLocal;



}
