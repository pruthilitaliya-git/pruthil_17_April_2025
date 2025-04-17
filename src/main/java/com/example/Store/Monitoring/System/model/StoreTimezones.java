package com.example.Store.Monitoring.System.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "store_timezones")
@Data
public class StoreTimezones {

    @Id
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "timezones_str")
    private String timezonesStr;

}
