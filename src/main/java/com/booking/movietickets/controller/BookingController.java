package com.booking.movietickets.controller;

import com.booking.movietickets.dto.BookingRequestDTO;
import com.booking.movietickets.dto.BookingResponseDTO;
import com.booking.movietickets.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @CacheEvict(value = "theatresAndShows", allEntries = true)
    public ResponseEntity<BookingResponseDTO> bookTickets(@Valid @RequestBody BookingRequestDTO request) {
        log.info("Received request to book tickets for user: {} and show: {}", request.getUserId(),
                request.getShowId());

        BookingResponseDTO response = bookingService.bookTickets(request);

        log.info("Successfully booked tickets. Booking ID: {}", response.getBookingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
