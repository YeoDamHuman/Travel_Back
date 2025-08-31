package com.example.backend.weather.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum City {

    // 특별시, 광역시, 특별자치시
    SEOUL("서울", "Seoul", 37.5665, 126.9780),
    BUSAN("부산", "Busan", 35.1796, 129.0756),
    INCHEON("인천", "Incheon", 37.4563, 126.7052),
    DAEGU("대구", "Daegu", 35.8714, 128.6014),
    GWANGJU("광주", "Gwangju", 35.1595, 126.8526),
    DAEJEON("대전", "Daejeon", 36.3504, 127.3845),
    ULSAN("울산", "Ulsan", 35.5384, 129.3114),
    SEJONG("세종", "Sejong", 36.4801, 127.2890),

    // 경기도
    SUWON("수원", "Suwon", 37.2636, 127.0286),
    YONGIN("용인", "Yongin", 37.2411, 127.1775),
    GOYANG("고양", "Goyang", 37.6584, 126.7952),
    SEONGNAM("성남", "Seongnam", 37.4200, 127.1269),
    HWASEONG("화성", "Hwaseong", 37.1995, 126.8315),
    BUCHEON("부천", "Bucheon", 37.5033, 126.7660),
    NAMYANGJU("남양주", "Namyangju", 37.6360, 127.2165),
    ANSAN("안산", "Ansan", 37.3218, 126.8309),
    ANYANG("안양", "Anyang", 37.3943, 126.9568),
    PYEONGTAEK("평택", "Pyeongtaek", 36.9922, 127.1127),
    SIHEUNG("시흥", "Siheung", 37.3801, 126.8029),
    UIJEONGBU("의정부", "Uijeongbu", 37.7381, 127.0338),
    PAJU("파주", "Paju", 37.7615, 126.7785),
    GIMPO("김포", "Gimpo", 37.6153, 126.7157),
    GWANGMYEONG("광명", "Gwangmyeong", 37.4784, 126.8646),
    GWANGJU_GYEONGGI("광주", "Gwangju-si", 37.4296, 127.2553),
    GUNPO("군포", "Gunpo", 37.3614, 126.9351),
    OSAN("오산", "Osan", 37.1498, 127.0775),
    ICHEON("이천", "Icheon", 37.2795, 127.4421),
    YANGJU("양주", "Yangju", 37.7852, 127.0458),
    GURI("구리", "Guri", 37.5999, 127.1402),
    POCHEON("포천", "Pocheon", 37.8949, 127.2003),
    UIWANG("의왕", "Uiwang", 37.3446, 126.9683),
    HANAM("하남", "Hanam", 37.5393, 127.2146),
    ANSEONG("안성", "Anseong", 37.0084, 127.2797),
    YEOJU("여주", "Yeoju", 37.2941, 127.6375),
    DONGDUCHEON("동두천", "Dongducheon", 37.9038, 127.0570),
    GWACHEON("과천", "Gwacheon", 37.4293, 126.9875),

    // 강원특별자치도
    CHUNCHEON("춘천", "Chuncheon", 37.8813, 127.7298),
    WONJU("원주", "Wonju", 37.3422, 127.9202),
    GANGNEUNG("강릉", "Gangneung", 37.7519, 128.8761),
    DONGHAE("동해", "Donghae", 37.5249, 129.1143),
    SOKCHO("속초", "Sokcho", 38.2070, 128.5919),
    SAMCHEOK("삼척", "Samcheok", 37.4498, 129.1653),
    TAEBAEK("태백", "Taebaek", 37.1657, 128.9856),

    // 충청북도
    CHEONGJU("청주", "Cheongju", 36.6425, 127.4891),
    CHUNGJU("충주", "Chungju", 36.9715, 127.9299),
    JECHEON("제천", "Jecheon", 37.1328, 128.1923),

    // 충청남도
    CHEONAN("천안", "Cheonan", 36.8151, 127.1139),
    ASAN("아산", "Asan", 36.7806, 126.9988),
    SEOSAN("서산", "Seosan", 36.7842, 126.4505),
    DANGJIN("당진", "Dangjin", 36.8953, 126.6290),
    GONGJU("공주", "Gongju", 36.4552, 127.1268),
    NONSAN("논산", "Nonsan", 36.2045, 127.0860),
    BORYEONG("보령", "Boryeong", 36.3359, 126.5888),
    GYERYONG("계룡", "Gyeryong", 36.2750, 127.2481),

    // 전북특별자치도
    JEONJU("전주", "Jeonju", 35.8242, 127.1480),
    IKSAN("익산", "Iksan", 35.9472, 126.9577),
    GUNSAN("군산", "Gunsan", 35.9676, 126.7371),
    JEONGEUP("정읍", "Jeongeup", 35.5716, 126.8581),
    NAMWON("남원", "Namwon", 35.4053, 127.3786),
    GIMJE("김제", "Gimje", 35.8028, 126.8831),

    // 전라남도
    MOKPO("목포", "Mokpo", 34.8118, 126.3922),
    YEOSU("여수", "Yeosu", 34.7604, 127.6622),
    SUNCHEON("순천", "Suncheon", 34.9507, 127.4871),
    GWANGYANG("광양", "Gwangyang", 34.9452, 127.6943),
    NAJU("나주", "Naju", 35.0152, 126.7183),

    // 경상북도
    POHANG("포항", "Pohang", 36.0384, 129.3649),
    GUMI("구미", "Gumi", 36.1141, 128.3396),
    GYEONGJU("경주", "Gyeongju", 35.8562, 129.2248),
    GYEONGSAN("경산", "Gyeongsan", 35.8252, 128.7425),
    ANDONG("안동", "Andong", 36.5683, 128.7291),
    GIMCHEON("김천", "Gimcheon", 36.1394, 128.1130),
    YEONGJU("영주", "Yeongju", 36.8063, 128.6219),
    SANGJU("상주", "Sangju", 36.4103, 128.1583),
    YEONGCHEON("영천", "Yeongcheon", 35.9723, 128.9388),
    MUNGYEONG("문경", "Mungyeong", 36.5925, 128.1884),

    // 경상남도
    CHANGWON("창원", "Changwon", 35.2281, 128.6811),
    GIMHAE("김해", "Gimhae", 35.2335, 128.8811),
    JINJU("진주", "Jinju", 35.1804, 128.0834),
    YANGSAN("양산", "Yangsan", 35.3400, 129.0343),
    GEOJE("거제", "Geoje", 34.8816, 128.6212),
    TONGYEONG("통영", "Tongyeong", 34.8544, 128.4332),
    SACHEON("사천", "Sacheon", 35.0927, 128.0617),
    MIRYANG("밀양", "Miryang", 35.5036, 128.7470),

    // 제주특별자치도
    JEJU("제주", "Jeju", 33.4996, 126.5312),
    SEOGWIPO("서귀포", "Seogwipo", 33.2543, 126.5601);

    private final String koreanName;
    private final String englishName;
    private final double lat;
    private final double lon;

    /**
     * 한글 이름으로 해당하는 City Enum 객체 전체를 찾아 반환합니다.
     * @param koreanName 찾을 도시의 한글 이름
     * @return Optional<City> 찾은 City 객체. 없으면 비어있는 Optional
     */
    public static Optional<City> findByKoreanName(String koreanName) {
        // '광주' 검색 시 광주광역시를 우선 반환
        if ("광주".equalsIgnoreCase(koreanName)) {
            return Optional.of(GWANGJU);
        }
        return Arrays.stream(values())
                .filter(city -> city.koreanName.equalsIgnoreCase(koreanName))
                .findFirst();
    }
}