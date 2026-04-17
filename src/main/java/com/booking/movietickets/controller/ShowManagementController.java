package com.booking.movietickets.controller;

import com.booking.movietickets.dto.ShowManagementRequestDTO;
import com.booking.movietickets.model.Show;
import com.booking.movietickets.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/management/shows")
@RequiredArgsConstructor
public class ShowManagementController {

    private final ShowManagementService showManagementService;

    @PostMapping
    public ResponseEntity<Show> createShow(@RequestBody ShowManagementRequestDTO request) {
        Show createdShow = showManagementService.createShow(request);
        return new ResponseEntity<>(createdShow, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Show> updateShow(@PathVariable Long id, @RequestBody ShowManagementRequestDTO request) {
        Show updatedShow = showManagementService.updateShow(id, request);
        return ResponseEntity.ok(updatedShow);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showManagementService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}
