package com.booking.movietickets.service;

import com.booking.movietickets.dto.MovieResponseDTO;
import com.booking.movietickets.dto.TheatreShowResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface BrowseService {
    List<MovieResponseDTO> getMovies();
    List<TheatreShowResponseDTO> getTheatresAndShows(Long movieId, Long cityId, LocalDate date);
}
