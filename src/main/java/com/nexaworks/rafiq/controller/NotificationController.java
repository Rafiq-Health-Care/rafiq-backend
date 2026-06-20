package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.notification.NotificationResponse;
import com.nexaworks.rafiq.exception.model.ErrorResponse;
import com.nexaworks.rafiq.service.notification.INotificationPersistenceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/notification")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Endpoints for notification")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied to this notification", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
public class NotificationController {
    private final INotificationPersistenceService notificationPersistenceService;

    @Operation(summary = "Mark a notification as read", description = "Marks a specific notification as read by its ID")
    @ApiResponse(responseCode = "204", description = "Notification marked as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(
            @Parameter(description = "UUID of the notification to mark as read", required = true) @PathVariable UUID id) {
        notificationPersistenceService.markNotificationAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a notification", description = "Permanently deletes a specific notification by its ID")
    @ApiResponse(responseCode = "204", description = "Notification deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "UUID of the notification to delete", required = true) @PathVariable UUID id) {
        notificationPersistenceService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for the currently authenticated user")
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @PatchMapping("/read")
    public ResponseEntity<Void> readAll() {
        notificationPersistenceService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all notifications", description = "Returns a paginated list of notifications for the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getAllNotifications(
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        PageResponse<NotificationResponse> response = notificationPersistenceService
                .getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a notification by ID", description = "Returns the details of a specific notification")
    @ApiResponse(responseCode = "200", description = "Notification retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "UUID of the notification to retrieve", required = true) @PathVariable UUID id) {
        NotificationResponse response = notificationPersistenceService.getById(id);
        return ResponseEntity.ok(response);
    }

}
