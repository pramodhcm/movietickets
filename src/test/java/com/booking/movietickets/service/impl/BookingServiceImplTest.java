package com.booking.movietickets.service.impl;

import com.booking.movietickets.dto.BookingRequestDTO;
import com.booking.movietickets.dto.BookingResponseDTO;
import com.booking.movietickets.exception.ResourceNotFoundException;
import com.booking.movietickets.exception.SeatNotAvailableException;
import com.booking.movietickets.model.*;
import com.booking.movietickets.repository.BookingRepository;
import com.booking.movietickets.repository.ShowRepository;
import com.booking.movietickets.repository.ShowSeatRepository;
import com.booking.movietickets.repository.UserRepository;
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
public class BookingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void bookTickets_ShouldSucceed_WithCorrectPriceCalculation() {
        BookingRequestDTO request = new BookingRequestDTO(1L, 10L, List.of(101L, 102L, 103L));

        User user = User.builder().id(1L).email("user@test.com").build();
        Show show = Show.builder().id(10L).showDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(14, 0)) // Afternoon show (20% discount)
                .build();

        Seat s1 = Seat.builder().seatNumber("A1").build();
        Seat s2 = Seat.builder().seatNumber("A2").build();
        Seat s3 = Seat.builder().seatNumber("A3").build();

        ShowSeat ss1 = ShowSeat.builder().id(101L).price(new BigDecimal("100.00")).status(ShowSeatStatus.AVAILABLE)
                .seat(s1).build();
        ShowSeat ss2 = ShowSeat.builder().id(102L).price(new BigDecimal("100.00")).status(ShowSeatStatus.AVAILABLE)
                .seat(s2).build();
        ShowSeat ss3 = ShowSeat.builder().id(103L).price(new BigDecimal("100.00")).status(ShowSeatStatus.AVAILABLE)
                .seat(s3).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByIdsAndShowIdWithPessimisticWriteLock(anyList(), eq(10L)))
                .thenReturn(List.of(ss1, ss2, ss3));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(500L);
            return b;
        });

        // When
        BookingResponseDTO response = bookingService.bookTickets(request);

        assertEquals(500L, response.getBookingId());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getTotalAmount()));
        assertEquals(3, response.getBookedSeats().size());

        verify(bookingRepository, times(1)).save(any(Booking.class));
        assertEquals(ShowSeatStatus.BOOKED, ss1.getStatus());
    }

    @Test
    void bookTickets_ShouldThrowException_WhenUserNotFound() {
        BookingRequestDTO request = new BookingRequestDTO(1L, 10L, List.of(101L));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.bookTickets(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTickets_ShouldThrowException_WhenSeatIsAlreadyBooked() {
        BookingRequestDTO request = new BookingRequestDTO(1L, 10L, List.of(101L));

        User user = User.builder().id(1L).build();
        Show show = Show.builder().id(10L).showDate(LocalDate.now().plusDays(1)).startTime(LocalTime.of(19, 0)).build();
        ShowSeat ss1 = ShowSeat.builder().id(101L).status(ShowSeatStatus.BOOKED).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByIdsAndShowIdWithPessimisticWriteLock(anyList(), eq(10L)))
                .thenReturn(List.of(ss1));

        assertThrows(SeatNotAvailableException.class, () -> bookingService.bookTickets(request));
    }
}
