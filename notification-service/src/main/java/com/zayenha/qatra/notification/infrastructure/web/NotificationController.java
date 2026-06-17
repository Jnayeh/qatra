package com.zayenha.qatra.notification.infrastructure.web;

import com.zayenha.qatra.notification._shared.web.ApiResponse;
import com.zayenha.qatra.notification._shared.web.Paginated;
import com.zayenha.qatra.notification.application.dto.NotificationResponse;
import com.zayenha.qatra.notification.application.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationQueryService queryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications(
            Authentication auth,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        var safePage = page > 1 ? page - 1 : 1;
        int pageIndex = page - 1;
        var notifications = queryService.getUserNotifications(userId, type, read, pageIndex, size);
        var total = queryService.countUserNotifications(userId, type, read);
        var paginated = new Paginated(safePage, size, total, (int) Math.ceil((double) total / size));
        return ResponseEntity.ok(ApiResponse.success(notifications, paginated));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        var response = queryService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        var count = queryService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        var count = queryService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
