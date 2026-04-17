package com.booking.movietickets.service.impl;

import com.booking.movietickets.dto.ShowManagementRequestDTO;
import com.booking.movietickets.exception.InvalidBookingException;
import com.booking.movietickets.exception.ResourceNotFoundException;
import com.booking.movietickets.model.*;
import com.booking.movietickets.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowManagementServiceImplTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ShowManagementServiceImpl showManagementService;

    // -Tests for createShow ---

    @Test
    void createShow_ShouldSucceed_WhenInputIsValid() {
        ShowManagementRequestDTO request = ShowManagementRequestDTO.builder()
                .movieId(1L).screenId(2L).basePrice(new BigDecimal("150.00"))
                .showDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(21, 0))
                .build();

        Movie movie = Movie.builder().id(1L).title("Inception").build();
        Screen screen = Screen.builder().id(2L).name("Screen 1").build();
        Seat seat1 = Seat.builder().id(10L).seatNumber("A1").build();

        Show savedShow = Show.builder().id(100L).movie(movie).screen(screen).build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screenRepository.findById(2L)).thenReturn(Optional.of(screen));
        when(showRepository.save(any(Show.class))).thenReturn(savedShow);
        when(seatRepository.findByScreenId(2L)).thenReturn(List.of(seat1));

        Show result = showManagementService.createShow(request);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(showSeatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createShow_ShouldThrowException_WhenMovieNotFound() {
        // Given
        ShowManagementRequestDTO request = ShowManagementRequestDTO.builder().movieId(1L).build();
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showManagementService.createShow(request));
    }

    @Test
    void createShow_ShouldThrowException_WhenScreenNotFound() {
        ShowManagementRequestDTO request = ShowManagementRequestDTO.builder().movieId(1L).screenId(2L).build();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(new Movie()));
        when(screenRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showManagementService.createShow(request));
    }

    // -Tests for updateShow

    @Test
    void updateShow_ShouldSucceed_WhenShowExists() {
        Long showId = 100L;
        ShowManagementRequestDTO request = ShowManagementRequestDTO.builder()
                .showDate(LocalDate.now()).startTime(LocalTime.of(10, 0)).build();
        Show existingShow = Show.builder().id(showId).build();

        when(showRepository.findById(showId)).thenReturn(Optional.of(existingShow));
        when(showRepository.save(any(Show.class))).thenReturn(existingShow);
        Show result = showManagementService.updateShow(showId, request);

        assertNotNull(result);
        verify(showRepository).save(existingShow);
    }

    @Test
    void updateShow_ShouldThrowException_WhenShowNotFound() {
        when(showRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> showManagementService.updateShow(1L, new ShowManagementRequestDTO()));
    }

    // Tests for deleteShow

    @Test
    void deleteShow_ShouldSucceed_WhenNoBookingsExist() {
        Long showId = 1L;
        when(bookingRepository.existsByShowId(showId)).thenReturn(false);

        showManagementService.deleteShow(showId);
        verify(showSeatRepository).deleteByShowId(showId);
        verify(showRepository).deleteById(showId);
    }

    @Test
    void deleteShow_ShouldThrowException_WhenBookingsExist() {
        Long showId = 1L;
        when(bookingRepository.existsByShowId(showId)).thenReturn(true);
        assertThrows(InvalidBookingException.class, () -> showManagementService.deleteShow(showId));
    }
}
