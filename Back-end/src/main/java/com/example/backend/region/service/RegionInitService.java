package com.example.backend.region.service;

import com.example.backend.region.entity.Region;
import com.example.backend.region.repository.RegionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionInitService {

    private final RegionRepository regionRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${tour.api.key}")
    private String apiKey;

    @Value("${tour.api.base-url:http://apis.data.go.kr/B551011/KorService2}")
    private String baseUrl;

    @PostConstruct
    @Transactional
    public void initializeRegions() {
        try {
            long count = regionRepository.count();
            if (count > 0) {
                log.info("Region 데이터가 이미 존재합니다. 초기화를 건너뜁니다. (현재 개수: {})", count);
                return;
            }

            log.info("Region 데이터 초기화를 시작합니다...");
            
            List<Region> regions = fetchRegionsFromApi();
            if (!regions.isEmpty()) {
                regionRepository.saveAll(regions);
                log.info("Region 데이터 초기화 완료: {}개 지역", regions.size());
            } else {
                log.warn("API에서 지역 데이터를 가져오지 못했습니다. 기본 데이터를 생성합니다.");
                createDefaultRegions();
            }
        } catch (Exception e) {
            log.error("Region 데이터 초기화 중 오류 발생", e);
            createDefaultRegions();
        }
    }

    private List<Region> fetchRegionsFromApi() {
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/ldongCode2")
                    .queryParam("serviceKey", apiKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TravelPlanner")
                    .queryParam("_type", "json")
                    .queryParam("lDongListYn", "Y")
                    .queryParam("numOfRows", "300")
                    .queryParam("pageNo", "1")
                    .build(false)
                    .toUriString();

            log.info("법정동 코드 API 호출: {}", uri);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return parseRegionResponse(response);

        } catch (Exception e) {
            log.error("법정동 코드 API 호출 실패", e);
            return new ArrayList<>();
        }
    }

    private List<Region> parseRegionResponse(String response) {
        List<Region> regions = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            
            String resultCode = responseNode.path("header").path("resultCode").asText();
            if (!"0000".equals(resultCode)) {
                log.error("지역코드 API 오류: {}", responseNode.path("header").path("resultMsg").asText());
                return regions;
            }

            JsonNode items = responseNode.path("body").path("items");
            JsonNode itemNode = items.path("item");

            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    Region region = createRegionFromApiData(item);
                    if (region != null) {
                        regions.add(region);
                    }
                }
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                Region region = createRegionFromApiData(itemNode);
                if (region != null) {
                    regions.add(region);
                }
            }

        } catch (Exception e) {
            log.error("지역 데이터 파싱 실패", e);
        }
        
        return regions;
    }

    private Region createRegionFromApiData(JsonNode item) {
        try {
            String lDongRegnCd = item.path("lDongRegnCd").asText("");
            String lDongRegnNm = item.path("lDongRegnNm").asText("");
            String lDongSignguCd = item.path("lDongSignguCd").asText("");
            String lDongSignguNm = item.path("lDongSignguNm").asText("");

            if (lDongRegnCd.isEmpty() || lDongSignguCd.isEmpty() || lDongSignguNm.isEmpty()) {
                log.warn("잘못된 지역 데이터: lDongRegnCd={}, lDongSignguCd={}, lDongSignguNm={}", 
                         lDongRegnCd, lDongSignguCd, lDongSignguNm);
                return null;
            }

            // regionCode는 lDongRegnCd + lDongSignguCd 조합으로 생성
            String regionCode = lDongRegnCd + lDongSignguCd;
            String regionName = lDongSignguNm; // 시/군/구명 사용

            return Region.builder()
                    .regionCode(regionCode)
                    .regionName(regionName)
                    .lDongRegnCd(lDongRegnCd)
                    .lDongSignguCd(lDongSignguCd)
                    .regionImage(getDefaultRegionImage(regionName))
                    .description(regionName + " 지역의 관광정보")
                    .build();

        } catch (Exception e) {
            log.error("Region 객체 생성 실패", e);
            return null;
        }
    }

    private String getDefaultRegionImage(String regionName) {
        Map<String, String> regionImages = new HashMap<>();
        regionImages.put("서울특별시", "https://example.com/images/seoul.jpg");
        regionImages.put("인천광역시", "https://example.com/images/incheon.jpg");
        regionImages.put("대전광역시", "https://example.com/images/daejeon.jpg");
        regionImages.put("대구광역시", "https://example.com/images/daegu.jpg");
        regionImages.put("광주광역시", "https://example.com/images/gwangju.jpg");
        regionImages.put("부산광역시", "https://example.com/images/busan.jpg");
        regionImages.put("울산광역시", "https://example.com/images/ulsan.jpg");
        regionImages.put("세종특별자치시", "https://example.com/images/sejong.jpg");
        regionImages.put("경기도", "https://example.com/images/gyeonggi.jpg");
        regionImages.put("강원도", "https://example.com/images/gangwon.jpg");
        regionImages.put("충청북도", "https://example.com/images/chungbuk.jpg");
        regionImages.put("충청남도", "https://example.com/images/chungnam.jpg");
        regionImages.put("경상북도", "https://example.com/images/gyeongbuk.jpg");
        regionImages.put("경상남도", "https://example.com/images/gyeongnam.jpg");
        regionImages.put("전라북도", "https://example.com/images/jeonbuk.jpg");
        regionImages.put("전라남도", "https://example.com/images/jeonnam.jpg");
        regionImages.put("제주특별자치도", "https://example.com/images/jeju.jpg");

        return regionImages.getOrDefault(regionName, "https://example.com/images/default.jpg");
    }

    @Transactional
    public void createDefaultRegions() {
        try {
            log.info("기본 지역 데이터를 생성합니다 (시 단위)...");
            
            List<Region> defaultRegions = new ArrayList<>();
            
            // 주요 시 데이터 (lDongRegnCd, lDongSignguCd, regionName) - API에서 실제 확인한 데이터
            String[][] cityData = {
                    {"36110", "36110", "세종특별자치시"},
                    {"41", "110", "수원시"}, {"41", "130", "성남시"}, {"41", "150", "의정부시"}, {"41", "170", "안양시"}, {"41", "190", "부천시"},
                    {"41", "210", "광명시"}, {"41", "220", "평택시"}, {"41", "270", "안산시"}, {"41", "280", "고양시"}, {"41", "460", "용인시"},
                    {"43", "110", "청주시"}, {"43", "130", "충주시"}, {"43", "150", "제천시"},
                    {"44", "130", "천안시"}, {"44", "150", "공주시"}, {"44", "180", "보령시"}, {"44", "200", "아산시"},
                    {"46", "110", "목포시"}, {"46", "130", "여수시"}, {"46", "150", "순천시"}, {"46", "170", "나주시"}, {"46", "230", "광양시"},
                    {"47", "110", "포항시"}, {"47", "130", "경주시"}, {"47", "150", "김천시"}, {"47", "170", "안동시"}, {"47", "190", "구미시"},
                    {"48", "120", "창원시"}, {"48", "170", "진주시"}, {"48", "220", "통영시"}, {"48", "250", "김해시"}, {"48", "330", "양산시"},
                    {"50", "110", "제주시"}, {"50", "130", "서귀포시"},
                    {"51", "110", "춘천시"}, {"51", "130", "원주시"}, {"51", "150", "강릉시"}, {"51", "170", "동해시"}, {"51", "210", "속초시"}, {"51", "230", "삼척시"},
                    {"52", "110", "전주시"}, {"52", "130", "군산시"}, {"52", "140", "익산시"}, {"52", "180", "정읍시"}
            };
            
            for (String[] data : cityData) {
                String lDongRegnCd = data[0];
                String lDongSignguCd = data[1]; 
                String regionName = data[2];
                String regionCode = lDongRegnCd + lDongSignguCd;
                
                Region region = Region.builder()
                        .regionCode(regionCode)
                        .regionName(regionName)
                        .lDongRegnCd(lDongRegnCd)
                        .lDongSignguCd(lDongSignguCd)
                        .regionImage(getDefaultRegionImage(regionName))
                        .description(regionName + " 지역의 관광정보")
                        .build();
                defaultRegions.add(region);
            }
            
            regionRepository.saveAll(defaultRegions);
            log.info("기본 지역 데이터 생성 완료: {}개 시", defaultRegions.size());
            
        } catch (Exception e) {
            log.error("기본 지역 데이터 생성 실패", e);
        }
    }

    @Transactional
    public void refreshRegions() {
        log.info("지역 데이터 갱신을 시작합니다...");
        
        regionRepository.deleteAll();
        
        List<Region> regions = fetchRegionsFromApi();
        if (!regions.isEmpty()) {
            regionRepository.saveAll(regions);
            log.info("지역 데이터 갱신 완료: {}개 지역", regions.size());
        } else {
            createDefaultRegions();
        }
    }
}