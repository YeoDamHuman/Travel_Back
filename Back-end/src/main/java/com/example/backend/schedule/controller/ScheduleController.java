package com.example.backend.schedule.controller;


import com.example.backend.schedule.dto.request.ScheduleRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.service.ScheduleService;
import com.example.backend.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("")
    public ResponseEntity<ScheduleResponse.scheduleCreateResponse> create(@RequestBody ScheduleRequest.scheduleCreateRequest request) {
        try {
            UUID scheduleId = scheduleService.createSchedule(request);

            ScheduleResponse.scheduleCreateResponse response = ScheduleResponse.scheduleCreateResponse.builder()
                    .scheduleId(scheduleId)
                    .message("스케쥴이 성공적으로 생성되었습니다")
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ScheduleResponse.scheduleCreateResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("")
    public ResponseEntity<List<ScheduleResponse.scheduleInfo>>

}
