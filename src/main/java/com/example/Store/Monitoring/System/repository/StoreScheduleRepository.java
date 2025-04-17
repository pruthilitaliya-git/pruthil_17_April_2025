    package com.example.Store.Monitoring.System.repository;

    import com.example.Store.Monitoring.System.model.StoreSchedule;
    import com.example.Store.Monitoring.System.model.StoreScheduleId;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, StoreScheduleId> {

    }