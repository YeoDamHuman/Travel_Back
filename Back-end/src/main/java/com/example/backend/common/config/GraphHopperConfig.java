// com/example/backend/common/config/GraphHopperConfig.java

package com.example.backend.common.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.CustomModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphHopperConfig {

    private static final String OSM_FILE_PATH = "map-data/south-korea-latest.osm.pbf";
    private static final String GRAPH_CACHE_PATH = "graph-cache";

    @Bean
    public GraphHopper graphHopper() {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(OSM_FILE_PATH);
        hopper.setGraphHopperLocation(GRAPH_CACHE_PATH);

        // 1. Custom Model 생성
        CustomModel customModel = new CustomModel();

        // 2. Custom Model을 사용하는 프로필 설정
        Profile carProfile = new Profile("car")
                .setVehicle("car")
                .setWeighting("custom")
                .putHint("custom_model", customModel);

        hopper.setProfiles(carProfile);


        System.out.println("==> GraphHopper 엔진 로딩을 시작합니다...");
        hopper.importOrLoad();
        System.out.println("==> GraphHopper 엔진 로딩이 완료되었습니다.");

        return hopper;
    }
}