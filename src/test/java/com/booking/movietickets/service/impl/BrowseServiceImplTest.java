package com.booking.movietickets.service.impl;

import com.booking.movietickets.dto.MovieResponseDTO;
import com.booking.movietickets.dto.TheatreShowResponseDTO;
import com.booking.movietickets.model.Movie;
import com.booking.movietickets.model.Screen;
import com.booking.movietickets.model.Show;
import com.booking.movietickets.model.Theatre;
import com.booking.movietickets.repository.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowseServiceImplTest {

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private BrowseServiceImpl browseService;

    @Test
    void getMovies_ShouldReturnMovies_WhenFutureShowsExist() {
        Movie movie = Movie.builder().id(1L).title("The Matrix").build();
        when(showRepository.findMoviesWithFutureShows(any(), any())).thenReturn(List.of(movie));

        List<MovieResponseDTO> result = browseService.getMovies();
        assertFalse(result.isEmpty());
        assertEquals("The Matrix", result.get(0).getTitle());
    }

    @Test
    void getMovies_ShouldReturnEmptyList_WhenNoFutureShowsExist() {
        when(showRepository.findMoviesWithFutureShows(any(), any())).thenReturn(Collections.emptyList());

        List<MovieResponseDTO> result = browseService.getMovies();
        assertTrue(result.isEmpty());
    }

    @Test
    void getTheatresAndShows_ShouldReturnGroupedShows_WhenFutureShowsExist() {
        Long movieId = 1L;
        Long cityId = 1L;
        LocalDate date = LocalDate.now();

        Theatre theatre = Theatre.builder().id(1L).name("PVR Cinemas").build();
        Screen screen = Screen.builder().id(1L).name("Screen 1").theatre(theatre).build();

        // Show in the future
        Show show = Show.builder().id(100L).screen(screen).showDate(date).startTime(LocalTime.now().plusHours(5))
                .build();

        when(showRepository.findShowsByMovieAndCityAndDate(movieId, cityId, date)).thenReturn(List.of(show));

        List<TheatreShowResponseDTO> result = browseService.getTheatresAndShows(movieId, cityId, date);

        assertEquals(1, result.size());
        assertEquals("PVR Cinemas", result.get(0).getTheatreName());
        assertEquals(1, result.get(0).getShows().size());
    }

    @Test
    void getTheatresAndShows_ShouldReturnEmptyList_WhenNoShowsForCriteria() {
        // Given
        when(showRepository.findShowsByMovieAndCityAndDate(any(), any(), any())).thenReturn(Collections.emptyList());

        List<TheatreShowResponseDTO> result = browseService.getTheatresAndShows(1L, 1L, LocalDate.now());
        assertTrue(result.isEmpty());
    }

    @Test
    void getTheatresAndShows_ShouldExcludePastShows_WhenThresholdIsReached() {
        Long movieId = 1L;
        Long cityId = 1L;
        LocalDate date = LocalDate.now();

        Theatre theatre = Theatre.builder().id(1L).build();
        Screen screen = Screen.builder().id(1L).theatre(theatre).build();

        // manully created Show in the past
        Show pastShow = Show.builder().id(100L).screen(screen).showDate(date).startTime(LocalTime.now().minusHours(5))
                .build();

        when(showRepository.findShowsByMovieAndCityAndDate(movieId, cityId, date)).thenReturn(List.of(pastShow));

        List<TheatreShowResponseDTO> result = browseService.getTheatresAndShows(movieId, cityId, date);
        assertTrue(result.isEmpty());
    }
}
