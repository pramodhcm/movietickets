package com.booking.movietickets.service.impl;

import com.booking.movietickets.service.BookingService;

import com.booking.movietickets.dto.BookingRequestDTO;
import com.booking.movietickets.dto.BookingResponseDTO;
import com.booking.movietickets.exception.InvalidBookingException;
import com.booking.movietickets.exception.ResourceNotFoundException;
import com.booking.movietickets.exception.SeatNotAvailableException;
import com.booking.movietickets.model.*;
import com.booking.movietickets.repository.BookingRepository;
import com.booking.movietickets.repository.ShowRepository;
import com.booking.movietickets.repository.ShowSeatRepository;
import com.booking.movietickets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

        private final UserRepository userRepository;
        private final ShowRepository showRepository;
        private final ShowSeatRepository showSeatRepository;
        private final BookingRepository bookingRepository;

        @Transactional
        public BookingResponseDTO bookTickets(BookingRequestDTO request) {
                log.info("Starting ticket booking process for user: {} and show: {}", request.getUserId(),
                                request.getShowId());

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with id: " + request.getUserId()));

                Show show = showRepository.findById(request.getShowId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Show not found with id: " + request.getShowId()));

                LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
                if (showDateTime.isBefore(LocalDateTime.now())) {
                        log.warn("Attempted to book tickets for a past show: {}", show.getId());
                        throw new InvalidBookingException(
                                        "Cannot book tickets for a show that has already started or is in the past.");
                }

                List<Long> requestedSeatIds = request.getSeatIds();
                List<ShowSeat> showSeats = showSeatRepository
                                .findByIdsAndShowIdWithPessimisticWriteLock(requestedSeatIds, show.getId());

                if (showSeats.size() != requestedSeatIds.size()) {
                        throw new ResourceNotFoundException("One or more requested seats not found for this show.");
                }

                // Check if all seats are AVAILABLE
                for (ShowSeat ss : showSeats) {
                        if (ss.getStatus() != ShowSeatStatus.AVAILABLE) {
                                throw new SeatNotAvailableException("One or more seats are already booked or locked.");
                        }
                }

                log.debug("All requested seats are available, calculating amount and changing status");

                // Evaluate Special Offers
                boolean isAfternoonShow = show.getStartTime().getHour() >= 12 && show.getStartTime().getHour() < 17;

                BigDecimal totalAmount = BigDecimal.ZERO;
                int ticketCount = 0;

                for (ShowSeat ss : showSeats) {
                        ticketCount++;
                        BigDecimal seatPrice = ss.getPrice();

                        // 20% discount for afternoon shows
                        if (isAfternoonShow) {
                                seatPrice = seatPrice.multiply(new BigDecimal("0.80"));
                        }

                        // 50% discount on the 3rd ticket
                        if (ticketCount % 3 == 0) {
                                seatPrice = seatPrice.multiply(new BigDecimal("0.50"));
                        }

                        totalAmount = totalAmount.add(seatPrice);
                        ss.setStatus(ShowSeatStatus.BOOKED);
                        // automatically updating the entity inside a transaction context.
                }

                Booking booking = Booking.builder().user(user).show(show).bookingTime(LocalDateTime.now())
                                .totalAmount(totalAmount).status(BookingStatus.CONFIRMED).bookedSeats(showSeats)
                                .build();

                Booking savedBooking = bookingRepository.save(booking);

                List<String> bookedSeatNumbers = new ArrayList<>();
                for (ShowSeat ss : showSeats) {
                        bookedSeatNumbers.add(ss.getSeat().getSeatNumber());
                }

                BookingResponseDTO response = BookingResponseDTO.builder().bookingId(savedBooking.getId())
                                .status(savedBooking.getStatus().name()).totalAmount(savedBooking.getTotalAmount())
                                .bookingTime(savedBooking.getBookingTime()).bookedSeats(bookedSeatNumbers).build();
                log.info("Exiting bookTickets with bookingId: {}", response.getBookingId());
                return response;
        }
}
