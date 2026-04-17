package com.booking.movietickets.service;

import com.booking.movietickets.dto.BookingRequestDTO;
import com.booking.movietickets.dto.BookingResponseDTO;

public interface BookingService {
    BookingResponseDTO bookTickets(BookingRequestDTO request);
}
