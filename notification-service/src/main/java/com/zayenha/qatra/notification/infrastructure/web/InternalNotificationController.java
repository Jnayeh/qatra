package com.zayenha.qatra.notification.infrastructure.web;

import com.zayenha.qatra.notification._shared.web.ApiResponse;
import com.zayenha.qatra.notification._shared.web.Paginated;
import com.zayenha.qatra.notification.application.dto.NotificationResponse;
import com.zayenha.qatra.notification.application.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/internal")
@Tag(name = "Internal Notification API", description = "Endpoints for nginx / inter-service communication")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
public class InternalNotificationController {

    private final NotificationQueryService queryService;

    @Operation(summary = "Get notifications for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var safePage = page > 1 ? page - 1 : 1;
        int pageIndex = page - 1;
        var notifications = queryService.getUserNotifications(userId, type, read, pageIndex, size);
        var total = queryService.countUserNotifications(userId, type, read);
        var paginated = new Paginated(safePage, size, total, (int) Math.ceil((double) total / size));
        return ResponseEntity.ok(ApiResponse.success(notifications, paginated));
    }

    @Operation(summary = "Get unread notification count for a user")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@PathVariable Long userId) {
        var count = queryService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Mark a specific notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {
        var response = queryService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark all notifications as read for a user")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@PathVariable Long userId) {
        var count = queryService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }
}
