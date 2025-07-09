package com.example.backend.tour.service;

import com.example.backend.tour.dto.response.TourResponse;
import com.example.backend.tour.entity.Tour;
import com.example.backend.tour.entity.TourCategory;
import com.example.backend.tour.repository.TourRepository;
import com.example.backend.tour.webclient.TourApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final TourApiClient tourApiClient;

    @Transactional(readOnly = true)
    public Page<TourResponse> searchTours(String keyword, String region, String category, Pageable pageable) {
        return tourApiClient.searchTours(keyword, region, category, pageable);
    }

    @Transactional
    public Tour getOrCreateTour(String contentId) {
        Optional<Tour> existingTour = tourRepository.findByContentId(contentId);
        if (existingTour.isPresent()) {
            return existingTour.get();
        }

        TourResponse tourDetails = tourApiClient.fetchTourDetails(contentId);
        if (tourDetails == null) {
            throw new RuntimeException("Tour details could not be fetched for contentId: " + contentId);
        }
        Tour newTour = convertToEntity(tourDetails);
        return tourRepository.save(newTour);
    }

    private Tour convertToEntity(TourResponse dto) {
        return Tour.builder()
                .contentId(dto.getContentId())
                .contentTypeId(dto.getContentTypeId())
                .title(dto.getTitle())
                .address(dto.getAddress())
                .address2(dto.getAddress2())
                .zipcode(dto.getZipcode())
                .areaCode(dto.getAreaCode())
                .cat1(dto.getCat1())
                .cat2(dto.getCat2())
                .cat3(dto.getCat3())
                .createdTime(dto.getCreatedTime())
                .firstImage(dto.getFirstImage())
                .firstImage2(dto.getFirstImage2())
                .cpyrhtDivCd(dto.getCpyrhtDivCd())
                .mapX(dto.getMapX())
                .mapY(dto.getMapY())
                .mlevel(dto.getMlevel())
                .modifiedTime(dto.getModifiedTime())
                .sigunguCode(dto.getSigunguCode())
                .tel(dto.getTel())
                .overview(dto.getOverview())
                .longitude(dto.getMapX() != null && !dto.getMapX().isEmpty()
                        ? Double.valueOf(dto.getMapX()) : null)
                .latitude(dto.getMapY() != null && !dto.getMapY().isEmpty()
                        ? Double.valueOf(dto.getMapY()) : null)
                .image(dto.getFirstImage())
                .category(mapContentTypeToCategory(dto.getContentTypeId()))
                .build();
    }

    private TourCategory mapContentTypeToCategory(String contentTypeId) {
        if (contentTypeId == null) {
            return null;
        }
        return switch (contentTypeId) {
            case "12" -> TourCategory.TOURIST_SPOT;
            case "39" -> TourCategory.RESTAURANT;
            case "32" -> TourCategory.ACCOMMODATION;
            case "28" -> TourCategory.LEISURE;
            default -> TourCategory.ETC;
        };
    }
}
