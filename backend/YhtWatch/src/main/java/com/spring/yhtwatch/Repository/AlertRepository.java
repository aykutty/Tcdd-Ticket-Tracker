package com.spring.yhtwatch.Repository;

import com.spring.yhtwatch.Entity.Alert;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByActiveTrue();

    @Modifying
    @Transactional
    @Query("DELETE FROM Alert a WHERE a.lastNotifiedAt IS NOT NULL AND a.lastNotifiedAt < :cutoff")
    void deleteOldAlerts(@Param("cutoff") LocalDateTime cutoff);
}
