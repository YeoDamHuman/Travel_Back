package com.example.backend.favorite.service;

import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.favorite.dto.request.FavoriteRequest;
import com.example.backend.favorite.dto.response.FavoriteResponse;
import com.example.backend.favorite.entity.Favorite;
import com.example.backend.favorite.repository.FavoriteRepository;
import com.example.backend.tour.webclient.TourApiClient;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TourApiClient tourApiClient;

    @Transactional
    public FavoriteResponse.FavoriteActionResponse toggleFavorite(String userIdString, FavoriteRequest.AddFavoriteRequest request) {
        User user = findUserById(userIdString);
        
        // 이미 즐겨찾기에 있으면 제거
        if (favoriteRepository.existsByUserAndContentId(user, request.getContentId())) {
            favoriteRepository.deleteByUserAndContentId(user, request.getContentId());
            return new FavoriteResponse.FavoriteActionResponse("즐겨찾기에서 제거되었습니다.", false);
        }

        // TourAPI에서 상세 정보 가져오기
        try {
            CartResponse.TourDetailResponse tourDetail = tourApiClient.getTourDetail(request.getContentId());
            
            // 없으면 추가
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .contentId(request.getContentId())
                    .placeTitle(tourDetail.getTitle())
                    .placeAddress(tourDetail.getAddress())
                    .placeImage(tourDetail.getImage())
                    .regionCode(tourDetail.getRegion())
                    .build();

            favoriteRepository.save(favorite);
            return new FavoriteResponse.FavoriteActionResponse("즐겨찾기에 추가되었습니다.", true);
            
        } catch (Exception e) {
            log.error("TourAPI에서 상세 정보 가져오기 실패 - contentId: {}", request.getContentId(), e);
            throw new RuntimeException("장소 정보를 가져올 수 없습니다.");
        }
    }

    @Transactional
    public FavoriteResponse.FavoriteActionResponse removeFavorite(String userIdString, String contentId) {
        User user = findUserById(userIdString);
        
        if (!favoriteRepository.existsByUserAndContentId(user, contentId)) {
            return new FavoriteResponse.FavoriteActionResponse("즐겨찾기에 없는 장소입니다.", false);
        }

        favoriteRepository.deleteByUserAndContentId(user, contentId);
        return new FavoriteResponse.FavoriteActionResponse("즐겨찾기에서 제거되었습니다.", false);
    }

    @Transactional(readOnly = true)
    public FavoriteResponse.FavoriteListResponse getFavorites(String userIdString) {
        User user = findUserById(userIdString);
        List<Favorite> favorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);

        List<FavoriteResponse.FavoriteInfo> favoriteInfos = favorites.stream()
                .map(this::convertToFavoriteInfo)
                .collect(Collectors.toList());

        return FavoriteResponse.FavoriteListResponse.builder()
                .favorites(favoriteInfos)
                .totalCount(favoriteInfos.size())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String userIdString, String contentId) {
        User user = findUserById(userIdString);
        return favoriteRepository.existsByUserAndContentId(user, contentId);
    }

    private User findUserById(String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private FavoriteResponse.FavoriteInfo convertToFavoriteInfo(Favorite favorite) {
        return FavoriteResponse.FavoriteInfo.builder()
                .favoriteId(favorite.getFavoriteId())
                .contentId(favorite.getContentId())
                .placeTitle(favorite.getPlaceTitle())
                .placeAddress(favorite.getPlaceAddress())
                .placeImage(favorite.getPlaceImage())
                .regionCode(favorite.getRegionCode())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}