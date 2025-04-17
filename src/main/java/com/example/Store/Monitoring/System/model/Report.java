package com.example.Store.Monitoring.System.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "report")
@Data
public class Report {

    @Id
    private String id = UUID.randomUUID().toString();

    private String status;

    private String filePath;

    private ZonedDateTime createdAt;

}
