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
import lombok.extern.slf4j.Slf4j;

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
    
    // 검색 결과를 캐시하여 상세 조회에 활용
    private final Map<String, JsonNode> tourDataCache = new HashMap<>();

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
                        // 상세 조회용 캐시에 저장
                        tourDataCache.put(tour.getContentId(), item);
                        log.info("추가된 tour: {}", tour.getTitle());
                    }
                }
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                // 단일 결과인 경우
                CartResponse.TourSearchResponse tour = createTourResponse(itemNode);
                if (tour != null) {
                    tours.add(tour);
                    // 상세 조회용 캐시에 저장
                    tourDataCache.put(tour.getContentId(), itemNode);
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
            log.info("item 전체 구조: {}", item.toPrettyString());

            return CartResponse.TourSearchResponse.builder()
                    .contentId(item.path("contentid").asText(""))
                    .contentTypeId(item.path("contenttypeid").asText(""))
                    .title(item.path("title").asText(""))
                    .address(item.path("addr1").asText(""))
                    .address2(item.path("addr2").asText(""))
                    .zipcode(item.path("zipcode").asText(""))
                    .areaCode(item.path("areacode").asText(""))
                    .cat1(item.path("cat1").asText(""))
                    .cat2(item.path("cat2").asText(""))
                    .cat3(item.path("cat3").asText(""))
                    .createdTime(item.path("createdtime").asText(""))
                    .firstImage(item.path("firstimage").asText(""))
                    .firstImage2(item.path("firstimage2").asText(""))
                    .cpyrhtDivCd(item.path("cpyrhtDivCd").asText(""))
                    .mapX(item.path("mapx").asText(""))
                    .mapY(item.path("mapy").asText(""))
                    .mlevel(item.path("mlevel").asText(""))
                    .modifiedTime(item.path("modifiedtime").asText(""))
                    .sigunguCode(item.path("sigungucode").asText(""))
                    .tel(item.path("tel").asText(""))
                    .overview("") // overview는 JSON에 없어서 기본값
                    .lDongRegnCd(item.path("lDongRegnCd").asText(""))
                    .lDongSignguCd(item.path("lDongSignguCd").asText(""))
                    .lclsSystm1(item.path("lclsSystm1").asText(""))
                    .lclsSystm2(item.path("lclsSystm2").asText(""))
                    .lclsSystm3(item.path("lclsSystm3").asText(""))
                    .build();
        } catch (Exception e) {
            log.error("Tour 응답 객체 생성 오류", e);
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
     * contentId로 투어 상세 정보 조회 (KorService2 표준 API 사용)
     */
    public CartResponse.TourDetailResponse getTourDetail(String contentId) {
        try {
            log.info("getTourDetail - contentId: {}", contentId);

            // 1. detailCommon2 호출
            JsonNode commonData = callDetailApi("detailCommon2", contentId, null);
            if (commonData == null) {
                return createFallbackDetailResponse(contentId);
            }

            // contentTypeId 추출
            String contentTypeId = commonData.path("contenttypeid").asText("12");
            
            // 2. detailIntro2 호출 (contentTypeId 필요)
            JsonNode introData = callDetailApi("detailIntro2", contentId, contentTypeId);
            
            // 3. detailInfo2 호출 (contentTypeId 필요) 
            JsonNode infoData = callDetailApi("detailInfo2", contentId, contentTypeId);
            
            // 4. detailImage2 호출
            JsonNode imageData = callDetailApi("detailImage2", contentId, null);
            
            // 5. detailPetTour2 호출 (선택사항)
            JsonNode petData = callDetailApi("detailPetTour2", contentId, null);

            return buildIntegratedDetailResponse(contentId, contentTypeId, commonData, introData, infoData, imageData, petData);

        } catch (Exception e) {
            log.error("상세 정보 조회 실패 - contentId: {}", contentId, e);
            return createFallbackDetailResponse(contentId);
        }
    }

    /**
     * KorService2 Detail API 호출 (Postman Collection 기준)
     */
    private JsonNode callDetailApi(String apiPath, String contentId, String contentTypeId) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/" + apiPath)
                    .queryParam("serviceKey", apiKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "MyApp")
                    .queryParam("contentId", contentId)
                    .queryParam("_type", "json");

            // detailIntro2, detailInfo2는 contentTypeId 필요
            if (contentTypeId != null && ("detailIntro2".equals(apiPath) || "detailInfo2".equals(apiPath))) {
                builder.queryParam("contentTypeId", contentTypeId);
            }

            // detailImage2는 imageYN 파라미터 필요
            if ("detailImage2".equals(apiPath)) {
                builder.queryParam("imageYN", "Y");
            }

            String uri = builder.build(false).toUriString();
            log.info("{} 호출 URL: {}", apiPath, uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            
            if (!"0000".equals(responseNode.path("header").path("resultCode").asText())) {
                log.warn("{} API 오류: {}", apiPath, responseNode.path("header").path("resultMsg").asText());
                return null;
            }

            JsonNode items = responseNode.path("body").path("items");
            JsonNode itemNode = items.path("item");
            
            // 배열인 경우 첫 번째 항목 반환, 단일 항목인 경우 그대로 반환
            if (itemNode.isArray() && itemNode.size() > 0) {
                return itemNode.get(0);
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                return itemNode;
            }
            
            return null;

        } catch (Exception e) {
            log.error("{} 호출 실패: {}", apiPath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 프론트엔드 요구사항에 맞는 간소화된 응답 생성
     */
    private CartResponse.TourDetailResponse buildIntegratedDetailResponse(
            String contentId, String contentTypeId,
            JsonNode commonData, JsonNode introData, JsonNode infoData, 
            JsonNode imageData, JsonNode petData) {
        
        try {
            // 프론트엔드 필수 정보만 추출
            String title = commonData.path("title").asText("");
            String addr1 = commonData.path("addr1").asText("");
            String areacode = commonData.path("areacode").asText("");
            String cat1 = commonData.path("cat1").asText("");
            String cat2 = commonData.path("cat2").asText("");
            String cat3 = commonData.path("cat3").asText("");
            String mapx = commonData.path("mapx").asText("");
            String mapy = commonData.path("mapy").asText("");
            String firstimage = commonData.path("firstimage").asText("");
            
            // 상세 페이지용 추가 정보 (선택사항)
            String tel = commonData.path("tel").asText("");
            String homepage = commonData.path("homepage").asText("");

            return CartResponse.TourDetailResponse.builder()
                    .contentId(contentId)
                    .contentTypeId(contentTypeId)
                    .title(title)                    // content_name 역할
                    .address(addr1)                  // 주소
                    .region(getRegionByAreaCode(areacode))  // 지역
                    .theme(getCategoryName(cat1, cat2, cat3))  // 테마
                    .latitude(parseDouble(mapy))     // 위도
                    .longitude(parseDouble(mapx))    // 경도
                    .image(firstimage)               // 이미지
                    .tel(tel)                        // 전화번호 (상세 페이지용)
                    .homepage(homepage)              // 홈페이지 (상세 페이지용)
                    .overview("")                    // overview 제거 (불필요)
                    .isFavorite(false)              // 즐겨찾기 상태
                    .isInCart(false)                // 장바구니 상태
                    .build();

        } catch (Exception e) {
            log.error("통합 응답 생성 실패", e);
            return createFallbackDetailResponse(contentId);
        }
    }

    


    /**
     * 테마별 투어 검색 (cat1, cat2, cat3 활용)
     */
    public Page<CartResponse.TourSearchResponse> searchToursByTheme(String theme, String region, Pageable pageable) {
        try {
            log.info("=== 테마별 검색 시작 - theme: {}, region: {} ===", theme, region);

            String uri = buildThemeSearchUri(theme, region, pageable);
            log.info("테마 검색 요청 URL: {}", uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("테마 검색 API 응답 수신 완료");
            return parseTourResponse(response, pageable);

        } catch (Exception e) {
            log.error("테마별 검색 실패", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * 테마 검색용 URI 생성 (cat1, cat2, cat3 조합)
     */
    private String buildThemeSearchUri(String theme, String region, Pageable pageable) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/areaBasedList2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "TravelPlanner")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("numOfRows", pageable.getPageSize());

        // 지역 코드 추가
        if (region != null && !region.isEmpty()) {
            String areaCode = AREA_CODES.get(region);
            if (areaCode != null) {
                builder.queryParam("areaCode", areaCode);
            }
        }

        // 테마 기반 카테고리 설정
        String[] categories = getThemeCategories(theme);
        if (categories.length > 0) {
            builder.queryParam("cat1", categories[0]);
            if (categories.length > 1) {
                builder.queryParam("cat2", categories[1]);
            }
            if (categories.length > 2) {
                builder.queryParam("cat3", categories[2]);
            }
        }

        return builder.build(false).toUriString();
    }

    /**
     * 테마에 따른 카테고리 코드 반환
     */
    private String[] getThemeCategories(String theme) {
        Map<String, String[]> themeMap = new HashMap<>();
        
        // 한국관광공사 표준 분류 체계
        themeMap.put("자연", new String[]{"A01"});                    // 자연
        themeMap.put("인문", new String[]{"A02"});                    // 인문(문화/예술/역사)
        themeMap.put("레포츠", new String[]{"A03"});                  // 레포츠
        themeMap.put("쇼핑", new String[]{"A04"});                    // 쇼핑
        themeMap.put("음식", new String[]{"A05"});                    // 음식
        themeMap.put("숙박", new String[]{"B02"});                    // 숙박
        themeMap.put("문화시설", new String[]{"A02", "A0206"});       // 문화시설
        themeMap.put("축제", new String[]{"A02", "A0208"});          // 축제공연행사
        themeMap.put("체험", new String[]{"A03", "A0302"});          // 체험관광
        themeMap.put("힐링", new String[]{"A01", "A0101"});          // 자연관광지
        
        return themeMap.getOrDefault(theme, new String[]{});
    }


    /**
     * Fallback 응답 생성
     */
    private CartResponse.TourDetailResponse createFallbackDetailResponse(String contentId) {
        return CartResponse.TourDetailResponse.builder()
                .contentId(contentId)
                .contentTypeId("12")
                .title("상세 정보 조회 중...")
                .address("정보 없음")
                .region("기타")
                .theme("기타")
                .latitude(37.5665)
                .longitude(126.9780)
                .image("")
                .tel("")
                .homepage("")
                .overview("상세 정보를 조회할 수 없습니다.")
                .isFavorite(false)
                .isInCart(false)
                .build();
    }

    /**
     * 지역코드로 지역명 조회
     */
    private String getRegionByAreaCode(String areaCode) {
        return AREA_CODES.entrySet().stream()
                .filter(entry -> entry.getValue().equals(areaCode))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("기타");
    }

    /**
     * 카테고리 코드를 한글명으로 변환
     */
    private String getCategoryName(String cat1, String cat2, String cat3) {
        Map<String, String> categoryNames = new HashMap<>();
        categoryNames.put("A01", "자연");
        categoryNames.put("A02", "인문");
        categoryNames.put("A03", "레포츠");
        categoryNames.put("A04", "쇼핑");
        categoryNames.put("A05", "음식");
        categoryNames.put("B02", "숙박");
        
        return categoryNames.getOrDefault(cat1, "기타");
    }

    /**
     * 문자열을 Double로 안전하게 변환
     */
    private Double parseDouble(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
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