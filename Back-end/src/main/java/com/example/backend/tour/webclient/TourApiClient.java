package com.example.backend.tour.webclient;

import com.example.backend.tour.dto.response.TourResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TourApiClient {
    Page<TourResponse> searchTours(String keyword, String region, String category, Pageable pageable);
    TourResponse fetchTourDetails(String contentId);
}
