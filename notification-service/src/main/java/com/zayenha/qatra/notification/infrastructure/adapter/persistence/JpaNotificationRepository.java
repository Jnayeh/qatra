package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' ORDER BY n.createdAt DESC")
    List<NotificationEntity> findInAppByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.channels LIKE '%IN_APP%' ORDER BY n.createdAt DESC")
    List<NotificationEntity> findInAppByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadInAppByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' AND n.readAt IS NOT NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findReadInAppByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadInAppByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.channels LIKE '%IN_APP%' AND n.readAt IS NOT NULL ORDER BY n.createdAt DESC")
    List<NotificationEntity> findReadInAppByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%'")
    long countInAppByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.channels LIKE '%IN_APP%'")
    long countInAppByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL")
    long countUnreadInAppByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.type = :type AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL")
    long countUnreadInAppByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = CURRENT_TIMESTAMP, n.status = 'READ' WHERE n.userId = :userId AND n.channels LIKE '%IN_APP%' AND n.readAt IS NULL")
    int markAllInAppAsReadByUserId(@Param("userId") Long userId);

    boolean existsByCorrelationId(String correlationId);
}
