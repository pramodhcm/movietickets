package com.booking.movietickets.service;

import com.booking.movietickets.dto.ShowManagementRequestDTO;
import com.booking.movietickets.model.Show;

public interface ShowManagementService {
    Show createShow(ShowManagementRequestDTO request);
    Show updateShow(Long id, ShowManagementRequestDTO request);
    void deleteShow(Long id);
}
