package com.booking.movietickets.service.impl;

import com.booking.movietickets.dto.ShowManagementRequestDTO;
import com.booking.movietickets.exception.InvalidBookingException;
import com.booking.movietickets.exception.ResourceNotFoundException;
import com.booking.movietickets.model.*;
import com.booking.movietickets.repository.*;
import com.booking.movietickets.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowManagementServiceImpl implements ShowManagementService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Show createShow(ShowManagementRequestDTO request) {
        log.info("Entering createShow with request: {}", request);
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        Show show = Show.builder().movie(movie).screen(screen).showDate(request.getShowDate())
                .startTime(request.getStartTime()).endTime(request.getEndTime()).build();

        Show savedShow = showRepository.save(show);

        // Initialize seats for the show
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : seats) {
            ShowSeat showSeat = ShowSeat.builder().show(savedShow).seat(seat).status(ShowSeatStatus.AVAILABLE)
                    .price(request.getBasePrice()).build();
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);

        log.info("Exiting createShow with created show ID: {}", savedShow.getId());
        return savedShow;
    }

    @Override
    @Transactional
    public Show updateShow(Long id, ShowManagementRequestDTO request) {
        log.info("Entering updateShow for ID: {} with request: {}", id, request);
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());

        if (request.getMovieId() != null) {
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
            show.setMovie(movie);
        }

        Show updatedShow = showRepository.save(show);
        log.info("Exiting updateShow for ID: {}", updatedShow.getId());
        return updatedShow;
    }

    @Override
    @Transactional
    public void deleteShow(Long id) {
        log.info("Entering deleteShow for ID: {}", id);
        // Can only delete if no bookings exist
        if (bookingRepository.existsByShowId(id)) {
            throw new InvalidBookingException("Cannot delete show as active bookings exist");
        }

        // Cleanup seats first, then the show
        showSeatRepository.deleteByShowId(id);
        showRepository.deleteById(id);
        log.info("Exiting deleteShow for ID: {}", id);
    }
}
