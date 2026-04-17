package com.booking.movietickets.repository;

import com.booking.movietickets.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s JOIN FETCH s.screen sc JOIN FETCH sc.theatre t JOIN FETCH t.city c WHERE s.movie.id = :movieId AND c.id = :cityId AND s.showDate = :date")
    List<Show> findShowsByMovieAndCityAndDate(Long movieId, Long cityId, LocalDate date);

    @Query("SELECT DISTINCT s.movie FROM Show s WHERE s.showDate > :date OR (s.showDate = :date AND s.startTime >= :time)")
    List<com.booking.movietickets.model.Movie> findMoviesWithFutureShows(java.time.LocalDate date, java.time.LocalTime time);
}
