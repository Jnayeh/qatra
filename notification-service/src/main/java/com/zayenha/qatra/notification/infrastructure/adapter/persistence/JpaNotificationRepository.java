package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<NotificationEntity> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.readAt IS NOT NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findReadByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.readAt IS NOT NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findReadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndType(Long userId, NotificationType type);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.readAt IS NULL")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.readAt IS NULL")
    long countUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = CURRENT_TIMESTAMP, n.status = 'READ' WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    boolean existsByCorrelationId(String correlationId);
}
