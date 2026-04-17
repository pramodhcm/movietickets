package com.booking.movietickets.service.impl;

import com.booking.movietickets.service.BrowseService;

import com.booking.movietickets.dto.MovieResponseDTO;
import com.booking.movietickets.dto.ShowResponseDTO;
import com.booking.movietickets.dto.TheatreShowResponseDTO;
import com.booking.movietickets.model.Movie;
import com.booking.movietickets.model.Show;
import com.booking.movietickets.model.Theatre;
import com.booking.movietickets.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrowseServiceImpl implements BrowseService {

        private final ShowRepository showRepository;

        @Transactional(readOnly = true)
        public List<MovieResponseDTO> getMovies() {
                log.info("Entering getMovies");
                LocalDateTime threshold = LocalDateTime.now().plusHours(1);
                List<Movie> movies = showRepository
                                .findMoviesWithFutureShows(threshold.toLocalDate(), threshold.toLocalTime());

                List<MovieResponseDTO> movieDTOs = new ArrayList<>();
                for (Movie movie : movies) {
                        movieDTOs.add(MovieResponseDTO.builder().id(movie.getId()).title(movie.getTitle())
                                        .description(movie.getDescription()).language(movie.getLanguage())
                                        .genre(movie.getGenre()).durationMinutes(movie.getDurationMinutes())
                                        .build());
                }
                log.info("Exiting getMovies with {} movies", movieDTOs.size());
                return movieDTOs;
        }

        @Transactional(readOnly = true)
        public List<TheatreShowResponseDTO> getTheatresAndShows(Long movieId, Long cityId, LocalDate date) {
                log.info("Entering getTheatresAndShows with movieId: {}, cityId: {}, date: {}", movieId, cityId, date);
                LocalDateTime threshold = LocalDateTime.now().plusHours(1);
                List<Show> allShows = showRepository.findShowsByMovieAndCityAndDate(movieId, cityId, date);

                // Map to group shows by Theatre ID for easy aggregation
                Map<Theatre, List<Show>> theatreGroupMap = new HashMap<>();
                for (Show show : allShows) {
                        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
                        if (showDateTime.isAfter(threshold)) {
                                Theatre theatre = show.getScreen().getTheatre();
                                theatreGroupMap.computeIfAbsent(theatre, k -> new ArrayList<>()).add(show);
                        }
                }

                List<TheatreShowResponseDTO> response = new ArrayList<>();
                for (Map.Entry<Theatre, List<Show>> entry : theatreGroupMap.entrySet()) {
                        Theatre theatre = entry.getKey();

                        List<ShowResponseDTO> showDTOs = new ArrayList<>();
                        for (Show s : entry.getValue()) {
                                showDTOs.add(ShowResponseDTO.builder().showId(s.getId()).screenId(s.getScreen().getId())
                                                .screenName(s.getScreen().getName()).showDate(s.getShowDate())
                                                .startTime(s.getStartTime()).endTime(s.getEndTime()).build());
                        }

                        response.add(TheatreShowResponseDTO.builder().theatreId(theatre.getId())
                                        .theatreName(theatre.getName()).address(theatre.getAddress())
                                        .shows(showDTOs).build());
                }
                log.info("Exiting getTheatresAndShows with {} theatres", response.size());
                return response;
        }
}
