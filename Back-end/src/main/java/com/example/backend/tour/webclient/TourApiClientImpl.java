package com.example.backend.tour.webclient;

import com.example.backend.tour.dto.response.TourResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TourApiClientImpl implements TourApiClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final String baseUrl;

    public TourApiClientImpl(WebClient.Builder webClientBuilder, @Value("${tour.api.base-url}") String baseUrl, @Value("${tour.api.key}") String serviceKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    private static final Map<String, String> REGION_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put("관광지", "12");
        CATEGORY_MAP.put("문화시설", "14");
        CATEGORY_MAP.put("축제공연행사", "15");
        CATEGORY_MAP.put("여행코스", "25");
        CATEGORY_MAP.put("레포츠", "28");
        CATEGORY_MAP.put("숙박", "32");
        CATEGORY_MAP.put("쇼핑", "38");
        CATEGORY_MAP.put("음식점", "39");

        REGION_MAP.put("서울", "1");
        REGION_MAP.put("인천", "2");
        REGION_MAP.put("대전", "3");
        REGION_MAP.put("대구", "4");
        REGION_MAP.put("광주", "5");
        REGION_MAP.put("부산", "6");
        REGION_MAP.put("울산", "7");
        REGION_MAP.put("세종", "8");
        REGION_MAP.put("경기", "31");
        REGION_MAP.put("강원", "32");
        REGION_MAP.put("충북", "33");
        REGION_MAP.put("충남", "34");
        REGION_MAP.put("경북", "35");
        REGION_MAP.put("경남", "36");
        REGION_MAP.put("전북", "37");
        REGION_MAP.put("전남", "38");
        REGION_MAP.put("제주", "39");
    }

    @Override
    public Page<TourResponse> searchTours(String keyword, String region, String category, Pageable pageable) {
        UriComponentsBuilder uriBuilder;
        String endpoint;

        if (keyword != null && !keyword.isEmpty()) {
            endpoint = "/searchKeyword2";
        } else {
            endpoint = "/areaBasedList2clau";
        }

        String url = baseUrl + endpoint;
        uriBuilder = UriComponentsBuilder.fromUriString(url)
                .queryParam("serviceKey", URLEncoder.encode(serviceKey, StandardCharsets.UTF_8))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "TravelPlanner")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("numOfRows", pageable.getPageSize());

        if (keyword != null && !keyword.isEmpty()) {
            uriBuilder.queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        }

        if (region != null && !region.isEmpty()) {
            String areaCode = REGION_MAP.get(region);
            if (areaCode != null) {
                uriBuilder.queryParam("areaCode", URLEncoder.encode(areaCode, StandardCharsets.UTF_8));
            } else {
            }
        }

        if (category != null && !category.isEmpty()) {
            String contentTypeId = CATEGORY_MAP.get(category);
            if (contentTypeId != null) {
                uriBuilder.queryParam("contentTypeId", URLEncoder.encode(contentTypeId, StandardCharsets.UTF_8));
            } else {
            }
        }

        URI uri = uriBuilder.build(true).toUri();


        Map<String, Object> apiResponse = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (apiResponse == null || !apiResponse.containsKey("response")) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Map<String, Object> responseBody = (Map<String, Object>) ((Map<String, Object>) apiResponse.get("response")).get("body");
        if (responseBody == null || !responseBody.containsKey("items")) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Map<String, Object> items = (Map<String, Object>) responseBody.get("items");
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");

        if (itemList == null) {
            itemList = Collections.emptyList();
        }

        long totalCount = ((Number) responseBody.get("totalCount")).longValue();

        List<TourResponse> tourResponses = itemList.stream()
                .map(itemMap -> {
                    TourResponse.TourResponseBuilder builder = TourResponse.builder();
                    builder.contentId(getString(itemMap, "contentid"));
                    builder.contentTypeId(getString(itemMap, "contenttypeid"));
                    builder.title(getString(itemMap, "title"));
                    builder.address(getString(itemMap, "addr1"));
                    builder.address2(getString(itemMap, "addr2"));
                    builder.zipcode(getString(itemMap, "zipcode"));
                    builder.areaCode(getString(itemMap, "areacode"));
                    builder.cat1(getString(itemMap, "cat1"));
                    builder.cat2(getString(itemMap, "cat2"));
                    builder.cat3(getString(itemMap, "cat3"));
                    builder.createdTime(getString(itemMap, "createdtime"));
                    builder.firstImage(getString(itemMap, "firstimage"));
                    builder.firstImage2(getString(itemMap, "firstimage2"));
                    builder.cpyrhtDivCd(getString(itemMap, "cpyrhtDivCd"));
                    builder.mapX(getString(itemMap, "mapx"));
                    builder.mapY(getString(itemMap, "mapy"));
                    builder.mlevel(getString(itemMap, "mlevel"));
                    builder.modifiedTime(getString(itemMap, "modifiedtime"));
                    builder.sigunguCode(getString(itemMap, "sigungucode"));
                    builder.tel(getString(itemMap, "tel"));
                    builder.lDongSignguCd(getString(itemMap, "ldongSignguCd"));
                    builder.lDongRegnCd(getString(itemMap, "ldongRegnCd"));
                    builder.overview(getString(itemMap, "overview"));
                    return builder.build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(tourResponses, pageable, totalCount);
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    @Override
    public TourResponse fetchTourDetails(String contentId) {
        String url = baseUrl + "/detailCommon2";
        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "Y")
                .queryParam("areacodeYN", "Y")
                .queryParam("catcodeYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("mapinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .build(true).toUri();

        Map<String, Object> apiResponse = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (apiResponse == null || !apiResponse.containsKey("response")) {
            return null;
        }

        Map<String, Object> responseBody = (Map<String, Object>) ((Map<String, Object>) apiResponse.get("response")).get("body");
        if (responseBody == null || !responseBody.containsKey("items")) {
            return null;
        }

        Map<String, Object> items = (Map<String, Object>) responseBody.get("items");
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");

        if (itemList == null || itemList.isEmpty()) {
            return null;
        }

        Map<String, Object> itemMap = itemList.get(0);

        TourResponse.TourResponseBuilder builder = TourResponse.builder();
        builder.contentId(getString(itemMap, "contentid"));
        builder.contentTypeId(getString(itemMap, "contenttypeid"));
        builder.title(getString(itemMap, "title"));
        builder.address(getString(itemMap, "addr1"));
        builder.address2(getString(itemMap, "addr2"));
        builder.zipcode(getString(itemMap, "zipcode"));
        builder.areaCode(getString(itemMap, "areacode"));
        builder.cat1(getString(itemMap, "cat1"));
        builder.cat2(getString(itemMap, "cat2"));
        builder.cat3(getString(itemMap, "cat3"));
        builder.createdTime(getString(itemMap, "createdtime"));
        builder.firstImage(getString(itemMap, "firstimage"));
        builder.firstImage2(getString(itemMap, "firstimage2"));
        builder.cpyrhtDivCd(getString(itemMap, "cpyrhtDivCd"));
        builder.mapX(getString(itemMap, "mapx"));
        builder.mapY(getString(itemMap, "mapy"));
        builder.mlevel(getString(itemMap, "mlevel"));
        builder.modifiedTime(getString(itemMap, "modifiedtime"));
        builder.sigunguCode(getString(itemMap, "sigungucode"));
        builder.tel(getString(itemMap, "tel"));
        builder.lDongSignguCd(getString(itemMap, "ldongSignguCd"));
        builder.lDongRegnCd(getString(itemMap, "ldongRegnCd"));
        builder.overview(getString(itemMap, "overview"));

        return builder.build();
    }
}
