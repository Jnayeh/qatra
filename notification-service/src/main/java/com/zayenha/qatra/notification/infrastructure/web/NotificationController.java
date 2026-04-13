package com.zayenha.qatra.notification.infrastructure.web;

import com.zayenha.qatra.notification.application.dto.NotificationResponse;
import com.zayenha.qatra.notification.application.service.NotificationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationQueryService queryService;

    public NotificationController(NotificationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DONOR', 'CENTER_STAFF')")
    public ResponseEntity<Map<String, Object>> listNotifications(
            Authentication auth,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        var notifications = queryService.getUserNotifications(userId, type, read, page, size);
        var total = queryService.countUserNotifications(userId, type, read);
        return ResponseEntity.ok(Map.of(
                "content", notifications,
                "page", page,
                "size", size,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size)
        ));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('DONOR', 'CENTER_STAFF')")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        var response = queryService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('DONOR', 'CENTER_STAFF')")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        var count = queryService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('DONOR', 'CENTER_STAFF')")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        var count = queryService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
