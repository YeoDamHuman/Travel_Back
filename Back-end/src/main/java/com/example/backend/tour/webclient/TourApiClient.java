package com.example.backend.tour.webclient;

import com.example.backend.cart.dto.response.CartResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${tour.api.key}")
    private String apiKey;

    @Value("${tour.api.base-url:http://apis.data.go.kr/B551011/KorService2}")
    private String baseUrl;

    // 지역코드 매핑 (KorService2용)
    private static final Map<String, String> AREA_CODES = new HashMap<>() {{
        put("서울", "1");
        put("인천", "2");
        put("대전", "3");
        put("대구", "4");
        put("광주", "5");
        put("부산", "6");
        put("울산", "7");
        put("세종", "8");
        put("경기", "31");
        put("강원", "32");
        put("충북", "33");
        put("충남", "34");
        put("경북", "35");
        put("경남", "36");
        put("전북", "37");
        put("전남", "38");
        put("제주", "39");
    }};

    /**
     * 키워드를 이용한 관광지 검색
     */
    public Page<CartResponse.TourSearchResponse> searchTours(String keyword, String region, String category, Pageable pageable) {
        try {
            log.info("=== searchTours 시작 ===");
            log.info("keyword: {}, region: {}, category: {}", keyword, region, category);

            String uri = buildSearchUri(keyword, region, category, pageable);
            log.info("요청 URL: {}", uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("API 응답 수신 완료");
            return parseTourResponse(response, pageable);

        } catch (Exception e) {
            log.error("searchTours 오류", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * 키워드 검색용 URI 생성 (searchKeyword2 사용)
     */
    private String buildSearchUri(String keyword, String region, String category, Pageable pageable) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/searchKeyword2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "TravelPlanner")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")  // A=제목순, B=조회순, C=수정일순, D=생성일순
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("numOfRows", pageable.getPageSize());

        // 키워드 추가
        if (keyword != null && !keyword.isEmpty()) {
            builder.queryParam("keyword", keyword);
        }

        // 지역 코드 추가
        if (region != null && !region.isEmpty()) {
            String areaCode = AREA_CODES.get(region);
            if (areaCode != null) {
                builder.queryParam("areaCode", areaCode);
            }
        }

        // 카테고리(관광타입) 추가
        if (category != null && !category.isEmpty()) {
            String contentTypeId = getContentTypeId(category);
            if (contentTypeId != null) {
                builder.queryParam("contentTypeId", contentTypeId);
            }
        }

        String uri = builder.build(false).toUriString();
        log.info("최종 요청 URI: {}", uri);

        return uri;
    }

    /**
     * 지역 기반 관광지 검색
     */
    public Page<CartResponse.TourSearchResponse> searchToursByArea(String areaCode, String sigunguCode, Pageable pageable) {
        try {
            log.info("지역 기반 관광지 검색 - areaCode: {}, sigunguCode: {}", areaCode, sigunguCode);

            String uri = buildAreaBasedUri(areaCode, sigunguCode, pageable);
            log.info("요청 URL: {}", uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("API 응답 수신 완료");
            return parseTourResponse(response, pageable);

        } catch (Exception e) {
            log.error("지역 기반 관광지 검색 중 오류 발생: {}", e.getMessage(), e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * 지역 기반 검색용 URI 생성 (areaBasedList2 사용)
     */
    private String buildAreaBasedUri(String areaCode, String sigunguCode, Pageable pageable) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/areaBasedList2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "TravelPlanner")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("numOfRows", pageable.getPageSize());

        if (areaCode != null) {
            builder.queryParam("areaCode", areaCode);
        }

        if (sigunguCode != null) {
            builder.queryParam("sigunguCode", sigunguCode);
        }

        return builder.build(false).toUriString();
    }

    /**
     * API 응답 파싱
     */
    private Page<CartResponse.TourSearchResponse> parseTourResponse(String response, Pageable pageable) {
        try {
            log.info("=== parseTourResponse 시작 ===");
            log.info("응답 길이: {}", response.length());

            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");

            if (responseNode.isMissingNode()) {
                log.error("response 노드 없음");
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            JsonNode header = responseNode.path("header");
            String resultCode = header.path("resultCode").asText();
            log.info("resultCode: {}", resultCode);

            if (!"0000".equals(resultCode)) {
                log.error("API 오류: {}", header.path("resultMsg").asText());
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            JsonNode items = responseNode.path("body").path("items");
            JsonNode itemNode = items.path("item");

            log.info("item 노드 타입: {}, 크기: {}",
                    itemNode.getNodeType(),
                    itemNode.isArray() ? itemNode.size() : "단일");

            List<CartResponse.TourSearchResponse> tours = new ArrayList<>();

            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    CartResponse.TourSearchResponse tour = createTourResponse(item);
                    if (tour != null) {
                        tours.add(tour);
                        log.info("추가된 tour: {}", tour.getTitle());
                    }
                }
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                // 단일 결과인 경우
                CartResponse.TourSearchResponse tour = createTourResponse(itemNode);
                if (tour != null) {
                    tours.add(tour);
                }
            }

            log.info("파싱 완료 - 총 {}개", tours.size());
            return new PageImpl<>(tours, pageable, responseNode.path("body").path("totalCount").asInt(0));

        } catch (Exception e) {
            log.error("파싱 오류", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * 개별 tour 응답 객체 생성 (KorService2 응답 포맷)
     */
    private CartResponse.TourSearchResponse createTourResponse(JsonNode item) {
        try {
            return CartResponse.TourSearchResponse.builder()
                    .contentId(item.path("contentid").asText(""))
                    .contentTypeId(item.path("contenttypeid").asText(""))
                    .title(item.path("title").asText(""))
                    .address(item.path("addr1").asText(""))
                    .address2(item.path("addr2").asText(""))
                    .areaCode(item.path("areacode").asText(""))
                    .sigunguCode(item.path("sigungucode").asText(""))
                    .latitude(item.path("mapy").asDouble(0.0))
                    .longitude(item.path("mapx").asDouble(0.0))
                    .image(item.path("firstimage").asText(""))
                    .thumbnail(item.path("firstimage2").asText(""))
                    .tel(item.path("tel").asText(""))
                    .createdTime(item.path("createdtime").asText(""))
                    .modifiedTime(item.path("modifiedtime").asText(""))
                    .tema(item.path("cat3").asText(""))
                    .description("")  // overview는 상세조회에서만 제공
                    .build();
        } catch (Exception e) {
            log.error("TourResponse 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 관광타입 ID 반환 (contentTypeId)
     */
    private String getContentTypeId(String category) {
        Map<String, String> contentTypes = new HashMap<>();
        contentTypes.put("관광지", "12");
        contentTypes.put("문화시설", "14");
        contentTypes.put("행사", "15");
        contentTypes.put("축제", "15");
        contentTypes.put("여행코스", "25");
        contentTypes.put("레포츠", "28");
        contentTypes.put("숙박", "32");
        contentTypes.put("쇼핑", "38");
        contentTypes.put("음식점", "39");

        return contentTypes.get(category);
    }

    /**
     * API 테스트용 단순 호출
     */
    public String testApiConnection() {
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/searchKeyword2")
                    .queryParam("serviceKey", apiKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TravelPlanner")
                    .queryParam("_type", "json")
                    .queryParam("arrange", "A")
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "1")
                    .queryParam("keyword", "서울")
                    .build(false)
                    .toUriString();

            log.info("API 연결 테스트 URL: {}", uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("API 연결 테스트 성공");
            return response;

        } catch (Exception e) {
            log.error("API 연결 테스트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("TourAPI 연결 실패: " + e.getMessage(), e);
        }
    }
}