package com.example.backend.tour.entity;

public enum TourCategory {
    TOURIST_SPOT("관광지"),
    RESTAURANT("맛집"),
    ACCOMMODATION("숙소"),
    HEALING("힐링"),
    LEISURE("레저");

    private final String description;

    TourCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}