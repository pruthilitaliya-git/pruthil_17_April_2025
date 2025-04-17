package com.example.Store.Monitoring.System.repository;

import com.example.Store.Monitoring.System.model.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreStatusRepository extends JpaRepository<StoreStatus, Long> {

    @Query(value = "select * from store_status where store_id = ?", nativeQuery = true)
    List<StoreStatus> findByStoreId(String id);

}
