// com/example/backend/map/dto/response/MapResponse.java

package com.example.backend.map.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapResponse {

    private final int order;
    private final String contentId;
    private final double lat;
    private final double lon;
}