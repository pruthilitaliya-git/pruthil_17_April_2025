package com.example.Store.Monitoring.System.repository;

import com.example.Store.Monitoring.System.model.StoreTimezones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTimezonesRepository extends JpaRepository<StoreTimezones, String> {

}
