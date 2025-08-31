package com.example.backend.weather.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum City {

    // 특별시, 광역시, 특별자치시
    SEOUL("서울", "Seoul"),
    BUSAN("부산", "Busan"),
    INCHEON("인천", "Incheon"),
    DAEGU("대구", "Daegu"),
    GWANGJU("광주", "Gwangju"),
    DAEJEON("대전", "Daejeon"),
    ULSAN("울산", "Ulsan"),
    SEJONG("세종", "Sejong"),

    // 경기도
    SUWON("수원", "Suwon"),
    YONGIN("용인", "Yongin"),
    GOYANG("고양", "Goyang"),
    SEONGNAM("성남", "Seongnam"),
    HWASEONG("화성", "Hwaseong"),
    BUCHEON("부천", "Bucheon"),
    NAMYANGJU("남양주", "Namyangju"),
    ANSAN("안산", "Ansan"),
    ANYANG("안양", "Anyang"),
    PYEONGTAEK("평택", "Pyeongtaek"),
    SIHEUNG("시흥", "Siheung"),
    UIJEONGBU("의정부", "Uijeongbu"),
    PAJU("파주", "Paju"),
    GIMPO("김포", "Gimpo"),
    GWANGMYEONG("광명", "Gwangmyeong"),
    GWANGJU_GYEONGGI("광주", "Gwangju-si"), // 전라도 광주와 구분
    GUNPO("군포", "Gunpo"),
    OSAN("오산", "Osan"),
    ICHEON("이천", "Icheon"),
    YANGJU("양주", "Yangju"),
    GURI("구리", "Guri"),
    POCHEON("포천", "Pocheon"),
    UIWANG("의왕", "Uiwang"),
    HANAM("하남", "Hanam"),
    ANSEONG("안성", "Anseong"),
    YEOJU("여주", "Yeoju"),
    DONGDUCHEON("동두천", "Dongducheon"),
    GWACHEON("과천", "Gwacheon"),

    // 강원특별자치도
    CHUNCHEON("춘천", "Chuncheon"),
    WONJU("원주", "Wonju"),
    GANGNEUNG("강릉", "Gangneung"),
    DONGHAE("동해", "Donghae"),
    SOKCHO("속초", "Sokcho"),
    SAMCHEOK("삼척", "Samcheok"),
    TAEBAEK("태백", "Taebaek"),

    // 충청북도
    CHEONGJU("청주", "Cheongju"),
    CHUNGJU("충주", "Chungju"),
    JECHEON("제천", "Jecheon"),

    // 충청남도
    CHEONAN("천안", "Cheonan"),
    ASAN("아산", "Asan"),
    SEOSAN("서산", "Seosan"),
    DANGJIN("당진", "Dangjin"),
    GONGJU("공주", "Gongju"),
    NONSAN("논산", "Nonsan"),
    BORYEONG("보령", "Boryeong"),
    GYERYONG("계룡", "Gyeryong"),

    // 전북특별자치도
    JEONJU("전주", "Jeonju"),
    IKSAN("익산", "Iksan"),
    GUNSAN("군산", "Gunsan"),
    JEONGEUP("정읍", "Jeongeup"),
    NAMWON("남원", "Namwon"),
    GIMJE("김제", "Gimje"),

    // 전라남도
    MOKPO("목포", "Mokpo"),
    YEOSU("여수", "Yeosu"),
    SUNCHEON("순천", "Suncheon"),
    GWANGYANG("광양", "Gwangyang"),
    NAJU("나주", "Naju"),

    // 경상북도
    POHANG("포항", "Pohang"),
    GUMI("구미", "Gumi"),
    GYEONGJU("경주", "Gyeongju"),
    GYEONGSAN("경산", "Gyeongsan"),
    ANDONG("안동", "Andong"),
    GIMCHEON("김천", "Gimcheon"),
    YEONGJU("영주", "Yeongju"),
    SANGJU("상주", "Sangju"),
    YEONGCHEON("영천", "Yeongcheon"),
    MUNGYEONG("문경", "Mungyeong"),

    // 경상남도
    CHANGWON("창원", "Changwon"),
    GIMHAE("김해", "Gimhae"),
    JINJU("진주", "Jinju"),
    YANGSAN("양산", "Yangsan"),
    GEOJE("거제", "Geoje"),
    TONGYEONG("통영", "Tongyeong"),
    SACHEON("사천", "Sacheon"),
    MIRYANG("밀양", "Miryang"),

    // 제주특별자치도
    JEJU("제주", "Jeju"),
    SEOGWIPO("서귀포", "Seogwipo");

    private final String koreanName;
    private final String englishName;

    /**
     * 입력된 한글 도시 이름에 해당하는 영문 이름을 찾아 반환합니다.
     * 일치하는 도시가 없으면 입력된 값을 그대로 반환합니다.
     * @param koreanName 변환할 한글 도시 이름
     * @return 변환된 영문 도시 이름 또는 원본 값
     */
    public static String findEnglishNameByKoreanName(String koreanName) {
        // '광주' 검색 시 기본적으로 광주광역시를 반환하도록 처리
        if ("광주".equalsIgnoreCase(koreanName)) {
            return GWANGJU.getEnglishName();
        }

        for (City city : values()) {
            if (city.koreanName.equalsIgnoreCase(koreanName)) {
                return city.englishName;
            }
        }
        return koreanName; // 매핑되는 도시가 없으면 원본 이름 반환
    }
}