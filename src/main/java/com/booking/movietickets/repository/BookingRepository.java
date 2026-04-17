package com.booking.movietickets.repository;

import com.booking.movietickets.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByShowId(Long showId);
}
