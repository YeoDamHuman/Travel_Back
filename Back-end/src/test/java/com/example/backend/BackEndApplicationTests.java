package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 임시방편으로 넣어녾는거
class BackEndApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("Spring Application Context loaded successfully!");
    }

}
