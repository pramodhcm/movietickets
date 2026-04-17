package com.booking.movietickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowResponseDTO {
    private Long showId;
    private Long screenId;
    private String screenName;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
