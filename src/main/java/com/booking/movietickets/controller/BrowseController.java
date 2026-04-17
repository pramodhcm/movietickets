package com.booking.movietickets.controller;

import com.booking.movietickets.dto.MovieResponseDTO;
import com.booking.movietickets.dto.TheatreShowResponseDTO;
import com.booking.movietickets.service.BrowseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/browse")
@RequiredArgsConstructor
@Slf4j
public class BrowseController {

    private final BrowseService browseService;

    @GetMapping("/movies")
    @Cacheable("movies")
    public ResponseEntity<List<MovieResponseDTO>> getAllMovies() {
        List<MovieResponseDTO> response = browseService.getMovies();
        log.debug("Fetched {} movies", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movies/{movieId}/theaters")
    @Cacheable(value = "theatresAndShows", key = "{#movieId, #cityId, #date}")
    public ResponseEntity<List<TheatreShowResponseDTO>> browseTheatresWithShows(
            @PathVariable Long movieId,
            @RequestParam Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("Received request to browse theatres and shows for movieId: {}, cityId: {}, date: {}", movieId, cityId,
                date);
        List<TheatreShowResponseDTO> response = browseService.getTheatresAndShows(movieId, cityId, date);
        log.debug("Found {} theatres with available shows", response.size());

        return ResponseEntity.ok(response);
    }
}
