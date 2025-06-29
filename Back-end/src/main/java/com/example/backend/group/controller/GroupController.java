package com.example.backend.group.controller;

import com.example.backend.group.dto.request.GroupRequest;
import com.example.backend.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "GroupAPI", description = "그룹 관련 API.")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("create")
    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성하는 API.")
    public ResponseEntity<Void> groupCreate(@RequestBody GroupRequest.groupCreateRequest request) {
        groupService.create(request);
        return ResponseEntity.ok().build();
    }
}
