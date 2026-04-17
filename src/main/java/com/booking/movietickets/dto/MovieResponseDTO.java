package com.booking.movietickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String language;
    private String genre;
    private Integer durationMinutes;
}
