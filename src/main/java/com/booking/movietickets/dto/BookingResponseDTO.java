package com.booking.movietickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime bookingTime;
    private List<String> bookedSeats;
}
