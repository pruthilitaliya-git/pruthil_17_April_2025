package com.example.Store.Monitoring.System.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "store_status")
public class StoreStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="store_id")
    private String storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "timestamp_utc")
    private String timestampUtc;

    public enum Status{
        active,
        inactive
    }

}
