package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long>, JpaSpecificationExecutor<NotificationEntity> {

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = CURRENT_TIMESTAMP, n.status = 'READ' WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL")
    int markAllInAppAsReadByUserId(@Param("userId") Long userId);

    boolean existsByCorrelationId(String correlationId);
}
