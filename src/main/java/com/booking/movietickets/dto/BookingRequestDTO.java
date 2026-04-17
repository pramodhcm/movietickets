package com.booking.movietickets.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {
    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotNull(message = "Show ID must not be null")
    private Long showId;

    @NotEmpty(message = "Seat IDs must not be empty")
    private List<Long> seatIds;
}
